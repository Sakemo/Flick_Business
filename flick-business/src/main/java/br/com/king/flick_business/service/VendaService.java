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

import br.com.king.flick_business.dto.response.TotalPorFormaPagamentoDTO;
import br.com.king.flick_business.dto.VendaRequestDTO;
import br.com.king.flick_business.dto.VendaResponseDTO;
import br.com.king.flick_business.dto.request.ItemVendaRequestDTO;
import br.com.king.flick_business.dto.response.GroupsummaryDTO;
import br.com.king.flick_business.dto.response.PageResponse;
import br.com.king.flick_business.entity.Cliente;
import br.com.king.flick_business.entity.ConfiguracaoGeral;
import br.com.king.flick_business.entity.ItemVenda;
import br.com.king.flick_business.entity.Product;
import br.com.king.flick_business.entity.Venda;
import br.com.king.flick_business.enums.FormaPagamento;
import br.com.king.flick_business.exception.BusinessException;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.repository.ClienteRepository;
import br.com.king.flick_business.repository.ProductRepository;
import br.com.king.flick_business.repository.VendaRepository;
import br.com.king.flick_business.mapper.VendaMapper;

@Service
public class VendaService {
  // Repositórios e serviços necessários para operações de venda
  private final VendaRepository vendaRepository;
  private final ClienteRepository clienteRepository;
  private final ProductRepository productRepository;
  private final ConfiguracaoGeralService configuracaoService;

  // Construtor para injeção de dependências
  public VendaService(VendaRepository vendaRepository, ClienteRepository clienteRepository,
      ProductRepository productRepository, ConfiguracaoGeralService configuracaoService) {
    this.vendaRepository = vendaRepository;
    this.clienteRepository = clienteRepository;
    this.productRepository = productRepository;
    this.configuracaoService = configuracaoService;
  }

