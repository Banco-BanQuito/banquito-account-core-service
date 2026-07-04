package ec.edu.espe.banquito.core.accountcore.mapper;

import ec.edu.espe.banquito.core.accountcore.dto.AccountDetailResponseDTO;
import ec.edu.espe.banquito.core.accountcore.dto.AccountSummaryResponseDTO;
import ec.edu.espe.banquito.core.accountcore.dto.FavoriteAccountResponseDTO;
import ec.edu.espe.banquito.core.accountcore.model.Account;

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
        String subtypeName = resolveSubtypeName(account);

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

    private static String resolveSubtypeName(Account account) {
        if (account.getAccountSubtype() == null) {
            return null;
        }
        String description = account.getAccountSubtype().getDescription();
        return description != null ? description : account.getAccountSubtype().getName();
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
