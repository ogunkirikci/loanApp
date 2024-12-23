package com.example.loanapp.service;

import com.example.loanapp.dto.DetailedPaymentResponse;
import com.example.loanapp.dto.InstallmentPaymentDetail;
import com.example.loanapp.dto.PaymentCalculation;
import com.example.loanapp.model.LoanInstallment;
import com.example.loanapp.repository.LoanInstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final LoanInstallmentRepository installmentRepository;
    
    public PaymentCalculation calculatePayment(LoanInstallment installment, LocalDate paymentDate) {
        long daysDifference = ChronoUnit.DAYS.between(paymentDate, installment.getDueDate());
        BigDecimal adjustedAmount = installment.getAmount();
        
        if (daysDifference > 0) {
            // Early payment discount
            BigDecimal discount = calculateEarlyPaymentDiscount(installment.getAmount(), daysDifference);
            adjustedAmount = adjustedAmount.subtract(discount);
        } else if (daysDifference < 0) {
            // Late payment penalty
            BigDecimal penalty = calculateLatePaymentPenalty(installment.getAmount(), Math.abs(daysDifference));
            adjustedAmount = adjustedAmount.add(penalty);
        }
        
        return new PaymentCalculation(adjustedAmount, daysDifference);
    }
    
    public DetailedPaymentResponse processPayment(List<LoanInstallment> installments, BigDecimal paymentAmount) {
        BigDecimal remainingAmount = paymentAmount;
        List<InstallmentPaymentDetail> paymentDetails = new ArrayList<>();
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalPenalty = BigDecimal.ZERO;
        int paidCount = 0;
        
        for (LoanInstallment installment : installments) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) break;
            
            PaymentCalculation calculation = calculatePayment(installment, LocalDate.now());
            
            if (remainingAmount.compareTo(calculation.getAdjustedAmount()) >= 0) {
                // Pay installment
                installment.setPaid(true);
                installment.setPaidAmount(calculation.getAdjustedAmount());
                installment.setPaymentDate(LocalDate.now());
                installmentRepository.save(installment);
                
                // Update statistics
                remainingAmount = remainingAmount.subtract(calculation.getAdjustedAmount());
                paidCount++;
                
                if (calculation.getDaysDifference() > 0) {
                    totalDiscount = totalDiscount.add(
                        installment.getAmount().subtract(calculation.getAdjustedAmount())
                    );
                } else if (calculation.getDaysDifference() < 0) {
                    totalPenalty = totalPenalty.add(
                        calculation.getAdjustedAmount().subtract(installment.getAmount())
                    );
                }
                
                // Add payment details
                paymentDetails.add(createPaymentDetail(installment, calculation));
            }
        }
        
        return DetailedPaymentResponse.builder()
                .paidInstallments(paidCount)
                .totalPaidAmount(paymentAmount.subtract(remainingAmount))
                .totalDiscount(totalDiscount)
                .totalPenalty(totalPenalty)
                .isLoanFullyPaid(isAllInstallmentsPaid(installments))
                .paidInstallmentDetails(paymentDetails)
                .remainingDebt(calculateRemainingDebt(installments))
                .paymentDate(LocalDateTime.now())
                .paymentStatus(generatePaymentStatus(paidCount))
                .build();
    }
    
    private BigDecimal calculateEarlyPaymentDiscount(BigDecimal amount, long days) {
        return amount.multiply(new BigDecimal("0.001"))
                    .multiply(BigDecimal.valueOf(days));
    }
    
    private BigDecimal calculateLatePaymentPenalty(BigDecimal amount, long days) {
        return amount.multiply(new BigDecimal("0.001"))
                    .multiply(BigDecimal.valueOf(days));
    }
    
    private InstallmentPaymentDetail createPaymentDetail(LoanInstallment installment, PaymentCalculation calculation) {
        return InstallmentPaymentDetail.builder()
                .installmentId(installment.getId())
                .dueDate(installment.getDueDate())
                .originalAmount(installment.getAmount())
                .paidAmount(calculation.getAdjustedAmount())
                .wasLate(calculation.getDaysDifference() < 0)
                .lateFee(calculation.getDaysDifference() < 0 ? 
                    calculation.getAdjustedAmount().subtract(installment.getAmount()) : 
                    BigDecimal.ZERO)
                .earlyPaymentDiscount(calculation.getDaysDifference() > 0 ? 
                    installment.getAmount().subtract(calculation.getAdjustedAmount()) : 
                    BigDecimal.ZERO)
                .build();
    }
    
    private boolean isAllInstallmentsPaid(List<LoanInstallment> installments) {
        return installments.stream().allMatch(LoanInstallment::isPaid);
    }
    
    private BigDecimal calculateRemainingDebt(List<LoanInstallment> installments) {
        return installments.stream()
                .filter(i -> !i.isPaid())
                .map(LoanInstallment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private String generatePaymentStatus(int paidCount) {
        return paidCount > 0 ? 
            String.format("Successfully paid %d installment(s)", paidCount) : 
            "No installments were paid";
    }
} 