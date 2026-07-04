package ec.edu.espe.banquito.core.accountcore.dto;

import ec.edu.espe.banquito.core.accountcore.enums.AccountingOperationType;
import ec.edu.espe.banquito.core.accountcore.enums.AccountingProductType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountingOperationReqDTO(
        String operationUuid,
        AccountingOperationType operationType,
        AccountingProductType sourceAccountProductType,
        AccountingProductType destinationAccountProductType,
        BigDecimal amount,
        BigDecimal commissionAmount,
        String reference,
        LocalDate accountingDate,
        BigDecimal ivaAmount
) {
    public AccountingOperationReqDTO(
            String operationUuid,
            AccountingOperationType operationType,
            AccountingProductType sourceAccountProductType,
            AccountingProductType destinationAccountProductType,
            BigDecimal amount,
            BigDecimal commissionAmount,
            String reference,
            LocalDate accountingDate) {
        this(operationUuid, operationType, sourceAccountProductType, destinationAccountProductType, amount, commissionAmount, reference, accountingDate, BigDecimal.ZERO);
    }

    public AccountingOperationReqDTO(
            String operationUuid,
            AccountingOperationType operationType,
            AccountingProductType accountProductType,
            BigDecimal amount,
            BigDecimal commissionAmount,
            String reference,
            LocalDate accountingDate) {
        this(operationUuid, operationType, accountProductType, null, amount, commissionAmount, reference, accountingDate, BigDecimal.ZERO);
    }
}
