package ec.edu.espe.banquito.core.accountcore.repository;

import ec.edu.espe.banquito.core.accountcore.model.InstitutionalAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionalAccountRepository extends JpaRepository<InstitutionalAccount, Integer> {
}
