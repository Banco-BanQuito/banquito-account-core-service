package ec.edu.espe.banquito.core.accountcore.exception;

public class FavoriteAccountNotFoundException extends RuntimeException {

    public FavoriteAccountNotFoundException(Long customerId) {
        super("Favorite account not found for customer: " + customerId);
    }
}
