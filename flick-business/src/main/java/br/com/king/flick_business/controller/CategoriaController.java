package br.com.king.flick_business.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.king.flick_business.dto.CategoriaDTO;
import br.com.king.flick_business.service.CategoriaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CategoriaController {
  private final CategoriaService categoriaService;

  public CategoriaController(CategoriaService categoriaService) {
    this.categoriaService = categoriaService;
  }

  @PostMapping
  public ResponseEntity<CategoriaDTO> criarCategoria(@Valid @RequestBody CategoriaDTO dto,
      UriComponentsBuilder uriBuilder) {
    CategoriaDTO categoriaSalva = categoriaService.salvar(dto);
    URI uri = uriBuilder.path("/{id}").buildAndExpand(categoriaSalva.id()).toUri();
    return ResponseEntity.created(uri).body(categoriaSalva);
  }

  @GetMapping
  public ResponseEntity<List<CategoriaDTO>> listarCategorias() {
    return ResponseEntity.ok(categoriaService.listarTodas());
  }

  @GetMapping("/{id}")
  public ResponseEntity<CategoriaDTO> buscarCategoriaPorId(@PathVariable Long id) {
    return ResponseEntity.ok(categoriaService.buscarDtoPorId(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<CategoriaDTO> atualizarCategoria(@PathVariable Long id,
      @Valid @RequestBody CategoriaDTO dto) {
    CategoriaDTO categoriaAtualizada = categoriaService.atualizar(id, dto);
    return ResponseEntity.ok(categoriaAtualizada);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletarTodasCategorias(@PathVariable Long id) {
    categoriaService.deletar(id);
    return ResponseEntity.noContent().build();
  }
}