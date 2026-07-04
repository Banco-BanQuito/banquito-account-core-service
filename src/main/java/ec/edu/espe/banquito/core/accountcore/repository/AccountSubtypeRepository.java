package ec.edu.espe.banquito.core.accountcore.repository;

import ec.edu.espe.banquito.core.accountcore.model.AccountSubtype;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountSubtypeRepository extends JpaRepository<AccountSubtype, Integer> {
    Optional<AccountSubtype> findByCode(String code);
}
