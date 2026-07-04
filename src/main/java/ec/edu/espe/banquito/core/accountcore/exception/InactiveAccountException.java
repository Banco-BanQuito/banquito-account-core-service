package ec.edu.espe.banquito.core.accountcore.exception;

public class InactiveAccountException extends RuntimeException {

    public InactiveAccountException(String accountNumber) {
        super("Account is not active: " + accountNumber);
    }
}
