package ec.edu.espe.banquito.core.accountcore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CorporateRefundReqDTO(
        @NotBlank(message = "Account number is required")
        String accountNumber,
        @NotNull(message = "Refund amount is required")
        @Positive(message = "Refund amount must be greater than zero")
        BigDecimal refundAmount,
        @NotBlank(message = "Batch ID is required")
        String batchId,
        @NotBlank(message = "Transaction UUID is required")
        String transactionUuid
) {}