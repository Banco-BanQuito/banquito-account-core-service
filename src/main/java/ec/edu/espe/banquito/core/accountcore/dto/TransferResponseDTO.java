package ec.edu.espe.banquito.core.accountcore.dto;

import ec.edu.espe.banquito.core.accountcore.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferResponseDTO(
        String transactionId,
        BigDecimal originNewBalance,
        String destinationAccountNumber,
        String destinationHolderName,
        TransactionStatus status,
        LocalDate accountingDate
) {}
