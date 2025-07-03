package br.com.king.flick_business.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import br.com.king.flick_business.dto.request.DespesaRequestDTO;
import br.com.king.flick_business.dto.response.DespesaResponseDTO;
import br.com.king.flick_business.service.DespesaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/despesas")
public class DespesaController {
  private final DespesaService despesaService;

  public DespesaController(DespesaService despesaService) {
    this.despesaService = despesaService;
  }

  // Criar Despesa
  @PostMapping
  public ResponseEntity<DespesaResponseDTO> criarDespesa(
      @Valid @RequestBody DespesaRequestDTO dto,
      UriComponentsBuilder uriBuilder) {
    DespesaResponseDTO despesaSalva = despesaService.salvarDespesa(dto);

    URI uri = uriBuilder.path("/api/despesas/{id}").buildAndExpand(despesaSalva.id()).toUri();

    return ResponseEntity.created(uri).body(despesaSalva);
  }

  // Listar Despesas
  @GetMapping
  public ResponseEntity<List<DespesaResponseDTO>> listarDespesas(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
      @RequestParam(name = "tipoDespesa", required = false) String tipoDespesa) {

    List<DespesaResponseDTO> despesas = despesaService.listarDespesas(inicio, fim, tipoDespesa);

    return ResponseEntity.ok(despesas);
  }

  // Total Despesas
  @GetMapping("/total")
  public ResponseEntity<BigDecimal> getTotalExpenses(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime begin,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
    BigDecimal total = despesaService.calcTotalExpensesPerPeriod(begin, end);
    return ResponseEntity.ok(total);
  }

  // Buscar Despesa por ID
  @GetMapping("/{id}")
  public ResponseEntity<DespesaResponseDTO> buscarDespesaPorId(@PathVariable Long id) {
    DespesaResponseDTO despesa = despesaService.buscarDespesaPorId(id);
    return ResponseEntity.ok(despesa);
  }

  // Atualizar Despesa
  @PutMapping("/{id}")
  public ResponseEntity<DespesaResponseDTO> atualizarDespesa(
      @PathVariable Long id,
      @Valid @RequestBody DespesaRequestDTO dto) {
    DespesaResponseDTO despesaAtualizada = despesaService.atualizarDespesa(id, dto);
    return ResponseEntity.ok(despesaAtualizada);
  }

  // Deletar Despesa
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletarDespesa(@PathVariable Long id) {
    despesaService.deletarDespesa(id);
    return ResponseEntity.noContent().build();
  }
}