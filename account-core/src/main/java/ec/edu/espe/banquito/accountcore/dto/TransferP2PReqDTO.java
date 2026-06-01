package ec.edu.espe.banquito.accountcore.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransferP2PReqDTO(
        @NotNull(message = "El número de cuenta de origen es obligatorio")
        String sourceAccountNumber,

        @NotNull(message = "El número de cuenta de destino es obligatorio")
        String destinationAccountNumber,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a cero")
        BigDecimal amount,

        @NotNull(message = "El UUID de la transacción es obligatorio")
        String transactionUuid
) {}