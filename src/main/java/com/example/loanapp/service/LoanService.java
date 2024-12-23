package com.example.loanapp.service;

import com.example.loanapp.dto.CreateLoanRequest;
import com.example.loanapp.dto.PayLoanRequest;
import com.example.loanapp.dto.PaymentResponse;
import com.example.loanapp.dto.CustomerLoanDTO;
import com.example.loanapp.dto.LoanInstallmentDTO;
import com.example.loanapp.dto.LoanHistoryDTO;
import com.example.loanapp.dto.PaymentPlanDTO;
import com.example.loanapp.dto.RiskAnalysisDTO;
import com.example.loanapp.dto.EarlyClosureDTO;
import com.example.loanapp.exception.LoanValidationException;
import com.example.loanapp.model.Customer;
import com.example.loanapp.model.Loan;
import com.example.loanapp.model.LoanInstallment;
import com.example.loanapp.repository.CustomerRepository;
import com.example.loanapp.repository.LoanInstallmentRepository;
import com.example.loanapp.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanService {
    private static final Set<Integer> VALID_INSTALLMENTS = Set.of(6, 9, 12, 24);
    private static final BigDecimal MIN_INTEREST_RATE = new BigDecimal("0.1");
    private static final BigDecimal MAX_INTEREST_RATE = new BigDecimal("0.5");

    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository installmentRepository;
    private final CustomerRepository customerRepository;

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);

    @Transactional
    public Loan createLoan(CreateLoanRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new LoanValidationException("Customer not found"));

        validateCustomerLimit(customer, request.getAmount());

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanAmount(request.getAmount());
        loan.setNumberOfInstallments(request.getNumberOfInstallments());
        loan.setInterestRate(request.getInterestRate());
        loan.setCreateDate(LocalDateTime.now());
        loan.setPaid(false);

        loan = loanRepository.save(loan);
        List<LoanInstallment> installments = createInstallments(loan);
        installmentRepository.saveAll(installments);
        
        updateCustomerLimit(customer, request.getAmount());
        
        return loan;
    }

    @Transactional
    public PaymentResponse payLoan(PayLoanRequest request) {
        Loan loan = loanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new LoanValidationException("Loan not found"));

        if (loan.isPaid()) {
            throw new LoanValidationException("Loan is already paid");
        }

        List<LoanInstallment> unpaidInstallments = loan.getInstallments().stream()
                .filter(i -> !i.isPaid())
                .filter(i -> !i.getDueDate().isAfter(LocalDate.now().plusMonths(3)))
                .sorted(Comparator.comparing(LoanInstallment::getDueDate))
                .collect(Collectors.toList());

        if (unpaidInstallments.isEmpty()) {
            throw new LoanValidationException("No eligible installments found within 3 months");
        }

        BigDecimal maxPayableAmount = unpaidInstallments.stream()
                .map(LoanInstallment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (request.getAmount().compareTo(maxPayableAmount) > 0) {
            throw new LoanValidationException("Cannot pay more than the total of next 3 months installments: " + maxPayableAmount);
        }

        int paidCount = 0;
        BigDecimal remainingAmount = request.getAmount();

        for (LoanInstallment installment : unpaidInstallments) {
            if (remainingAmount.compareTo(installment.getAmount()) >= 0) {
                long daysDifference = ChronoUnit.DAYS.between(LocalDate.now(), installment.getDueDate());
                BigDecimal actualPaidAmount = installment.getAmount();

                if (daysDifference > 0) {
                    // Erken ödeme indirimi
                    BigDecimal discount = installment.getAmount()
                            .multiply(new BigDecimal("0.001"))
                            .multiply(BigDecimal.valueOf(daysDifference));
                    actualPaidAmount = actualPaidAmount.subtract(discount);
                } else if (daysDifference < 0) {
                    // Geç ödeme cezası
                    BigDecimal penalty = installment.getAmount()
                            .multiply(new BigDecimal("0.001"))
                            .multiply(BigDecimal.valueOf(Math.abs(daysDifference)));
                    actualPaidAmount = actualPaidAmount.add(penalty);
                }

                installment.setPaid(true);
                installment.setPaidAmount(actualPaidAmount);
                installment.setPaymentDate(LocalDate.now());
                installmentRepository.save(installment);

                remainingAmount = remainingAmount.subtract(installment.getAmount());
                paidCount++;
            } else {
                break;
            }

        }

        boolean isFullyPaid = loan.getInstallments().stream().allMatch(LoanInstallment::isPaid);
        if (isFullyPaid) {
            loan.setPaid(true);
            loanRepository.save(loan);
            updateCustomerLimitAfterPayment(loan.getCustomer(), loan.getLoanAmount());
        }

        return PaymentResponse.builder()
                .paidInstallments(paidCount)
                .totalPaidAmount(request.getAmount().subtract(remainingAmount))
                .isLoanFullyPaid(isFullyPaid)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CustomerLoanDTO> getCustomerLoans(Long customerId) {
        return loanRepository.findByCustomerId(customerId).stream()
                .map(this::convertToCustomerLoanDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoanInstallmentDTO> getLoanInstallments(Long loanId) {
        logger.debug("Fetching installments for loan ID: {}", loanId);
        return loanRepository.findById(loanId)
                .map(loan -> loan.getInstallments().stream()
                        .map(this::convertToInstallmentDTO)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new LoanValidationException("Loan not found"));
    }

    @Transactional(readOnly = true)
    public List<LoanHistoryDTO> getLoanHistory(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanValidationException("Loan not found"));

        List<LoanHistoryDTO> history = new ArrayList<>();
        
        // Kredi oluşturma kaydı
        history.add(LoanHistoryDTO.builder()
                .transactionDate(loan.getCreateDate())
                .transactionType("CREATION")
                .amount(loan.getLoanAmount())
                .remainingDebt(loan.getLoanAmount())
                .description("Loan created")
                .build());

        // Ödeme kayıtları
        loan.getInstallments().stream()
                .filter(LoanInstallment::isPaid)
                .forEach(installment -> {
                    BigDecimal remainingDebt = calculateRemainingDebtAtDate(loan, installment.getPaymentDate());
                    history.add(LoanHistoryDTO.builder()
                            .transactionDate(installment.getPaymentDate().atStartOfDay())
                            .transactionType("PAYMENT")
                            .amount(installment.getPaidAmount())
                            .remainingDebt(remainingDebt)
                            .description(generatePaymentDescription(installment))
                            .build());
                });

        return history.stream()
                .sorted(Comparator.comparing(LoanHistoryDTO::getTransactionDate))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentPlanDTO> getPaymentPlan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanValidationException("Loan not found"));

        BigDecimal totalAmount = loan.getLoanAmount()
                .multiply(BigDecimal.ONE.add(loan.getInterestRate()));
        BigDecimal installmentAmount = totalAmount
                .divide(BigDecimal.valueOf(loan.getNumberOfInstallments()), 2, RoundingMode.HALF_UP);
        BigDecimal principalPerInstallment = loan.getLoanAmount()
                .divide(BigDecimal.valueOf(loan.getNumberOfInstallments()), 2, RoundingMode.HALF_UP);

        List<PaymentPlanDTO> plan = new ArrayList<>();
        BigDecimal remainingPrincipal = loan.getLoanAmount();

        for (LoanInstallment installment : loan.getInstallments()) {
            plan.add(PaymentPlanDTO.builder()
                    .installmentNumber(plan.size() + 1)
                    .dueDate(installment.getDueDate())
                    .installmentAmount(installmentAmount)
                    .principalAmount(principalPerInstallment)
                    .interestAmount(installmentAmount.subtract(principalPerInstallment))
                    .remainingPrincipal(remainingPrincipal)
                    .isPaid(installment.isPaid())
                    .build());

            remainingPrincipal = remainingPrincipal.subtract(principalPerInstallment);
        }

        return plan;
    }

    @Transactional(readOnly = true)
    public RiskAnalysisDTO analyzeCustomerRisk(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new LoanValidationException("Customer not found"));

        List<Loan> activeLoans = loanRepository.findByCustomerId(customerId).stream()
                .filter(loan -> !loan.isPaid())
                .collect(Collectors.toList());

        int latePayments = (int) activeLoans.stream()
                .flatMap(loan -> loan.getInstallments().stream())
                .filter(installment -> !installment.isPaid() && 
                        installment.getDueDate().isBefore(LocalDate.now()))
                .count();

        BigDecimal totalDebt = activeLoans.stream()
                .flatMap(loan -> loan.getInstallments().stream())
                .filter(i -> !i.isPaid())
                .map(LoanInstallment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String riskLevel = calculateRiskLevel(customer, latePayments, totalDebt);

        return RiskAnalysisDTO.builder()
                .customerId(customerId)
                .riskLevel(riskLevel)
                .totalDebt(totalDebt)
                .unusedCreditLimit(customer.getCreditLimit().subtract(customer.getUsedCreditLimit()))
                .activeLoans(activeLoans.size())
                .latePayments(latePayments)
                .creditScore(calculateCreditScore(latePayments, totalDebt))
                .recommendation(generateRecommendation(riskLevel))
                .build();
    }

    @Transactional(readOnly = true)
    public EarlyClosureDTO calculateEarlyClosure(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanValidationException("Loan not found"));

        BigDecimal remainingDebt = loan.getInstallments().stream()
                .filter(i -> !i.isPaid())
                .map(LoanInstallment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal savedInterest = calculateSavedInterest(loan);
        BigDecimal earlyClosureDiscount = savedInterest.multiply(new BigDecimal("0.5")); // 50% indirim
        BigDecimal finalAmount = remainingDebt.subtract(earlyClosureDiscount);

        return EarlyClosureDTO.builder()
                .totalRemainingDebt(remainingDebt)
                .earlyClosureAmount(finalAmount)
                .totalDiscount(earlyClosureDiscount)
                .savedInterest(savedInterest)
                .closureDate(LocalDate.now())
                .paymentInstructions("Please pay the early closure amount to complete the loan closure.")
                .build();
    }

    private BigDecimal calculateRemainingDebtAtDate(Loan loan, LocalDate date) {
        return loan.getInstallments().stream()
                .filter(i -> !i.isPaid() || i.getPaymentDate().isAfter(date))
                .map(LoanInstallment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generatePaymentDescription(LoanInstallment installment) {
        long daysDifference = ChronoUnit.DAYS.between(LocalDate.now(), installment.getDueDate());
        if (daysDifference > 0) {
            return "Early payment with discount";
        } else if (daysDifference < 0) {
            return "Late payment with penalty";
        }
        return "Regular payment";
    }

    private void validateCustomerLimit(Customer customer, BigDecimal amount) {
        if (customer.getUsedCreditLimit().add(amount).compareTo(customer.getCreditLimit()) > 0) {
            throw new LoanValidationException("Insufficient credit limit");
        }
    }

    private void updateCustomerLimit(Customer customer, BigDecimal amount) {
        customer.setUsedCreditLimit(customer.getUsedCreditLimit().add(amount));
        customerRepository.save(customer);
    }

    private void updateCustomerLimitAfterPayment(Customer customer, BigDecimal amount) {
        customer.setUsedCreditLimit(customer.getUsedCreditLimit().subtract(amount));
        customerRepository.save(customer);
    }

    private List<LoanInstallment> createInstallments(Loan loan) {
        List<LoanInstallment> installments = new ArrayList<>();
        BigDecimal totalAmount = loan.getLoanAmount().multiply(BigDecimal.ONE.add(loan.getInterestRate()));
        BigDecimal installmentAmount = totalAmount.divide(
            BigDecimal.valueOf(loan.getNumberOfInstallments()), 2, RoundingMode.HALF_UP);
        
        LocalDate firstDueDate = LocalDate.now().withDayOfMonth(1).plusMonths(1);
        
        for (int i = 0; i < loan.getNumberOfInstallments(); i++) {
            installments.add(LoanInstallment.builder()
                    .loan(loan)
                    .amount(installmentAmount)
                    .paidAmount(BigDecimal.ZERO)
                    .dueDate(firstDueDate.plusMonths(i))
                    .paid(false)
                    .build());
        }
        
        return installments;
    }

    private LoanInstallmentDTO convertToInstallmentDTO(LoanInstallment installment) {
        return LoanInstallmentDTO.builder()
                .id(installment.getId())
                .amount(installment.getAmount())
                .paidAmount(installment.getPaidAmount())
                .dueDate(installment.getDueDate())
                .paymentDate(installment.getPaymentDate())
                .paid(installment.isPaid())
                .build();
    }

    private CustomerLoanDTO convertToCustomerLoanDTO(Loan loan) {
        BigDecimal remainingAmount = loan.getInstallments().stream()
                .filter(i -> !i.isPaid())
                .map(LoanInstallment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CustomerLoanDTO.builder()
                .id(loan.getId())
                .loanAmount(loan.getLoanAmount())
                .remainingAmount(remainingAmount)
                .numberOfInstallments(loan.getNumberOfInstallments())
                .createDate(loan.getCreateDate())
                .isPaid(loan.isPaid())
                .build();
    }

    private Double calculateCreditScore(int latePayments, BigDecimal totalDebt) {
        return 100.0 - (latePayments * 10) - (totalDebt.doubleValue() / 1000);
    }

    private String calculateRiskLevel(Customer customer, int latePayments, BigDecimal totalDebt) {
        if (latePayments > 3 || totalDebt.compareTo(customer.getCreditLimit()) > 0) {
            return "HIGH";
        } else if (latePayments > 1 || totalDebt.compareTo(customer.getCreditLimit().multiply(new BigDecimal("0.7"))) > 0) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String generateRecommendation(String riskLevel) {
        switch (riskLevel) {
            case "HIGH":
                return "Credit applications should be carefully evaluated. Debt restructuring might be needed.";
            case "MEDIUM":
                return "New credit applications can be considered with additional guarantees.";
            default:
                return "Customer is eligible for new credit applications.";
        }
    }

    private BigDecimal calculateSavedInterest(Loan loan) {
        return loan.getInstallments().stream()
                .filter(i -> !i.isPaid())
                .map(i -> i.getAmount().subtract(
                    loan.getLoanAmount().divide(
                        BigDecimal.valueOf(loan.getNumberOfInstallments()), 
                        2, 
                        RoundingMode.HALF_UP
                    )
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 