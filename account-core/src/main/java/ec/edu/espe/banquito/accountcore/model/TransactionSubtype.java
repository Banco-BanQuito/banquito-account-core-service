package ec.edu.espe.banquito.accountcore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TRANSACTION_SUBTYPE")
@Getter
@Setter
public class TransactionSubtype {
    @Id
    @Column(name = "SUBTYPE_CODE", nullable = false, length = 10)
    private String code;

    @Column(name = "DESCRIPTION", nullable = false, length = 100)
    private String description;

    @Column(name = "TYPE", nullable = false, length = 10)
    private String type;
}
