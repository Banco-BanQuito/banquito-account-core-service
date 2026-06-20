package ec.edu.espe.banquito.accountcore.dto;

import ec.edu.espe.banquito.accountcore.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExternalTransferResponseDTO(
        String transactionId,
        String originAccountNumber,
        BigDecimal amount,
        BigDecimal commissionAmount,
        BigDecimal ivaAmount,
        BigDecimal totalDebited,
        BigDecimal remainingBalance,
        TransactionStatus status,
        LocalDate accountingDate
) {}
