package ec.edu.espe.banquito.core.accountcore.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String accountNumber) {
        super("Insufficient balance in account: " + accountNumber);
    }
}
