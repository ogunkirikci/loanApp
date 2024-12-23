package com.example.loanapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "loan_installment")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class LoanInstallment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal paidAmount;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate paymentDate;

    @Column(nullable = false)
    private boolean paid;

    public static LoanInstallment create(Loan loan, BigDecimal amount, LocalDate dueDate) {
        LoanInstallment installment = new LoanInstallment();
        installment.setLoan(loan);
        installment.setAmount(amount);
        installment.setPaidAmount(BigDecimal.ZERO);
        installment.setDueDate(dueDate);
        installment.setPaid(false);
        return installment;
    }
} 