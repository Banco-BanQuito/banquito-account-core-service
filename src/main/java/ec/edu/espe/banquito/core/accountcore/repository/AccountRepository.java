package ec.edu.espe.banquito.core.accountcore.repository;

import ec.edu.espe.banquito.core.accountcore.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerIdOrderByAccountNumberAsc(Long customerId);

    Optional<Account> findFirstByCustomerIdAndFavoriteTrueOrderByAccountNumberAsc(Long customerId);

    @EntityGraph(attributePaths = "accountSubtype")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findWithLockByAccountNumber(String accountNumber);

    @EntityGraph(attributePaths = "accountSubtype")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findWithLockById(Long id);

    Optional<Account> findTopByAccountNumberStartingWithOrderByAccountNumberDesc(String prefix);
}
