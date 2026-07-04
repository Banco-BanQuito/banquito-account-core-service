package ec.edu.espe.banquito.core.accountcore.dto;

public record AccountSubtypeResponseDTO(
    Integer id,
    String name,
    String description,
    String type
) {}
