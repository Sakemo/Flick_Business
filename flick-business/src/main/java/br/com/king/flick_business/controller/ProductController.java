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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.king.flick_business.dto.ProductRequestDTO;
import br.com.king.flick_business.dto.ProductResponseDTO;
import br.com.king.flick_business.service.ProductService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProductController {
  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  // Criar Product
  @PostMapping
  public ResponseEntity<ProductResponseDTO> criarProduct(@Valid @RequestBody ProductRequestDTO requestDTO,
      UriComponentsBuilder uriBuilder) {
    ProductResponseDTO productSalvoDTO = productService.salvar(requestDTO);
    URI uri = uriBuilder.path("/{id}").buildAndExpand(productSalvoDTO.id()).toUri();
    return ResponseEntity.created(uri).body(productSalvoDTO);
  }

  // List Products
  @GetMapping
  public ResponseEntity<List<ProductResponseDTO>> listProducts(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String orderBy) {
    System.out
        .println("BACKEND CONTROLLER: Recebidos parâmetros - name: [" + name + "], categoryId: [" + categoryId + "]");
    // Passe o categoryId para o serviço
    List<ProductResponseDTO> products = productService.listProducts(name, categoryId, orderBy);
    return ResponseEntity.ok(products);
  }

  // Buscar Product por ID
  @GetMapping("/{id}")
  public ResponseEntity<ProductResponseDTO> buscarProductPorId(@PathVariable Long id) {
    ProductResponseDTO product = productService.buscarPorId(id);
    return ResponseEntity.ok(product);
  }

  // Atualizar Product
  @PutMapping("/{id}")
  public ResponseEntity<ProductResponseDTO> atualizarProduct(@PathVariable Long id,
      @Valid @RequestBody ProductRequestDTO requestDTO) {
    ProductResponseDTO productAtualizado = productService.atualizar(id, requestDTO);
    return ResponseEntity.ok(productAtualizado);
  }

  // Copiar Product
  @PostMapping("/{id}/copiar")
  public ResponseEntity<ProductResponseDTO> copiarProduct(@PathVariable Long id, UriComponentsBuilder uriBuilder) {
    ProductResponseDTO productCopiadoDTO = productService.copiarProduct(id);
    URI uri = uriBuilder.path("/api/products/{id}").buildAndExpand(productCopiadoDTO.id()).toUri();
    return ResponseEntity.created(uri).body(productCopiadoDTO);
  }

  // Delete Product
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    productService.deleteLogicamente(id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/permanente")
  public ResponseEntity<Void> deleteProductFisicamente(@PathVariable Long id) {
    productService.deleteFisicamente(id);
    return ResponseEntity.noContent().build();
  }
}