  /**
   * Registra uma nova venda, atualiza estoque dos products e saldo devedor do
   * cliente (caso fiado).
   * 
   * @param requestDTO Dados da venda a ser registrada
   * @return VendaResponseDTO com os dados da venda registrada
   */
  @Transactional
  public VendaResponseDTO registrarVenda(VendaRequestDTO requestDTO) {
    System.out
        .println("LOG: VendaService.registrarVenda - Iniciando registro de venda. Dados recebidos: " + requestDTO);

    Venda novaVenda = new Venda();
    novaVenda.setFormaPagamento(requestDTO.formaPagamento());
    novaVenda.setObservacoes(requestDTO.observacoes());
    novaVenda.setDataVenda(ZonedDateTime.now());

    // Setando Cliente
    Cliente cliente = null;
    if (requestDTO.idCliente() != null) {
      System.out.println("LOG: VendaService.registrarVenda - idCliente fornecido: " + requestDTO.idCliente()
          + ". Buscando cliente...");
      cliente = clienteRepository.findById(requestDTO.idCliente())
          .orElseThrow(() -> new RecursoNaoEncontrado("Cliente não encontrado com ID: " + requestDTO.idCliente()));

      if (!cliente.getActive()) {
        System.out.println("LOG: VendaService.registrarVenda - Cliente selecionado está inactive." + cliente.getName());
        // TODO: adicionar reativação rapida a partir desse momento com modal
        throw new RecursoNaoEncontrado("Não é possível registrar venda para cliente inactive.");
      }
      novaVenda.setCliente(cliente);
      System.out.println("LOG: VendaService.registrarVenda - Cliente associado a venda: " + cliente.getName());
    }

    // Validação para vendas a prazo (fiado)
    if (requestDTO.formaPagamento() == FormaPagamento.FIADO) {
      System.out.println("LOG: VendaService.registrarVenda - Venda FIADO detectada.");
      if (cliente == null) {
        // Se idCliente não foi fornecido mas é FIADO
        System.out.println("LOG: VendaService.registrarVenda - Falha - ID do Cliente não informado para venda FIADO.");
        throw new RecursoNaoEncontrado("ID do Cliente é obrigatório para vendas FIADO.");
      }
      // Validação se o cliente PODE comprar fiado
      if (cliente.getControleFiado() != null && !cliente.getControleFiado()) {
        System.out
            .println("LOG: VendaService.registrarVenda - Cliente não habilitado para fiado: " + cliente.getName());
        throw new RecursoNaoEncontrado("Este cliente não está habilitado para compras fiado.");
      }
      System.out.println("LOG: VendaService.registrarVenda - Cliente validado para venda FIADO: " + cliente.getName());
    }

    BigDecimal valueTotalCalculado = BigDecimal.ZERO;
    List<Product> productsParaAtualizarEstoque = new ArrayList<>();

    // Processa cada item da venda, valida estoque e calcula value total
    for (ItemVendaRequestDTO itemDTO : requestDTO.itens()) {
      System.out.println("LOG: VendaService.registrarVenda - Processando item da venda: " + itemDTO);
      Product product = productRepository.findById(itemDTO.idProduct())
          .orElseThrow(() -> {
            System.out
                .println("LOG: VendaService.registrarVenda - Product não encontrado com ID: " + itemDTO.idProduct());
            return new RecursoNaoEncontrado("Product não encontrado com ID: " + itemDTO.idProduct());
          });

      if (!product.isActive()) {
        System.out.println("LOG: VendaService.registrarVenda - Product inactive: " + product.getName());
        throw new RecursoNaoEncontrado("Product inactive: " + product.getName());
      }

      BigDecimal quantidade = itemDTO.quantidade();
      BigDecimal precoUnitarioAtual = product.getSalePrice();

      if (precoUnitarioAtual == null || precoUnitarioAtual.compareTo(BigDecimal.ZERO) <= 0) {
        System.out.println("LOG: VendaService.registrarVenda - Preço inválido para product: " + product.getName());
        throw new RecursoNaoEncontrado("Preço inválido ou não definido para o product: " + product.getName());
      }

      ItemVenda itemVenda = ItemVenda.builder()
          .product(product)
          .quantidade(quantidade)
          .precoUnitarioVenda(precoUnitarioAtual)
          .build();

      novaVenda.adicionarItem(itemVenda);

      valueTotalCalculado = valueTotalCalculado.add(itemVenda.getValueTotalItem());
      System.out.println("LOG: VendaService.registrarVenda - Item adicionado. Product: " + product.getName()
          + ", Quantidade: " + quantidade
          + ", Preço: " + precoUnitarioAtual + ", Value total acumulado: " + valueTotalCalculado);

      // Atualiza estoque do product
      BigDecimal estoqueAtual = product.getStockQuantity();
      if (estoqueAtual != null && estoqueAtual.compareTo(BigDecimal.ZERO) > 0) {
        if (estoqueAtual.compareTo(quantidade) < 0) {
          System.out.println("LOG: VendaService.registrarVenda - Estoque insuficiente para product: "
              + product.getName() + ". Em estoque: "
              + estoqueAtual + ", Solicitado: " + quantidade);
          throw new RecursoNaoEncontrado("Estoque insuficiente para o product: " + product.getName() +
              ". Em estoque: " + estoqueAtual + ", Solicitado: " + quantidade);
        }
        product.setStockQuantity(estoqueAtual.subtract(quantidade));
        productsParaAtualizarEstoque.add(product);
        System.out.println("LOG: VendaService.registrarVenda - Estoque atualizado para product: " + product.getName()
            + ". Novo estoque: "
            + product.getStockQuantity());
      } else {
        System.out.println("LOG: VendaService.registrarVenda - Product sem estoque controlado ou estoque zerado: "
            + product.getName());
      }
    }

    novaVenda.setValueTotal(valueTotalCalculado);
    System.out.println("LOG: VendaService.registrarVenda - Value total da venda calculado: " + valueTotalCalculado);

    Venda vendaSalva = vendaRepository.save(novaVenda);
    System.out.println("LOG: VendaService.registrarVenda - Venda salva com ID: " + vendaSalva.getId());

    // Salva atualização de estoque dos products
    if (!productsParaAtualizarEstoque.isEmpty()) {
      productRepository.saveAll(productsParaAtualizarEstoque);
      System.out.println("LOG: VendaService.registrarVenda - Estoque dos products atualizado no banco de dados.");
    }

    // Atualiza saldo devedor do cliente em caso de venda fiado
    if (vendaSalva.getFormaPagamento() == FormaPagamento.FIADO && cliente != null) {
      System.out
          .println("LOG: VendaService.registrarVenda - Iniciando atualização de saldo devedor para cliente FIADO: "
              + cliente.getName());
      BigDecimal novoSaldoDevedor = cliente.getSaldoDevedor().add(vendaSalva.getValueTotal());

      // Busca configurações globais para cálculo de prazo e juros
      Optional<ConfiguracaoGeral> configOpt = configuracaoService.buscarEntidadeConfiguracao();
      Integer prazoGlobal = configOpt.map(ConfiguracaoGeral::getPrazoPagamentoFiado).orElse(null);
      BigDecimal taxaGlobal = configOpt.map(ConfiguracaoGeral::getTaxaJurosAtraso).orElse(null);

      System.out.println("LOG: VendaService.registrarVenda - Configurações globais - Prazo: " + prazoGlobal
          + ", Taxa de Juros: " + taxaGlobal);
      System.out.println(
          "LOG: VendaService.registrarVenda - Saldo devedor atual: " + cliente.getSaldoDevedor() + ", Value da venda: "
              + vendaSalva.getValueTotal() + ", Novo saldo: " + novoSaldoDevedor);

      if (cliente.getLimiteFiado() != null && novoSaldoDevedor.compareTo(cliente.getLimiteFiado()) > 0) {
        System.out.println(
            "LOG: VendaService.registrarVenda - Limite fiado excedido para cliente: " + cliente.getName() + ". Limite: "
                + cliente.getLimiteFiado() + ", Saldo após venda: " + novoSaldoDevedor);
        throw new BusinessException("Limite fiado excedido para o cliente: " + cliente.getName() +
            ". Limite: " + cliente.getLimiteFiado() + ", Saldo após venda: " + novoSaldoDevedor);
      }
      cliente.setSaldoDevedor(novoSaldoDevedor);
      cliente.setDataUltimaCompraFiado(vendaSalva.getDataVenda());
      System.out.println("LOG: VendaService.registrarVenda - Cliente devedor atualizado: " + cliente);
      clienteRepository.save(cliente);
      System.out.println("LOG: VendaService.registrarVenda - Cliente salvo com novo saldo devedor.");
    }

    System.out.println(
        "LOG: VendaService.registrarVenda - Registro de venda finalizado com sucesso. Venda ID: " + vendaSalva.getId());
    return new VendaResponseDTO(vendaSalva);
  }

