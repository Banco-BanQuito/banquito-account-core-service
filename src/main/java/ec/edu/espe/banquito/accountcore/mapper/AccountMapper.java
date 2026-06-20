package ec.edu.espe.banquito.accountcore.mapper;

import ec.edu.espe.banquito.accountcore.dto.AccountDetailResponseDTO;
import ec.edu.espe.banquito.accountcore.dto.AccountSummaryResponseDTO;
import ec.edu.espe.banquito.accountcore.dto.FavoriteAccountResponseDTO;
import ec.edu.espe.banquito.accountcore.model.Account;

public class AccountMapper {

    private static final String CURRENCY = "USD";

    private AccountMapper() {
    }

    public static AccountDetailResponseDTO toDetailResponse(Account account, String customerFullName, String branchName) {
        return new AccountDetailResponseDTO(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomerId(),
                customerFullName,
                account.getAccountSubtype().getDescription() != null
                        ? account.getAccountSubtype().getDescription()
                        : account.getAccountSubtype().getName(),
                account.getBranchId(),
                branchName,
                account.getAvailableBalance(),
                account.getAccountingBalance(),
                account.getStatus(),
                account.getOpeningDate()
        );
    }

    public static AccountSummaryResponseDTO toSummaryResponse(Account account, String branchName) {
        String subtypeName = account.getAccountSubtype() != null
                ? (account.getAccountSubtype().getDescription() != null
                        ? account.getAccountSubtype().getDescription()
                        : account.getAccountSubtype().getName())
                : null;

        return new AccountSummaryResponseDTO(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomerId(),
                account.getStatus(),
                account.getAvailableBalance(),
                account.getAccountingBalance(),
                CURRENCY,
                account.getBranchId(),
                branchName,
                subtypeName
        );
    }

    public static FavoriteAccountResponseDTO toFavoriteResponse(Account account) {
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
