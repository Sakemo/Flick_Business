package br.com.king.flick_business.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.king.flick_business.dto.GeneralSettingsDTO;
import br.com.king.flick_business.service.GeneralSettingsService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/configuracoes")
public class GeneralSettingsController {
  private final GeneralSettingsService generalSettingsService;

  public GeneralSettingsController(GeneralSettingsService generalSettingsService) {
    this.generalSettingsService = generalSettingsService;
  }

  @GetMapping
  public ResponseEntity<GeneralSettingsDTO> searchConfiguracao() {
    GeneralSettingsDTO config = generalSettingsService.searchConfiguracao();
    return ResponseEntity.ok(config);
  }

  @PutMapping
  public ResponseEntity<GeneralSettingsDTO> updateConfiguracao(
      @Valid @RequestBody GeneralSettingsDTO dto) {
    GeneralSettingsDTO configAtualizada = generalSettingsService.saveOrUpdateConfiguracao(dto);
    return ResponseEntity.ok(configAtualizada);
  }
}
