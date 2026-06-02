package ec.edu.espe.banquito.accountcore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CORE_USER")
@Getter
@Setter
public class CoreUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "USER_ID", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(name = "USERNAME", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "ROLE", nullable = false, length = 20)
    private String role;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive;
}
