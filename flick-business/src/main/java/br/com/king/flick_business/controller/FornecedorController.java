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

import br.com.king.flick_business.dto.FornecedorDTO;
import br.com.king.flick_business.service.FornecedorService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {
  private final FornecedorService fornecedorService;

  public FornecedorController(FornecedorService fornecedorService) {
    this.fornecedorService = fornecedorService;
  }

  @PostMapping
  public ResponseEntity<FornecedorDTO> criarFornecedor(@Valid @RequestBody FornecedorDTO dto,
      UriComponentsBuilder uriBuilder) {
    FornecedorDTO fornecedorSalvo = fornecedorService.salvar(dto);
    URI uri = uriBuilder.path("api/fornecedores/{id}").buildAndExpand(fornecedorSalvo.id()).toUri();
    return ResponseEntity.created(uri).body(fornecedorSalvo);
  }

  @GetMapping
  public ResponseEntity<List<FornecedorDTO>> listarFornecedores() {
    return ResponseEntity.ok(fornecedorService.listarTodos());
  }

  @GetMapping("/{id}")
  public ResponseEntity<FornecedorDTO> buscarFornecedorPorId(@PathVariable Long id) {
    return ResponseEntity.ok(fornecedorService.buscarDtoPorId(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<FornecedorDTO> atualizarFornecedor(@PathVariable Long id,
      @Valid @RequestBody FornecedorDTO dto) {
    FornecedorDTO fornecedorAtualizado = fornecedorService.atualizar(id, dto);
    return ResponseEntity.ok(fornecedorAtualizado);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletarFornecedor(@PathVariable Long id) {
    fornecedorService.deletar(id);
    return ResponseEntity.noContent().build();
  }
}