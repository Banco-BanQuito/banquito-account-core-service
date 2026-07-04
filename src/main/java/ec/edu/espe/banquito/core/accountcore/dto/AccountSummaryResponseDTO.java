package ec.edu.espe.banquito.core.accountcore.dto;

import ec.edu.espe.banquito.core.accountcore.enums.AccountStatus;

import java.math.BigDecimal;

public record AccountSummaryResponseDTO(
        Long accountId,
        String accountNumber,
        Long customerId,
        AccountStatus status,
        BigDecimal availableBalance,
        BigDecimal accountingBalance,
        String currency,
        Integer branchId,
        String branchName,
        String accountSubtypeName
) {}
