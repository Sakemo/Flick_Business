package br.com.king.flick_business.controller;

import org.springframework.http.ResponseEntity;

import br.com.king.flick_business.dto.VendaRequestDTO;
import br.com.king.flick_business.dto.VendaResponseDTO;
import br.com.king.flick_business.service.VendaService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {
  private final VendaService vendaService;

  public VendaController(VendaService vendaService) {
    this.vendaService = vendaService;
  }

  @PostMapping
  public ResponseEntity<VendaResponseDTO> registrarVenda(
      @Valid @RequestBody VendaRequestDTO requestDTO,
      UriComponentsBuilder uriBuilder) {
    VendaResponseDTO vendaSalva = vendaService.registrarVenda(requestDTO);
    URI uri = uriBuilder.path("/api/vendas/{id}").buildAndExpand(vendaSalva.id()).toUri();
    return ResponseEntity.created(uri).body(vendaSalva);
  }

  @GetMapping
  public ResponseEntity<List<VendaResponseDTO>> listarVendas() {
    List<VendaResponseDTO> vendas = vendaService.listarVendas();
    return ResponseEntity.ok(vendas);
  }

  @GetMapping("/{id}")
  public ResponseEntity<VendaResponseDTO> buscarVendaPorId(@PathVariable Long id) {
    VendaResponseDTO venda = vendaService.buscarVendaPorId(id);
    return ResponseEntity.ok(venda);
  }
}