package ec.edu.espe.banquito.core.accountcore.service;

import ec.edu.espe.banquito.core.accountcore.dto.FavoriteAccountResponseDTO;
import ec.edu.espe.banquito.core.accountcore.enums.AccountStatus;
import ec.edu.espe.banquito.core.accountcore.exception.FavoriteAccountNotFoundException;
import ec.edu.espe.banquito.core.accountcore.model.Account;
import ec.edu.espe.banquito.core.accountcore.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountQueryServiceTests {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountQueryService accountQueryService;

    @Test
    void shouldReturnFavoriteAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("2200000002");
        account.setCustomerId(2L);
        account.setStatus(AccountStatus.ACTIVA);
        account.setAvailableBalance(new BigDecimal("100.00"));
        account.setAccountingBalance(new BigDecimal("100.00"));
        account.setFavorite(true);
        when(accountRepository.findFirstByCustomerIdAndFavoriteTrueOrderByAccountNumberAsc(2L))
                .thenReturn(Optional.of(account));

        FavoriteAccountResponseDTO response = accountQueryService.getFavoriteAccount(2L);

        assertEquals(1L, response.accountId());
        assertEquals("2200000002", response.accountNumber());
        assertEquals(AccountStatus.ACTIVA, response.status());
        assertTrue(response.favorite());
    }

    @Test
    void shouldFailWhenCustomerHasNoFavoriteAccount() {
        when(accountRepository.findFirstByCustomerIdAndFavoriteTrueOrderByAccountNumberAsc(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                FavoriteAccountNotFoundException.class,
                () -> accountQueryService.getFavoriteAccount(2L)
        );
    }

    @Test
    void shouldRejectInvalidCustomerId() {
        assertThrows(IllegalArgumentException.class, () -> accountQueryService.getFavoriteAccount(0L));
    }
}
