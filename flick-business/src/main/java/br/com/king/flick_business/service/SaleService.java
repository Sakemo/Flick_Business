package br.com.king.flick_business.service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.king.flick_business.dto.response.TotalByPaymentMethodDTO;
import br.com.king.flick_business.dto.SaleRequestDTO;
import br.com.king.flick_business.dto.SaleResponseDTO;
import br.com.king.flick_business.dto.request.SaleItemRequestDTO;
import br.com.king.flick_business.dto.response.GroupsummaryDTO;
import br.com.king.flick_business.dto.response.PageResponse;
import br.com.king.flick_business.entity.Customer;
import br.com.king.flick_business.entity.GeneralSettings;
import br.com.king.flick_business.entity.SaleItem;
import br.com.king.flick_business.entity.Product;
import br.com.king.flick_business.entity.Sale;
import br.com.king.flick_business.enums.PaymentMethod;
import br.com.king.flick_business.exception.BusinessException;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.repository.CustomerRepository;
import br.com.king.flick_business.repository.ProductRepository;
import br.com.king.flick_business.repository.SaleRepository;
import br.com.king.flick_business.mapper.SaleMapper;

@Service
public class SaleService {
  // Repositórios e serviços necessários para operações de sale
  private final SaleRepository saleRepository;
  private final CustomerRepository customerRepository;
  private final ProductRepository productRepository;
  private final GeneralSettingsService configuracaoService;

  // Construtor para injeção de dependências
  public SaleService(SaleRepository saleRepository, CustomerRepository customerRepository,
      ProductRepository productRepository, GeneralSettingsService configuracaoService) {
    this.saleRepository = saleRepository;
    this.customerRepository = customerRepository;
    this.productRepository = productRepository;
    this.configuracaoService = configuracaoService;
  }

