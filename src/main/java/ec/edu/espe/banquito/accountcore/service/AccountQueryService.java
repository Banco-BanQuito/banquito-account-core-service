package ec.edu.espe.banquito.accountcore.service;

import ec.edu.espe.banquito.accountcore.dto.FavoriteAccountResponseDTO;
import ec.edu.espe.banquito.accountcore.exception.FavoriteAccountNotFoundException;
import ec.edu.espe.banquito.accountcore.model.Account;
import ec.edu.espe.banquito.accountcore.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountQueryService {

    private static final String CURRENCY = "USD";

    private final AccountRepository accountRepository;

    public AccountQueryService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public FavoriteAccountResponseDTO getFavoriteAccount(Long customerId) {
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Customer ID must be greater than zero");
        }

        Account account = accountRepository.findFirstByCustomerIdAndFavoriteTrueOrderByAccountNumberAsc(customerId)
                .orElseThrow(() -> new FavoriteAccountNotFoundException(customerId));

        return new FavoriteAccountResponseDTO(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomerId(),
                account.getStatus(),
                account.getAvailableBalance(),
                account.getAccountingBalance(),
                CURRENCY,
                Boolean.TRUE.equals(account.getFavorite())
        );
    }
}
