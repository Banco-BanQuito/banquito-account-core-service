package ec.edu.espe.banquito.accountcore.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "INSTITUTIONAL_ACCOUNT")
@Getter
@Setter
public class InstitutionalAccount {
    @Id
    @Column(name = "ACCOUNT_NUMBER", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "ACCOUNT_NAME", nullable = false, length = 100)
    private String accountName;

    @Column(name = "BALANCE", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
}
