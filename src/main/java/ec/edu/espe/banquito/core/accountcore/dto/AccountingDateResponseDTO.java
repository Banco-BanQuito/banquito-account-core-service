package ec.edu.espe.banquito.core.accountcore.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AccountingDateResponseDTO(
        LocalDate accountingDate,
        LocalTime cutOffTime,
        boolean isPastCutOff
) {}