  /**
   * Lista vendas com filtros opcionais por data, cliente e forma de pagamento.
   * 
   * @param inicio               Data/hora inicial do filtro
   * @param fim                  Data/hora final do filtro
   * @param clienteId            ID do cliente para filtro (opcional)
   * @param formaPagamentoString Forma de pagamento para filtro (opcional)
   * @return Lista de VendaResponseDTO correspondentes aos filtros
   */

  @Transactional(readOnly = true)
  public PageResponse<VendaResponseDTO> listVendas(
      ZonedDateTime inicio,
      ZonedDateTime fim,
      Long clienteId,
      String formaPagamentoString,
      Long productId,
      String orderBy,
      int page,
      int size) {
    System.out.println("LOG: VendaService.listVendas - inicio: " + inicio + ", fim: " + fim
        + ", clienteId: " + clienteId + ", formaPagamentoString: " + formaPagamentoString);

    // Extrai lógica de conversão e datas padrão
    FormaPagamento formaPagamentoFilter = VendaMapper.parseFormaPagamento(formaPagamentoString);
    ZonedDateTime[] range = VendaMapper.buildDataRange(inicio, fim);

    Sort sort = VendaMapper.buildSort(orderBy);
    Pageable pageable = PageRequest.of(page, size, sort);

    System.out.println("SERVICE: VendaService.listVendas - Usando sort: " + sort);
    System.out.println("SERVICE: Query - Inicio: " + range[0] + ", Fim: " + range[1]
        + ", ClienteId: " + clienteId + ", FormaPagamento: " + formaPagamentoFilter
        + ", Product: " + productId);

    Page<Venda> vendasPage = vendaRepository.findVendasComFilters(
        range[0],
        range[1],
        clienteId,
        formaPagamentoFilter,
        productId,
        pageable);

    Page<VendaResponseDTO> dtoPage = vendasPage.map(VendaResponseDTO::new);
    System.out.println("LOG: VendaService.listVendas - Vendas encontradas: " + vendasPage.getTotalElements());
    return new PageResponse<>(dtoPage);
  }

