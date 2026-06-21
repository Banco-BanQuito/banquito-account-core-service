package ec.edu.espe.banquito.accountcore.service;

import ec.edu.espe.banquito.accountcore.repository.HolidayRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountingDateServiceTests {

    private final HolidayRepository holidayRepository = mock(HolidayRepository.class);
    private final AccountingDateService service = new AccountingDateService(holidayRepository, "20:00");

    @Test
    void skipsWeekendDays() {
        LocalDate saturday = LocalDate.of(2026, Month.JUNE, 20);

        assertEquals(LocalDate.of(2026, Month.JUNE, 22), service.nextBusinessDay(saturday));
    }

    @Test
    void skipsRegisteredHoliday() {
        LocalDate holiday = LocalDate.of(2026, Month.JUNE, 22);
        when(holidayRepository.existsByHolidayDate(holiday)).thenReturn(true);

        assertEquals(LocalDate.of(2026, Month.JUNE, 23), service.nextBusinessDay(holiday));
    }

    @Test
    void keepsBusinessDay() {
        LocalDate businessDay = LocalDate.of(2026, Month.JUNE, 23);

        assertEquals(businessDay, service.nextBusinessDay(businessDay));
    }
}
