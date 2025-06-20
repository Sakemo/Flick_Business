package br.com.king.flick_business.controller;

import java.net.URI;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.king.flick_business.dto.PageResponse;
import br.com.king.flick_business.dto.VendaRequestDTO;
import br.com.king.flick_business.dto.VendaResponseDTO;
import br.com.king.flick_business.service.VendaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {
  // Serviço responsável pelas operações de venda
  private final VendaService vendaService;

  // Construtor para injeção de dependência do serviço de venda
  public VendaController(VendaService vendaService) {
    this.vendaService = vendaService;
    System.out.println("LOG: VendaController.<init> - VendaController inicializado com VendaService");
  }

  /**
   * Endpoint para registrar uma nova venda.
   * 
   * @param requestDTO Dados da venda a ser registrada.
   * @param uriBuilder Utilitário para construir a URI do recurso criado.
   * @return ResponseEntity com os dados da venda registrada.
   */
  @PostMapping
  public ResponseEntity<VendaResponseDTO> registrarVenda(
      @Valid @RequestBody VendaRequestDTO requestDTO,
      UriComponentsBuilder uriBuilder) {
    System.out.println("LOG: VendaController.registrarVenda - Iniciando registro de venda: " + requestDTO);
    VendaResponseDTO vendaSalva = vendaService.registrarVenda(requestDTO);
    System.out.println("LOG: VendaController.registrarVenda - Venda registrada com sucesso: " + vendaSalva);
    URI uri = uriBuilder.path("/api/vendas/{id}").buildAndExpand(vendaSalva.id()).toUri();
    System.out.println("LOG: VendaController.registrarVenda - URI do recurso criado: " + uri);
    return ResponseEntity.created(uri).body(vendaSalva);
  }

  /**
   * Endpoint para listar vendas com filtros opcionais.
   * 
   * @param inicio         Data/hora inicial do filtro.
   * @param fim            Data/hora final do filtro.
   * @param clienteId      ID do cliente para filtrar.
   * @param formaPagamento Forma de pagamento para filtrar.
   * @param orderBy        Ordenação de itens
   * @return Lista de vendas encontradas.
   */
  @GetMapping
  public ResponseEntity<PageResponse<VendaResponseDTO>> listarVendas(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
      @RequestParam(required = false) Long clienteId,
      @RequestParam(required = false) String formaPagamento,
      @RequestParam(required = false) Long produtoId,
      @RequestParam(required = false) String orderBy,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "8") int size) {
    System.out.println("LOG: VendaController.listarVendas - Listando vendas com filtros - inicio: " + inicio + ", fim: "
        + fim + ", clienteId: " + clienteId + ", formaPagamento: " + formaPagamento);
    System.out.println("PAGINATION: VendaController.listarVendas - " + page + " : " + size);
    PageResponse<VendaResponseDTO> paginatedResponse = vendaService.listarVendas(
        inicio,
        fim,
        clienteId,
        formaPagamento,
        produtoId,
        orderBy,
        page,
        size);
    System.out.println(
        "LOG: VendaController.listarVendas - Quantidade de vendas encontradas: " + paginatedResponse.getSize());
    return ResponseEntity.ok(paginatedResponse);
  }

  /**
   * Endpoint para buscar uma venda pelo ID.
   * 
   * @param id Identificador da venda.
   * @return Dados da venda encontrada.
   */
  @GetMapping("/{id}")
  public ResponseEntity<VendaResponseDTO> buscarVendaPorId(@PathVariable Long id) {
    System.out.println("LOG: VendaController.buscarVendaPorId - Buscando venda por ID: " + id);
    VendaResponseDTO venda = vendaService.buscarVendaPorId(id);
    System.out.println("LOG: VendaController.buscarVendaPorId - Venda encontrada: " + venda);
    return ResponseEntity.ok(venda);
  }

  @DeleteMapping("/{id}/permanente")
  public ResponseEntity<Void> deletarVendaFisicamente(@PathVariable Long id) {
    System.out.println("LOG: VendaController.deletarVendaFisicamente - Recebida requisição de deleção de ID: " + id);
    vendaService.deletarVendaFisicamente(id);
    return ResponseEntity.noContent().build();
  }
}