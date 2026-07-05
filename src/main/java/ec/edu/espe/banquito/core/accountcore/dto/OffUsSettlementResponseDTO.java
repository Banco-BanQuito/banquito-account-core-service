package ec.edu.espe.banquito.core.accountcore.dto;

import ec.edu.espe.banquito.core.accountcore.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OffUsSettlementResponseDTO(
        String transactionUuid,
        BigDecimal settledAmount,
        TransactionStatus status,
        LocalDate accountingDate
) {}
