package ec.edu.espe.banquito.accountcore.repository;

import ec.edu.espe.banquito.accountcore.model.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {
    boolean existsByTransactionUuidAndTransactionDateAfter(String uuid, LocalDateTime date);
}