  /**
   * Registra uma nova sale, atualiza estoque dos products e saldo devedor do
   * customer (caso fiado).
   * 
   * @param requestDTO Dados da sale a ser registrada
   * @return SaleResponseDTO com os dados da sale registrada
   */
  @Transactional
  public SaleResponseDTO registrarSale(SaleRequestDTO requestDTO) {
    System.out
        .println("LOG: SaleService.registrarSale - Iniciando registro de sale. Dados recebidos: " + requestDTO);

    Sale novaSale = new Sale();
    novaSale.setPaymentMethod(requestDTO.paymentMethod());
    novaSale.setDescription(requestDTO.description());
    novaSale.setDateSale(ZonedDateTime.now());

    // Setando Customer
    Customer customer = null;
    if (requestDTO.idCustomer() != null) {
      System.out.println("LOG: SaleService.registrarSale - idCustomer fornecido: " + requestDTO.idCustomer()
          + ". Buscando customer...");
      customer = customerRepository.findById(requestDTO.idCustomer())
          .orElseThrow(() -> new RecursoNaoEncontrado("Customer não encontrado com ID: " + requestDTO.idCustomer()));

      if (!customer.getActive()) {
        System.out.println("LOG: SaleService.registrarSale - Customer selecionado está inactive." + customer.getName());
        // TODO: adicionar reativação rapida a partir desse momento com modal
        throw new RecursoNaoEncontrado("Não é possível registrar sale para customer inactive.");
      }
      novaSale.setCustomer(customer);
      System.out.println("LOG: SaleService.registrarSale - Customer associado a sale: " + customer.getName());
    }

    // Validação para sales a prazo (fiado)
    if (requestDTO.paymentMethod() == PaymentMethod.FIADO) {
      System.out.println("LOG: SaleService.registrarSale - Sale FIADO detectada.");
      if (customer == null) {
        // Se idCustomer não foi fornecido mas é FIADO
        System.out.println("LOG: SaleService.registrarSale - Falha - ID do Customer não informado para sale FIADO.");
        throw new RecursoNaoEncontrado("ID do Customer é obrigatório para sales FIADO.");
      }
      // Validação se o customer PODE comprar fiado
      if (customer.getCreditManagement() != null && !customer.getCreditManagement()) {
        System.out
            .println("LOG: SaleService.registrarSale - Customer não habilitado para fiado: " + customer.getName());
        throw new RecursoNaoEncontrado("Este customer não está habilitado para compras fiado.");
      }
      System.out.println("LOG: SaleService.registrarSale - Customer validado para sale FIADO: " + customer.getName());
    }

    BigDecimal totalValueCalculado = BigDecimal.ZERO;
    List<Product> productsParaUpdateEstoque = new ArrayList<>();

    // Processa cada item da sale, valida estoque e calcula value total
    for (SaleItemRequestDTO itemDTO : requestDTO.items()) {
      System.out.println("LOG: SaleService.registrarSale - Processando item da sale: " + itemDTO);
      Product product = productRepository.findById(itemDTO.idProduct())
          .orElseThrow(() -> {
            System.out
                .println("LOG: SaleService.registrarSale - Product não encontrado com ID: " + itemDTO.idProduct());
            return new RecursoNaoEncontrado("Product não encontrado com ID: " + itemDTO.idProduct());
          });

      if (!product.isActive()) {
        System.out.println("LOG: SaleService.registrarSale - Product inactive: " + product.getName());
        throw new RecursoNaoEncontrado("Product inactive: " + product.getName());
      }

      BigDecimal quantidade = itemDTO.quantidade();
      BigDecimal priceUnitarioAtual = product.getSalePrice();

      if (priceUnitarioAtual == null || priceUnitarioAtual.compareTo(BigDecimal.ZERO) <= 0) {
        System.out.println("LOG: SaleService.registrarSale - Preço inválido para product: " + product.getName());
        throw new RecursoNaoEncontrado("Preço inválido ou não definido para o product: " + product.getName());
      }

      SaleItem saleItem = SaleItem.builder()
          .product(product)
          .quantidade(quantidade)
          .priceUnitarioSale(priceUnitarioAtual)
          .build();

      novaSale.adicionarItem(saleItem);

      totalValueCalculado = totalValueCalculado.add(saleItem.getTotalValueItem());
      System.out.println("LOG: SaleService.registrarSale - Item adicionado. Product: " + product.getName()
          + ", Quantidade: " + quantidade
          + ", Preço: " + priceUnitarioAtual + ", Value total acumulado: " + totalValueCalculado);

      // Atualiza estoque do product
      BigDecimal estoqueAtual = product.getStockQuantity();
      if (estoqueAtual != null && estoqueAtual.compareTo(BigDecimal.ZERO) > 0) {
        if (estoqueAtual.compareTo(quantidade) < 0) {
          System.out.println("LOG: SaleService.registrarSale - Estoque insuficiente para product: "
              + product.getName() + ". Em estoque: "
              + estoqueAtual + ", Solicitado: " + quantidade);
          throw new RecursoNaoEncontrado("Estoque insuficiente para o product: " + product.getName() +
              ". Em estoque: " + estoqueAtual + ", Solicitado: " + quantidade);
        }
        product.setStockQuantity(estoqueAtual.subtract(quantidade));
        productsParaUpdateEstoque.add(product);
        System.out.println("LOG: SaleService.registrarSale - Estoque updated para product: " + product.getName()
            + ". Novo estoque: "
            + product.getStockQuantity());
      } else {
        System.out.println("LOG: SaleService.registrarSale - Product sem estoque controlado ou estoque zerado: "
            + product.getName());
      }
    }

    novaSale.setTotalValue(totalValueCalculado);
    System.out.println("LOG: SaleService.registrarSale - Value total da sale calculado: " + totalValueCalculado);

    Sale saleSalva = saleRepository.save(novaSale);
    System.out.println("LOG: SaleService.registrarSale - Sale salva com ID: " + saleSalva.getId());

    // Salva atualização de estoque dos products
    if (!productsParaUpdateEstoque.isEmpty()) {
      productRepository.saveAll(productsParaUpdateEstoque);
      System.out.println("LOG: SaleService.registrarSale - Estoque dos products updated no banco de dados.");
    }

    // Atualiza saldo devedor do customer em caso de sale fiado
    if (saleSalva.getPaymentMethod() == PaymentMethod.FIADO && customer != null) {
      System.out
          .println("LOG: SaleService.registrarSale - Iniciando atualização de saldo devedor para customer FIADO: "
              + customer.getName());
      BigDecimal novoDebitBalance = customer.getDebitBalance().add(saleSalva.getTotalValue());

      // Busca configurações globais para cálculo de prazo e juros
      Optional<GeneralSettings> configOpt = configuracaoService.searchEntityConfiguracao();
      Integer prazoGlobal = configOpt.map(GeneralSettings::getPrazoPagamentoFiado).orElse(null);
      BigDecimal taxaGlobal = configOpt.map(GeneralSettings::getTaxaJurosAtraso).orElse(null);

      System.out.println("LOG: SaleService.registrarSale - Configurações globais - Prazo: " + prazoGlobal
          + ", Taxa de Juros: " + taxaGlobal);
      System.out.println(
          "LOG: SaleService.registrarSale - Saldo devedor atual: " + customer.getDebitBalance() + ", Value da sale: "
              + saleSalva.getTotalValue() + ", Novo saldo: " + novoDebitBalance);

      if (customer.getCreditLimit() != null && novoDebitBalance.compareTo(customer.getCreditLimit()) > 0) {
        System.out.println(
            "LOG: SaleService.registrarSale - Limite fiado excedido para customer: " + customer.getName() + ". Limite: "
                + customer.getCreditLimit() + ", Saldo após sale: " + novoDebitBalance);
        throw new BusinessException("Limite fiado excedido para o customer: " + customer.getName() +
            ". Limite: " + customer.getCreditLimit() + ", Saldo após sale: " + novoDebitBalance);
      }
      customer.setDebitBalance(novoDebitBalance);
      customer.setDateLastPurchaseOnCredit(saleSalva.getDateSale());
      System.out.println("LOG: SaleService.registrarSale - Customer devedor updated: " + customer);
      customerRepository.save(customer);
      System.out.println("LOG: SaleService.registrarSale - Customer saved com novo saldo devedor.");
    }

    System.out.println(
        "LOG: SaleService.registrarSale - Registro de sale finalizado com sucesso. Sale ID: " + saleSalva.getId());
    return new SaleResponseDTO(saleSalva);
  }

