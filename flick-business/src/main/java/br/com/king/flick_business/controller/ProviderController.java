package br.com.king.flick_business.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.king.flick_business.dto.ProviderDTO;
import br.com.king.flick_business.service.ProviderService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {
  private final ProviderService providerService;

  public ProviderController(ProviderService providerService) {
    this.providerService = providerService;
  }

  @PostMapping
  public ResponseEntity<ProviderDTO> criarProvider(@Valid @RequestBody ProviderDTO dto,
      UriComponentsBuilder uriBuilder) {
    ProviderDTO providerSalvo = providerService.salvar(dto);
    URI uri = uriBuilder.path("api/providers/{id}").buildAndExpand(providerSalvo.id()).toUri();
    return ResponseEntity.created(uri).body(providerSalvo);
  }

  @GetMapping
  public ResponseEntity<List<ProviderDTO>> listProvideres() {
    return ResponseEntity.ok(providerService.listTodos());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProviderDTO> buscarProviderPorId(@PathVariable Long id) {
    return ResponseEntity.ok(providerService.buscarDtoPorId(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProviderDTO> atualizarProvider(@PathVariable Long id,
      @Valid @RequestBody ProviderDTO dto) {
    ProviderDTO providerAtualizado = providerService.atualizar(id, dto);
    return ResponseEntity.ok(providerAtualizado);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProvider(@PathVariable Long id) {
    providerService.delete(id);
    return ResponseEntity.noContent().build();
  }
}