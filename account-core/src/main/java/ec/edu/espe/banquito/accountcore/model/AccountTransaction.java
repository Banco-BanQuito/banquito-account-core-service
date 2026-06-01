package ec.edu.espe.banquito.accountcore.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_transaction")
@Data
@NoArgsConstructor
public class AccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "transaction_uuid", nullable = false, length = 36)
    private String transactionUuid;

    @Column(name = "transaction_type", nullable = false, length = 15)
    private String transactionType;

    @Column(name = "transaction_subtype", length = 30)
    private String transactionSubtype;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "accounting_date", nullable = false)
    private LocalDate accountingDate;
}