  /**
   * Lista sales com filtros opcionais por date, customer e forma de pagamento.
   * 
   * @param start               Date/hora inicial do filtro
   * @param end                 Date/hora final do filtro
   * @param customerId          ID do customer para filtro (opcional)
   * @param paymentMethodString Forma de pagamento para filtro (opcional)
   * @return Lista de SaleResponseDTO correspondentes aos filtros
   */

  @Transactional(readOnly = true)
  public PageResponse<SaleResponseDTO> listSales(
      ZonedDateTime start,
      ZonedDateTime end,
      Long customerId,
      String paymentMethodString,
      Long productId,
      String orderBy,
      int page,
      int size) {
    System.out.println("LOG: SaleService.listSales - start: " + start + ", end: " + end
        + ", customerId: " + customerId + ", paymentMethodString: " + paymentMethodString);

    // Extrai lógica de conversão e dates padrão
    PaymentMethod paymentMethodFiltro = SaleMapper.parsePaymentMethod(paymentMethodString);
    ZonedDateTime[] range = SaleMapper.buildDateRange(start, end);

    Sort sort = SaleMapper.buildSort(orderBy);
    Pageable pageable = PageRequest.of(page, size, sort);

    System.out.println("SERVICE: SaleService.listSales - Usando sort: " + sort);
    System.out.println("SERVICE: Query - Start: " + range[0] + ", End: " + range[1]
        + ", CustomerId: " + customerId + ", PaymentMethod: " + paymentMethodFiltro
        + ", Product: " + productId);

    Page<Sale> salesPage = saleRepository.findSalesComFiltros(
        range[0],
        range[1],
        customerId,
        paymentMethodFiltro,
        productId,
        pageable);

    Page<SaleResponseDTO> dtoPage = salesPage.map(SaleResponseDTO::new);
    System.out.println("LOG: SaleService.listSales - Sales encontradas: " + salesPage.getTotalElements());
    return new PageResponse<>(dtoPage);
  }

