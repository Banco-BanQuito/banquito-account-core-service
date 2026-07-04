package ec.edu.espe.banquito.core.accountcore.domain;

import ec.edu.espe.banquito.core.accountcore.exception.InsufficientBalanceException;
import ec.edu.espe.banquito.core.accountcore.model.Account;

import java.math.BigDecimal;

public class AccountDomainService {

    private AccountDomainService() {
    }

    public static void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(account.getAccountNumber());
        }
    }

    public static void debit(Account account, BigDecimal amount) {
        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
        account.setAccountingBalance(account.getAccountingBalance().subtract(amount));
    }

    public static void credit(Account account, BigDecimal amount) {
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        account.setAccountingBalance(account.getAccountingBalance().add(amount));
    }
}
