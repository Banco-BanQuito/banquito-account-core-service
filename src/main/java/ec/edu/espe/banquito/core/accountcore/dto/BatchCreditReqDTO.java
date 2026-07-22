package ec.edu.espe.banquito.core.accountcore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record BatchCreditReqDTO(
        @NotNull(message = "Batch ID is required")
        String batchId,

        String originAccountNumber,

        @NotEmpty(message = "Credits are required")
        List<@Valid CreditItemDTO> credits
) {
    public record CreditItemDTO(
            @NotBlank(message = "Account number is required")
            String accountNumber,

            @NotNull(message = "Amount is required")
            @Positive(message = "Amount must be greater than zero")
            BigDecimal amount,

            String reference,

            @NotNull(message = "Transaction UUID is required")
            String transactionUuid
    ) {}
}
