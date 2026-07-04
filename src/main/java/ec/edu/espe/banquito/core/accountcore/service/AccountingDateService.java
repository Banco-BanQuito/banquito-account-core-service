package ec.edu.espe.banquito.core.accountcore.service;

import ec.edu.espe.banquito.core.accountcore.repository.HolidayRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class AccountingDateService {

    private static final ZoneId BANK_ZONE = ZoneId.of("America/Guayaquil");

    private final HolidayRepository holidayRepository;
    private final LocalTime cutOffTime;

    public AccountingDateService(HolidayRepository holidayRepository,
                                 @Value("${cut.off.time:20:00}") String cutOffTimeRaw) {
        this.holidayRepository = holidayRepository;
        this.cutOffTime = LocalTime.parse(cutOffTimeRaw);
    }

    public LocalDate resolveAccountingDate() {
        ZonedDateTime now = ZonedDateTime.now(BANK_ZONE);
        LocalDate candidate = now.toLocalDate();

        if (now.toLocalTime().isAfter(cutOffTime)) {
            candidate = candidate.plusDays(1);
        }

        return nextBusinessDay(candidate);
    }

    public LocalTime getCutOffTime() {
        return cutOffTime;
    }

    public boolean isPastCutOff() {
        return ZonedDateTime.now(BANK_ZONE).toLocalTime().isAfter(cutOffTime);
    }

    LocalDate nextBusinessDay(LocalDate date) {
        while (isNonBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private boolean isNonBusinessDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (DayOfWeek.SATURDAY.equals(dow) || DayOfWeek.SUNDAY.equals(dow)) {
            return true;
        }
        return holidayRepository.existsByHolidayDate(date);
    }
}
