package ec.edu.espe.banquito.core.accountcore.dto;

import ec.edu.espe.banquito.core.accountcore.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OperationResponseDTO(
        String transactionId,
        LocalDate accountingDate,
        BigDecimal newBalance,
        TransactionStatus status,
        LocalDateTime timestamp
) {}
