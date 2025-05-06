package br.com.king.flick_business.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
  public ResponseEntity<DashboardSummaryDTO> getDashboardSumarry(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

    LocalDateTime inicio = dataInicio.atStartOfDay();
    LocalDateTime fim = dataFim.atTime(LocalTime.MAX);

    DashboardSummaryDTO summary = dashboardService.getDashboardSummary(inicio, fim);
    return ResponseEntity.ok(summary);
  }

}
