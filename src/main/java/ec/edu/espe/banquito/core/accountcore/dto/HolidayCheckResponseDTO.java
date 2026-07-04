package ec.edu.espe.banquito.core.accountcore.dto;

import java.time.LocalDate;

public record HolidayCheckResponseDTO(
        LocalDate date,
        boolean holiday,
        String name,
        boolean weekend
) {}
