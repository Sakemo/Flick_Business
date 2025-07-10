package br.com.king.flick_business.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

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

import br.com.king.flick_business.dto.response.TotalByPaymentMethodDTO;
import br.com.king.flick_business.dto.SaleRequestDTO;
import br.com.king.flick_business.dto.SaleResponseDTO;
import br.com.king.flick_business.dto.response.PageResponse;
import br.com.king.flick_business.service.SaleService;
import br.com.king.flick_business.dto.response.GroupsummaryDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sales")
public class SaleController {
  // Serviço responsável pelas operações de sale
  private final SaleService saleService;

  // Construtor para injeção de dependência do serviço de sale
  public SaleController(SaleService saleService) {
    this.saleService = saleService;
    System.out.println("LOG: SaleController.<init> - SaleController inicializado com SaleService");
  }

  /**
   * Endpoint para registrar uma nova sale.
   * 
   * @param requestDTO Dados da sale a ser registrada.
   * @param uriBuilder Utilitário para construir a URI do recurso criado.
   * @return ResponseEntity com os dados da sale registrada.
   */
  @PostMapping
  public ResponseEntity<SaleResponseDTO> registrarSale(
      @Valid @RequestBody SaleRequestDTO requestDTO,
      UriComponentsBuilder uriBuilder) {
    System.out.println("LOG: SaleController.registrarSale - Iniciando registro de sale: " + requestDTO);
    SaleResponseDTO saleSalva = saleService.registrarSale(requestDTO);
    System.out.println("LOG: SaleController.registrarSale - Sale registrada com sucesso: " + saleSalva);
    URI uri = uriBuilder.path("/api/sales/{id}").buildAndExpand(saleSalva.id()).toUri();
    System.out.println("LOG: SaleController.registrarSale - URI do recurso criado: " + uri);
    return ResponseEntity.created(uri).body(saleSalva);
  }

  /**
   * Endpoint para list sales com filtros opcionais.
   * 
   * @param start         Date/hora inicial do filtro.
   * @param end           Date/hora final do filtro.
   * @param customerId    ID do customer para filtrar.
   * @param paymentMethod Forma de pagamento para filtrar.
   * @param orderBy       Ordenação de items
   * @return Lista de sales encontradas.
   */
  @GetMapping
  public ResponseEntity<PageResponse<SaleResponseDTO>> listSales(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end,
      @RequestParam(required = false) Long customerId,
      @RequestParam(required = false) String paymentMethod,
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) String orderBy,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "8") int size) {
    System.out.println("LOG: SaleController.listSales - Listando sales com filtros - start: " + start + ", end: "
        + end + ", customerId: " + customerId + ", paymentMethod: " + paymentMethod);
    System.out.println("PAGINATION: SaleController.listSales - " + page + " : " + size);
    PageResponse<SaleResponseDTO> paginatedResponse = saleService.listSales(
        start,
        end,
        customerId,
        paymentMethod,
        productId,
        orderBy,
        page,
        size);
    System.out.println(
        "LOG: SaleController.listSales - Quantidade de sales encontradas: " + paginatedResponse.getSize());
    return ResponseEntity.ok(paginatedResponse);
  }

  @GetMapping("/summary-by-group")
  public ResponseEntity<List<GroupsummaryDTO>> getSalessummary(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end,
      @RequestParam(required = false) Long customerId,
      @RequestParam(required = false) String paymentMethod,
      @RequestParam(required = false) Long productId,
      @RequestParam(required = true) String groupBy) {
    List<GroupsummaryDTO> summary = saleService.getSalessummary(start, end, customerId, paymentMethod, productId,
        groupBy);
    return ResponseEntity.ok(summary);
  }

  @GetMapping("/total-bruto")
  public ResponseEntity<BigDecimal> getTotalBrutoSales(
      @RequestParam(required = false) ZonedDateTime start,
      @RequestParam(required = false) ZonedDateTime end,
      @RequestParam(required = false) Long customerId,
      @RequestParam(required = false) String paymentMethod,
      @RequestParam(required = false) Long productId) {
    BigDecimal total = saleService.calcularTotalBrutoSales(start, end, customerId, paymentMethod, productId);
    return ResponseEntity.ok(total);
  }

  @GetMapping("total-por-pagamento")
  public ResponseEntity<List<TotalByPaymentMethodDTO>> getTotaisByPaymentMethod(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end) {
    List<TotalByPaymentMethodDTO> totais = saleService.calcularTotaisByPaymentMethod(start, end);
    return ResponseEntity.ok(totais);
  }

  /**
   * Endpoint para search uma sale pelo ID.
   * 
   * @param id Identificador da sale.
   * @return Dados da sale encontrada.
   */
  @GetMapping("/{id}")
  public ResponseEntity<SaleResponseDTO> searchSaleById(@PathVariable Long id) {
    System.out.println("LOG: SaleController.searchSaleById - Buscando sale por ID: " + id);
    SaleResponseDTO sale = saleService.searchSaleById(id);
    System.out.println("LOG: SaleController.searchSaleById - Sale encontrada: " + sale);
    return ResponseEntity.ok(sale);
  }

  @DeleteMapping("/{id}/permanente")
  public ResponseEntity<Void> deletarSaleFisicamente(@PathVariable Long id) {
    System.out.println("LOG: SaleController.deletarSaleFisicamente - Recebida requisição de deleção de ID: " + id);
    saleService.deletarSaleFisicamente(id);
    return ResponseEntity.noContent().build();
  }
}
