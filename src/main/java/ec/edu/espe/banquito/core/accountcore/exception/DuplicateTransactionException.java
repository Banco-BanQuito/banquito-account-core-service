package ec.edu.espe.banquito.core.accountcore.exception;

public class DuplicateTransactionException extends RuntimeException {

    public DuplicateTransactionException(String transactionUuid) {
        super("Duplicate transaction: " + transactionUuid);
    }
}
