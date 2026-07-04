package ec.edu.espe.banquito.core.accountcore.dto;

public record HealthResponseDTO(
        String status,
        String service,
        String version
) {}
