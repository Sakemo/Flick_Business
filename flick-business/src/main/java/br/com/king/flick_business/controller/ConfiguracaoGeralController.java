package br.com.king.flick_business.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.king.flick_business.dto.ConfiguracaoGeralDTO;
import br.com.king.flick_business.service.ConfiguracaoGeralService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/configuracoes")
public class ConfiguracaoGeralController {
  private final ConfiguracaoGeralService configuracaoGeralService;

  public ConfiguracaoGeralController(ConfiguracaoGeralService configuracaoGeralService) {
    this.configuracaoGeralService = configuracaoGeralService;
  }

  @GetMapping
  public ResponseEntity<ConfiguracaoGeralDTO> buscarConfiguracao() {
    ConfiguracaoGeralDTO config = configuracaoGeralService.buscarConfiguracao();
    return ResponseEntity.ok(config);
  }

  @PutMapping
  public ResponseEntity<ConfiguracaoGeralDTO> atualizarConfiguracao(
      @Valid @RequestBody ConfiguracaoGeralDTO dto) {
    ConfiguracaoGeralDTO configAtualizada = configuracaoGeralService.salvarOuAtualizarConfiguracao(dto);
    return ResponseEntity.ok(configAtualizada);
  }
}
