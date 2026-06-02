package ec.edu.espe.banquito.accountcore.enums;

public enum AccountType {
    SAVINGS,
    CHECKING;

    public static AccountType fromDatabaseValue(String value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "AHORROS", "SAVINGS" -> SAVINGS;
            case "CORRIENTE", "CHECKING" -> CHECKING;
            default -> AccountType.valueOf(value);
        };
    }
}
