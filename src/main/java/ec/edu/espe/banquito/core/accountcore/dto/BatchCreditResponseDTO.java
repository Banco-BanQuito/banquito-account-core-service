package ec.edu.espe.banquito.core.accountcore.dto;

import java.util.List;

public record BatchCreditResponseDTO(
        String batchId,
        int processed,
        int failed,
        List<BatchCreditResultDTO> results
) {
    public record BatchCreditResultDTO(
            String accountNumber,
            String status,
            String transactionId
    ) {}
}
