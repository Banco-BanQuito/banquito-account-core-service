package ec.edu.espe.banquito.accountcore.enums;

public enum TransactionSubtypeCode {
    TELLER_DEPOSIT,
    TELLER_WITHDRAWAL,
    BATCH_CREDIT,
    CORPORATE_DEBIT,
    P2P_OUT,
    P2P_IN;

    public static TransactionSubtypeCode fromDatabaseValue(String value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "DEP", "TELLER_DEPOSIT" -> TELLER_DEPOSIT;
            case "RET", "TELLER_WITHDRAWAL" -> TELLER_WITHDRAWAL;
            case "BATCH_CREDIT" -> BATCH_CREDIT;
            case "DEB_CORP", "CORPORATE_DEBIT" -> CORPORATE_DEBIT;
            default -> TransactionSubtypeCode.valueOf(value);
        };
    }
}
