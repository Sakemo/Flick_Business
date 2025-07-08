package br.com.king.flick_business.controller;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.king.flick_business.dto.DashboardSummaryDTO;
import br.com.king.flick_business.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
  private final DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/summary")
  public ResponseEntity<DashboardSummaryDTO> getDashboardSummary(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    ZonedDateTime start = startDate.atStartOfDay(java.time.ZoneId.systemDefault());
    ZonedDateTime end = endDate.atTime(LocalTime.MAX).atZone(java.time.ZoneId.systemDefault());

    DashboardSummaryDTO summary = dashboardService.getDashboardSummary(start, end);
    return ResponseEntity.ok(summary);
  }

}
