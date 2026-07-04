package ec.edu.espe.banquito.core.accountcore.service;

import ec.edu.espe.banquito.core.accountcore.dto.HolidayCheckResponseDTO;
import ec.edu.espe.banquito.core.accountcore.mapper.HolidayMapper;
import ec.edu.espe.banquito.core.accountcore.repository.HolidayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class CalendarQueryService {

    private final HolidayRepository holidayRepository;

    public CalendarQueryService(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @Transactional(readOnly = true)
    public HolidayCheckResponseDTO checkHoliday(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date is required");
        }

        return holidayRepository.findById(date)
                .map(holiday -> HolidayMapper.toResponse(date, holiday))
                .orElseGet(() -> new HolidayCheckResponseDTO(date, false, null, false));
    }
}