  public static FormaPagamento parseFormaPagamento(String formaPagamentoString) {
    if (formaPagamentoString == null || formaPagamentoString.isEmpty()) {
      return null;
    }

    try {
      return FormaPagamento.valueOf(formaPagamentoString.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @Transactional(readOnly = true)
  public BigDecimal calcularTotalBrutoVendas(ZonedDateTime inicio, ZonedDateTime fim,
      Long clienteId, String formaPagamentoString, Long productId) {
    FormaPagamento formaPagamentoFilter = parseFormaPagamento(formaPagamentoString);
    ZonedDateTime inicioQuery = (inicio != null) ? inicio : ZonedDateTime.parse("1900-01-01T00:00:00Z");
    ZonedDateTime fimQuery = (fim != null) ? fim : ZonedDateTime.parse("9999-12-31T23:59:59Z");

    return vendaRepository.sumValueTotalComFilters(
        inicioQuery, fimQuery, clienteId, formaPagamentoFilter, productId);
  }

  @Transactional(readOnly = true)
  public List<TotalPorFormaPagamentoDTO> calcularTotaisPorFormaPagamento(
      ZonedDateTime inicio, ZonedDateTime fim) {
    ZonedDateTime inicioQuery = (inicio != null) ? inicio : ZonedDateTime.parse("1900-01-01T00:00:00Z");
    ZonedDateTime fimQuery = (fim != null) ? fim : ZonedDateTime.parse("9999-12-31T23:59:59Z");

    List<Object[]> results = vendaRepository.sumValueTotalGroupByFormaPagamentoBetween(inicioQuery, fimQuery);

    return results.stream()
        .map(res -> new TotalPorFormaPagamentoDTO((FormaPagamento) res[0], (BigDecimal) res[1]))
        .collect(Collectors.toList());
  }

  /**
   * Busca uma venda pelo ID, incluindo itens e cliente relacionados.
   *
   * @param id Identificador da venda
   * @return VendaResponseDTO com os dados da venda encontrada
   */
  @Transactional(readOnly = true)
  public VendaResponseDTO buscarVendaPorId(Long id) {
    Venda venda = ((Optional<Venda>) vendaRepository.findByIdComItensECliente(id))
        .orElseThrow(() -> new RecursoNaoEncontrado("Venda não encontrada com ID: " + id));
    return new VendaResponseDTO(venda);
  }

  /**
   * Lógica para buscar a venda, estornar estoque, estornar saldo fiado e delete
   * a venda.
   * 
   * @param id Identificador da venda
   */
  @Transactional
  public void deleteVendaFisicamente(Long vendaId) {
    System.out
        .println("DELETE_START: VendaService.deleteVendaFisicamente - Buscando venda para deleção... - " + vendaId);

    Venda vendaParaDelete = vendaRepository.findByIdComItensECliente(vendaId)
        .orElseThrow(() -> {
          System.err.println("ERROR: VendaService.deleteVendaFisicamente - Venda não encontrada: " + vendaId);
          return new RecursoNaoEncontrado("Venda não encontrada: " + vendaId);
        });

    System.out.println("DELETE_LOG_01: VendaService.deleteVendaFisicamente - Venda " + vendaId + " encontrada.");
    System.out.println("DELETE_LOG_02: VendaService.deleteVendaFisicamente - Estornando itens ao estoque... ");
    List<Product> productsParaAtualizarEstoque = new ArrayList<>();
    if (vendaParaDelete.getItens() != null) {
      for (ItemVenda item : vendaParaDelete.getItens()) {
        Product productDoItem = item.getProduct();
        if (productDoItem != null) {
          Product productParaEstornar = productRepository.findById(productDoItem.getId())
              .orElseThrow(() -> new RecursoNaoEncontrado(
                  "Produo associado ao item da venda não encontrado: ID " + productDoItem.getId()));
          if (productParaEstornar.getStockQuantity() != null) {
            System.out.println("DELETE_LOG_03: VendaService.deleteVendaFisicamente - Processando estoque do item "
                + productParaEstornar.getName() + " de ID " + productParaEstornar.getId()
                + ". Quantia a ser processada: " + item.getQuantidade());
            BigDecimal novoEstoque = productParaEstornar.getStockQuantity().add(item.getQuantidade());
            productParaEstornar.setStockQuantity(novoEstoque);
            productsParaAtualizarEstoque.add(productParaEstornar);
            System.out.println("DELETE_LOG_04: VendaService.deleteVendaFisicamente. SUCESSO ao PROCESSAR ESTOQUE de "
                + productDoItem.getName() + ". Novo Estoque: " + novoEstoque);
          } else {
            System.out.println("DELETE_LOG_03_S/ESTOQUE - VendaService.deleteVendaFisicamente - Product "
                + productParaEstornar.getName() + "de ID: " + productParaEstornar.getId()
                + ", não possui controle de estoque. SUCESSO AO PROCESSAR.");
          }
        }
      }
    }
    if (!productsParaAtualizarEstoque.isEmpty()) {
      productRepository.saveAll(productsParaAtualizarEstoque);
      System.out.println("DELETE_LOG: VendaService.deleteVendaFisicamente. SUCESSO ao PROCESSAR ESTOQUE");
    }

    if (vendaParaDelete.getFormaPagamento() == FormaPagamento.FIADO && vendaParaDelete.getCliente() != null) {
      Cliente clienteDaVenda = clienteRepository.findById(vendaParaDelete.getCliente().getId())
          .orElse(null);
      System.out
          .println("DELETE_LOG_FIADO: VendaService.deleteVendaFisicamente. Estornando crédito de fiado ao cliente"
              + clienteDaVenda.getName() + "...");
      BigDecimal saldoAtual = clienteDaVenda.getSaldoDevedor();
      BigDecimal valueVenda = vendaParaDelete.getValueTotal();

      if (clienteDaVenda != null) {
        clienteDaVenda.setSaldoDevedor(saldoAtual.subtract(valueVenda));

        System.out.println("DELETE_LOG_FIADO: SUCESSO ao estornar R$" + valueVenda + " para o cliente "
            + clienteDaVenda.getName() + " totalizando no SALDO ATUAL: R$" + clienteDaVenda.getSaldoDevedor());

        if (clienteDaVenda.getSaldoDevedor().compareTo(BigDecimal.ZERO) <= 0
            && vendaParaDelete.getDataVenda().equals(clienteDaVenda.getDataUltimaCompraFiado())) {
          System.out.println(
              "LOG: VendaService.deleteVendaFisicamente - Saldo do cliente zerado ou negactive. Data da última compra fiado pode precisar de reavaliação.");
        }
        clienteRepository.save(clienteDaVenda);
        System.out.println("LOG: VendaService.deleteVendaFisicamente - Saldo devedor do cliente ID " +
            clienteDaVenda.getId() + " estornado. Novo saldo: " + clienteDaVenda.getSaldoDevedor());
      }
    }
    System.out.println("DELETE_LOG: Deletando Venda...");
    vendaRepository.delete(vendaParaDelete);
    System.out
        .println("LOG: VendaService.deleteVendaFisicamente - Venda ID " + vendaId + " deletada permanentemente.");
    System.out.println("DELETE_LOG: VENDA DELETADA COM SUCESSO");
  }

  public List<GroupsummaryDTO> getVendassummary(
      ZonedDateTime inicio, ZonedDateTime fim, Long clienteId,
      String formaPagamentoString, Long productId, String groupBy) {
    System.out.println("LOG: VendaService.getVendassummary - Iniciando resumo de vendas com filtros: "
        + "Inicio: " + inicio + ", Fim: " + fim + ", ClienteId: " + clienteId
        + ", FormaPagamento: " + formaPagamentoString + ", ProductId: " + productId
        + ", GroupBy: " + groupBy);

    FormaPagamento formaPagamentoFilter = VendaMapper.parseFormaPagamento(formaPagamentoString);
    ZonedDateTime incioQuery = (inicio != null) ? inicio
        : ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, java.time.ZoneId.systemDefault());
    ZonedDateTime fimQuery = (fim != null) ? fim
        : ZonedDateTime.of(9999, 12, 31, 23, 59, 59, 0, java.time.ZoneId.systemDefault());

    if ("dataVenda".equalsIgnoreCase(groupBy)) {
      List<Object[]> results = vendaRepository.sumTotalGroupByDay(incioQuery, fimQuery, clienteId, formaPagamentoFilter,
          productId);
      return results.stream()
          .map(res -> new GroupsummaryDTO((String) res[0], (String) res[0], (BigDecimal) res[1]))
          .collect(Collectors.toList());
    }

    if ("cliente.name".equalsIgnoreCase(groupBy)) {
      List<Object[]> results = vendaRepository.sumTotalGroupByCliente(incioQuery, fimQuery, clienteId,
          formaPagamentoFilter, productId);
      return results.stream()
          .map(res -> new GroupsummaryDTO(String.valueOf(res[0]), (String) res[1], (BigDecimal) res[2]))
          .collect(Collectors.toList());
    }

    return Collections.emptyList();
  }

}
