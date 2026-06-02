package ec.edu.espe.banquito.accountcore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ACCOUNT_SUBTYPE")
@Getter
@Setter
public class AccountSubtype {
    @Id
    @Column(name = "SUBTYPE_CODE", nullable = false, length = 10)
    private String code;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "INTEREST_RATE", nullable = false)
    private Double interestRate;
}
