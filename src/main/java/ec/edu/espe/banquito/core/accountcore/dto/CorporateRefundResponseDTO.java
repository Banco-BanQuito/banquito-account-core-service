package ec.edu.espe.banquito.core.accountcore.dto;

import ec.edu.espe.banquito.core.accountcore.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CorporateRefundResponseDTO(
        String transactionUuid,
        BigDecimal refundedAmount,
        TransactionStatus status,
        LocalDate accountingDate
) {}