package ec.edu.espe.banquito.accountcore.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TellerTransactionReqDTO(
        @NotNull(message = "El número de cuenta es obligatorio")
        String accountNumber,

        @NotNull(message = "El monto es obligatorio")
        BigDecimal amount,

        @NotNull(message = "El UUID de la transacción es obligatorio")
        String transactionUuid,

        @NotNull(message = "El ID del cajero es obligatorio")
        String tellerId
) {}