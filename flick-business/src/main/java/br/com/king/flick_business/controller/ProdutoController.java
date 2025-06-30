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

import br.com.king.flick_business.dto.ProdutoRequestDTO;
import br.com.king.flick_business.dto.ProdutoResponseDTO;
import br.com.king.flick_business.service.ProdutoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProdutoController {
  private final ProdutoService produtoService;

  public ProdutoController(ProdutoService produtoService) {
    this.produtoService = produtoService;
  }

  // Criar Produto
  @PostMapping
  public ResponseEntity<ProdutoResponseDTO> criarProduto(@Valid @RequestBody ProdutoRequestDTO requestDTO,
      UriComponentsBuilder uriBuilder) {
    ProdutoResponseDTO produtoSalvoDTO = produtoService.salvar(requestDTO);
    URI uri = uriBuilder.path("/{id}").buildAndExpand(produtoSalvoDTO.id()).toUri();
    return ResponseEntity.created(uri).body(produtoSalvoDTO);
  }

  // Listar Produtos
  @GetMapping
  public ResponseEntity<List<ProdutoResponseDTO>> listarProdutos(
      @RequestParam(required = false) String nome,
      @RequestParam(required = false) Long categoriaId

  ) {
    System.out
        .println("BACKEND CONTROLLER: Recebidos parâmetros - nome: [" + nome + "], categoriaId: [" + categoriaId + "]");
    // Passe o categoriaId para o serviço
    List<ProdutoResponseDTO> produtos = produtoService.listProducts(nome, categoriaId);
    return ResponseEntity.ok(produtos);
  }

  // Buscar Produto por ID
  @GetMapping("/{id}")
  public ResponseEntity<ProdutoResponseDTO> buscarProdutoPorId(@PathVariable Long id) {
    ProdutoResponseDTO produto = produtoService.buscarPorId(id);
    return ResponseEntity.ok(produto);
  }

  // Atualizar Produto
  @PutMapping("/{id}")
  public ResponseEntity<ProdutoResponseDTO> atualizarProduto(@PathVariable Long id,
      @Valid @RequestBody ProdutoRequestDTO requestDTO) {
    ProdutoResponseDTO produtoAtualizado = produtoService.atualizar(id, requestDTO);
    return ResponseEntity.ok(produtoAtualizado);
  }

  // Deletar Produto
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletarProduto(@PathVariable Long id) {
    produtoService.deletarLogicamente(id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/permanente")
  public ResponseEntity<Void> deletarProdutoFisicamente(@PathVariable Long id) {
    produtoService.deletarFisicamente(id);
    return ResponseEntity.noContent().build();
  }
}