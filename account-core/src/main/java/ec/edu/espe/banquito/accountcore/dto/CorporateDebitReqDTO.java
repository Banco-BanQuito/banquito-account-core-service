package ec.edu.espe.banquito.accountcore.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CorporateDebitReqDTO(
        @NotNull(message = "El ID de la cuenta es obligatorio")
        String accountId,

        @NotNull(message = "El monto total es obligatorio")
        BigDecimal totalAmount,

        @NotNull(message = "El monto de la comisión es obligatorio")
        BigDecimal commissionAmount,

        @NotNull(message = "El UUID de la transacción es obligatorio")
        String transactionUuid
) {}