  public static PaymentMethod parsePaymentMethod(String paymentMethodString) {
    if (paymentMethodString == null || paymentMethodString.isEmpty()) {
      return null;
    }

    try {
      return PaymentMethod.valueOf(paymentMethodString.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @Transactional(readOnly = true)
  public BigDecimal calcularTotalBrutoSales(ZonedDateTime start, ZonedDateTime end,
      Long customerId, String paymentMethodString, Long productId) {
    PaymentMethod paymentMethodFiltro = parsePaymentMethod(paymentMethodString);
    ZonedDateTime inicioQuery = (start != null) ? start : ZonedDateTime.parse("1900-01-01T00:00:00Z");
    ZonedDateTime fimQuery = (end != null) ? end : ZonedDateTime.parse("9999-12-31T23:59:59Z");

    return saleRepository.sumTotalValueComFiltros(
        inicioQuery, fimQuery, customerId, paymentMethodFiltro, productId);
  }

  @Transactional(readOnly = true)
  public List<TotalByPaymentMethodDTO> calcularTotaisByPaymentMethod(
      ZonedDateTime start, ZonedDateTime end) {
    ZonedDateTime inicioQuery = (start != null) ? start : ZonedDateTime.parse("1900-01-01T00:00:00Z");
    ZonedDateTime fimQuery = (end != null) ? end : ZonedDateTime.parse("9999-12-31T23:59:59Z");

    List<Object[]> results = saleRepository.sumTotalValueGroupByPaymentMethodBetween(inicioQuery, fimQuery);

    return results.stream()
        .map(res -> new TotalByPaymentMethodDTO((PaymentMethod) res[0], (BigDecimal) res[1]))
        .collect(Collectors.toList());
  }

  /**
   * Busca uma sale pelo ID, incluindo items e customer relacionados.
   *
   * @param id Identificador da sale
   * @return SaleResponseDTO com os dados da sale encontrada
   */
  @Transactional(readOnly = true)
  public SaleResponseDTO searchSaleById(Long id) {
    Sale sale = ((Optional<Sale>) saleRepository.findByIdComItemsECustomer(id))
        .orElseThrow(() -> new RecursoNaoEncontrado("Sale não encontrada com ID: " + id));
    return new SaleResponseDTO(sale);
  }

  /**
   * Lógica para search a sale, estornar estoque, estornar saldo fiado e deletar
   * a sale.
   * 
   * @param id Identificador da sale
   */
  @Transactional
  public void deletarSaleFisicamente(Long saleId) {
    System.out
        .println("DELETE_START: SaleService.deletarSaleFisicamente - Buscando sale para deleção... - " + saleId);

    Sale saleParaDeletar = saleRepository.findByIdComItemsECustomer(saleId)
        .orElseThrow(() -> {
          System.err.println("ERROR: SaleService.deletarSaleFisicamente - Sale não encontrada: " + saleId);
          return new RecursoNaoEncontrado("Sale não encontrada: " + saleId);
        });

    System.out.println("DELETE_LOG_01: SaleService.deletarSaleFisicamente - Sale " + saleId + " encontrada.");
    System.out.println("DELETE_LOG_02: SaleService.deletarSaleFisicamente - Estornando items ao estoque... ");
    List<Product> productsParaUpdateEstoque = new ArrayList<>();
    if (saleParaDeletar.getItems() != null) {
      for (SaleItem item : saleParaDeletar.getItems()) {
        Product productDoItem = item.getProduct();
        if (productDoItem != null) {
          Product productParaEstornar = productRepository.findById(productDoItem.getId())
              .orElseThrow(() -> new RecursoNaoEncontrado(
                  "Produo associado ao item da sale não encontrado: ID " + productDoItem.getId()));
          if (productParaEstornar.getStockQuantity() != null) {
            System.out.println("DELETE_LOG_03: SaleService.deletarSaleFisicamente - Processando estoque do item "
                + productParaEstornar.getName() + " de ID " + productParaEstornar.getId()
                + ". Quantia a ser processada: " + item.getQuantidade());
            BigDecimal novoEstoque = productParaEstornar.getStockQuantity().add(item.getQuantidade());
            productParaEstornar.setStockQuantity(novoEstoque);
            productsParaUpdateEstoque.add(productParaEstornar);
            System.out.println("DELETE_LOG_04: SaleService.deletarSaleFisicamente. SUCESSO ao PROCESSAR ESTOQUE de "
                + productDoItem.getName() + ". Novo Estoque: " + novoEstoque);
          } else {
            System.out.println("DELETE_LOG_03_S/ESTOQUE - SaleService.deletarSaleFisicamente - Product "
                + productParaEstornar.getName() + "de ID: " + productParaEstornar.getId()
                + ", não possui controle de estoque. SUCESSO AO PROCESSAR.");
          }
        }
      }
    }
    if (!productsParaUpdateEstoque.isEmpty()) {
      productRepository.saveAll(productsParaUpdateEstoque);
      System.out.println("DELETE_LOG: SaleService.deletarSaleFisicamente. SUCESSO ao PROCESSAR ESTOQUE");
    }

    if (saleParaDeletar.getPaymentMethod() == PaymentMethod.FIADO && saleParaDeletar.getCustomer() != null) {
      Customer customerDaSale = customerRepository.findById(saleParaDeletar.getCustomer().getId())
          .orElse(null);
      System.out
          .println("DELETE_LOG_FIADO: SaleService.deletarSaleFisicamente. Estornando crédito de fiado ao customer"
              + customerDaSale.getName() + "...");
      BigDecimal saldoAtual = customerDaSale.getDebitBalance();
      BigDecimal valueSale = saleParaDeletar.getTotalValue();

      if (customerDaSale != null) {
        customerDaSale.setDebitBalance(saldoAtual.subtract(valueSale));

        System.out.println("DELETE_LOG_FIADO: SUCESSO ao estornar R$" + valueSale + " para o customer "
            + customerDaSale.getName() + " totalizando no SALDO ATUAL: R$" + customerDaSale.getDebitBalance());

        if (customerDaSale.getDebitBalance().compareTo(BigDecimal.ZERO) <= 0
            && saleParaDeletar.getDateSale().equals(customerDaSale.getDateLastPurchaseOnCredit())) {
          System.out.println(
              "LOG: SaleService.deletarSaleFisicamente - Saldo do customer zerado ou negactive. Date da última compra fiado pode precisar de reavaliação.");
        }
        customerRepository.save(customerDaSale);
        System.out.println("LOG: SaleService.deletarSaleFisicamente - Saldo devedor do customer ID " +
            customerDaSale.getId() + " estornado. Novo saldo: " + customerDaSale.getDebitBalance());
      }
    }
    System.out.println("DELETE_LOG: Deletando Sale...");
    saleRepository.delete(saleParaDeletar);
    System.out
        .println("LOG: SaleService.deletarSaleFisicamente - Sale ID " + saleId + " deletada permanentemente.");
    System.out.println("DELETE_LOG: VENDA DELETADA COM SUCESSO");
  }

  public List<GroupsummaryDTO> getSalessummary(
      ZonedDateTime start, ZonedDateTime end, Long customerId,
      String paymentMethodString, Long productId, String groupBy) {
    System.out.println("LOG: SaleService.getSalessummary - Iniciando resumo de sales com filtros: "
        + "Start: " + start + ", End: " + end + ", CustomerId: " + customerId
        + ", PaymentMethod: " + paymentMethodString + ", ProductId: " + productId
        + ", GroupBy: " + groupBy);

    PaymentMethod paymentMethodFiltro = SaleMapper.parsePaymentMethod(paymentMethodString);
    ZonedDateTime incioQuery = (start != null) ? start
        : ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, java.time.ZoneId.systemDefault());
    ZonedDateTime fimQuery = (end != null) ? end
        : ZonedDateTime.of(9999, 12, 31, 23, 59, 59, 0, java.time.ZoneId.systemDefault());

    if ("dateSale".equalsIgnoreCase(groupBy)) {
      List<Object[]> results = saleRepository.sumTotalGroupByDay(incioQuery, fimQuery, customerId, paymentMethodFiltro,
          productId);
      return results.stream()
          .map(res -> new GroupsummaryDTO((String) res[0], (String) res[0], (BigDecimal) res[1]))
          .collect(Collectors.toList());
    }

    if ("customer.name".equalsIgnoreCase(groupBy)) {
      List<Object[]> results = saleRepository.sumTotalGroupByCustomer(incioQuery, fimQuery, customerId,
          paymentMethodFiltro, productId);
      return results.stream()
          .map(res -> new GroupsummaryDTO(String.valueOf(res[0]), (String) res[1], (BigDecimal) res[2]))
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

}
