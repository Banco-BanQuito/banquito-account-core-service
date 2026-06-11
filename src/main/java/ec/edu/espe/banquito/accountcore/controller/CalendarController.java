package ec.edu.espe.banquito.accountcore.controller;

import ec.edu.espe.banquito.accountcore.dto.HolidayCheckResponseDTO;
import ec.edu.espe.banquito.accountcore.service.CalendarQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v2/calendar")
@Tag(name = "Calendar", description = "Core banking business calendar queries.")
public class CalendarController {

    private final CalendarQueryService calendarQueryService;

    public CalendarController(CalendarQueryService calendarQueryService) {
        this.calendarQueryService = calendarQueryService;
    }

    @GetMapping("/holidays/check")
    @Operation(summary = "Check holiday", description = "Indicates whether a date is registered as a holiday in the Core banking calendar.")
    @ApiResponse(responseCode = "200", description = "Calendar status returned",
            content = @Content(schema = @Schema(implementation = HolidayCheckResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid or missing date")
    public ResponseEntity<HolidayCheckResponseDTO> checkHoliday(
            @Parameter(description = "Date in ISO-8601 format", example = "2026-12-25", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(calendarQueryService.checkHoliday(date));
    }
}
