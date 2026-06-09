package ec.edu.espe.banquito.accountcore.dto;

import java.time.LocalDate;

public record HolidayCheckResponseDTO(
        LocalDate date,
        boolean holiday,
        String name,
        boolean weekend
) {}
