package ec.edu.espe.banquito.core.accountcore.dto;

import ec.edu.espe.banquito.core.accountcore.enums.AccountStatus;

import java.math.BigDecimal;

public record BalanceResponseDTO(
        Long accountId,
        String accountNumber,
        BigDecimal availableBalance,
        BigDecimal accountingBalance,
        AccountStatus status,
        String currency
) {}
