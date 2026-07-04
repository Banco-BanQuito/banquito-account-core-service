package ec.edu.espe.banquito.core.accountcore.repository;

import ec.edu.espe.banquito.core.accountcore.model.TransactionSubtype;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionSubtypeRepository extends JpaRepository<TransactionSubtype, Integer> {
    Optional<TransactionSubtype> findByCode(String code);
}
