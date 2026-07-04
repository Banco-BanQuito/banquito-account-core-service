package ec.edu.espe.banquito.core.accountcore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountingOperationResponseDTO(
        Long entryId,
        String entryUuid,
        String status,
        String validationResult,
        LocalDateTime registeredAt,
        BigDecimal commissionAmount,
        BigDecimal ivaAmount,
        BigDecimal totalDebited
) {}
