package ec.edu.espe.banquito.core.accountcore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OffUsSettlementReqDTO(
        @NotBlank(message = "Batch ID is required")
        String batchId,
        @NotNull(message = "Settlement amount is required")
        @Positive(message = "Settlement amount must be greater than zero")
        BigDecimal amount,
        @NotBlank(message = "Transaction UUID is required")
        String transactionUuid
) {}
