package ec.edu.espe.banquito.core.accountcore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ExternalTransferReqDTO(
        @NotNull(message = "Origin account ID is required")
        Long originAccountId,

        @NotBlank(message = "External bank code is required")
        String externalBankCode,

        @NotBlank(message = "External bank name is required")
        String externalBankName,

        @NotBlank(message = "External account number is required")
        String externalAccountNumber,

        @NotBlank(message = "Beneficiary name is required")
        String beneficiaryName,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Transaction UUID is required")
        String transactionUuid,

        String reference
) {}
