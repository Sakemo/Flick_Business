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

import br.com.king.flick_business.dto.CategoryDTO;
import br.com.king.flick_business.service.CategoryService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorys")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CategoryController {
  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @PostMapping
  public ResponseEntity<CategoryDTO> criarCategory(@Valid @RequestBody CategoryDTO dto,
      UriComponentsBuilder uriBuilder) {
    CategoryDTO categorySalva = categoryService.save(dto);
    URI uri = uriBuilder.path("/{id}").buildAndExpand(categorySalva.id()).toUri();
    return ResponseEntity.created(uri).body(categorySalva);
  }

  @GetMapping
  public ResponseEntity<List<CategoryDTO>> listCategorys() {
    return ResponseEntity.ok(categoryService.listTodas());
  }

  @GetMapping("/{id}")
  public ResponseEntity<CategoryDTO> buscarCategoryPorId(@PathVariable Long id) {
    return ResponseEntity.ok(categoryService.buscarDtoPorId(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<CategoryDTO> atualizarCategory(@PathVariable Long id,
      @Valid @RequestBody CategoryDTO dto) {
    CategoryDTO categoryAtualizada = categoryService.atualizar(id, dto);
    return ResponseEntity.ok(categoryAtualizada);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTodasCategorys(@PathVariable Long id) {
    categoryService.delete(id);
    return ResponseEntity.noContent().build();
  }
}