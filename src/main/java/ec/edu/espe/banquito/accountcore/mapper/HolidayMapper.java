package ec.edu.espe.banquito.accountcore.mapper;

import ec.edu.espe.banquito.accountcore.dto.HolidayCheckResponseDTO;
import ec.edu.espe.banquito.accountcore.model.Holiday;

import java.time.LocalDate;

public class HolidayMapper {

    private HolidayMapper() {
    }

    public static HolidayCheckResponseDTO toResponse(LocalDate date, Holiday holiday) {
        return new HolidayCheckResponseDTO(
                date,
                true,
                holiday.getName(),
                Boolean.TRUE.equals(holiday.getIsWeekend())
        );
    }
}
