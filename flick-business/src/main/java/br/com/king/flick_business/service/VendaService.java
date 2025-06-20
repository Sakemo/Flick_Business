package br.com.king.flick_business.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.king.flick_business.dto.ItemVendaRequestDTO;
import br.com.king.flick_business.dto.PageResponse;
import br.com.king.flick_business.dto.VendaRequestDTO;
import br.com.king.flick_business.dto.VendaResponseDTO;
import br.com.king.flick_business.entity.Cliente;
import br.com.king.flick_business.entity.ConfiguracaoGeral;
import br.com.king.flick_business.entity.ItemVenda;
import br.com.king.flick_business.entity.Produto;
import br.com.king.flick_business.entity.Venda;
import br.com.king.flick_business.enums.FormaPagamento;
import br.com.king.flick_business.exception.BusinessException;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.repository.ClienteRepository;
import br.com.king.flick_business.repository.ProdutoRepository;
import br.com.king.flick_business.repository.VendaRepository;

@Service
public class VendaService {
  // Repositórios e serviços necessários para operações de venda
  private final VendaRepository vendaRepository;
  private final ClienteRepository clienteRepository;
  private final ProdutoRepository produtoRepository;
  private final ConfiguracaoGeralService configuracaoService;

  // Construtor para injeção de dependências
  public VendaService(VendaRepository vendaRepository, ClienteRepository clienteRepository,
      ProdutoRepository produtoRepository, ConfiguracaoGeralService configuracaoService) {
    this.vendaRepository = vendaRepository;
    this.clienteRepository = clienteRepository;
    this.produtoRepository = produtoRepository;
    this.configuracaoService = configuracaoService;
  }

  /**
   * Registra uma nova venda, atualiza estoque dos produtos e saldo devedor do
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
    novaVenda.setDataVenda(LocalDateTime.now());

    // Setando Cliente
    Cliente cliente = null;
    if (requestDTO.idCliente() != null) {
      System.out.println("LOG: VendaService.registrarVenda - idCliente fornecido: " + requestDTO.idCliente()
          + ". Buscando cliente...");
      cliente = clienteRepository.findById(requestDTO.idCliente())
          .orElseThrow(() -> new RecursoNaoEncontrado("Cliente não encontrado com ID: " + requestDTO.idCliente()));

      if (!cliente.getAtivo()) {
        System.out.println("LOG: VendaService.registrarVenda - Cliente selecionado está inativo." + cliente.getNome());
        // TODO: adicionar reativação rapida a partir desse momento com modal
        throw new RecursoNaoEncontrado("Não é possível registrar venda para cliente inativo.");
      }
      novaVenda.setCliente(cliente);
      System.out.println("LOG: VendaService.registrarVenda - Cliente associado a venda: " + cliente.getNome());
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
            .println("LOG: VendaService.registrarVenda - Cliente não habilitado para fiado: " + cliente.getNome());
        throw new RecursoNaoEncontrado("Este cliente não está habilitado para compras fiado.");
      }
      System.out.println("LOG: VendaService.registrarVenda - Cliente validado para venda FIADO: " + cliente.getNome());
    }

    BigDecimal valorTotalCalculado = BigDecimal.ZERO;
    List<Produto> produtosParaAtualizarEstoque = new ArrayList<>();

    // Processa cada item da venda, valida estoque e calcula valor total
    for (ItemVendaRequestDTO itemDTO : requestDTO.itens()) {
      System.out.println("LOG: VendaService.registrarVenda - Processando item da venda: " + itemDTO);
      Produto produto = produtoRepository.findById(itemDTO.idProduto())
          .orElseThrow(() -> {
            System.out
                .println("LOG: VendaService.registrarVenda - Produto não encontrado com ID: " + itemDTO.idProduto());
            return new RecursoNaoEncontrado("Produto não encontrado com ID: " + itemDTO.idProduto());
          });

      if (!produto.isAtivo()) {
        System.out.println("LOG: VendaService.registrarVenda - Produto inativo: " + produto.getNome());
        throw new RecursoNaoEncontrado("Produto inativo: " + produto.getNome());
      }

      BigDecimal quantidade = itemDTO.quantidade();
      BigDecimal precoUnitarioAtual = produto.getPrecoVenda();

      if (precoUnitarioAtual == null || precoUnitarioAtual.compareTo(BigDecimal.ZERO) <= 0) {
        System.out.println("LOG: VendaService.registrarVenda - Preço inválido para produto: " + produto.getNome());
        throw new RecursoNaoEncontrado("Preço inválido ou não definido para o produto: " + produto.getNome());
      }

      ItemVenda itemVenda = ItemVenda.builder()
          .produto(produto)
          .quantidade(quantidade)
          .precoUnitarioVenda(precoUnitarioAtual)
          .build();

      novaVenda.adicionarItem(itemVenda);

      valorTotalCalculado = valorTotalCalculado.add(itemVenda.getValorTotalItem());
      System.out.println("LOG: VendaService.registrarVenda - Item adicionado. Produto: " + produto.getNome()
          + ", Quantidade: " + quantidade
          + ", Preço: " + precoUnitarioAtual + ", Valor total acumulado: " + valorTotalCalculado);

      // Atualiza estoque do produto
      BigDecimal estoqueAtual = produto.getQuantidadeEstoque();
      if (estoqueAtual != null && estoqueAtual.compareTo(BigDecimal.ZERO) > 0) {
        if (estoqueAtual.compareTo(quantidade) < 0) {
          System.out.println("LOG: VendaService.registrarVenda - Estoque insuficiente para produto: "
              + produto.getNome() + ". Em estoque: "
              + estoqueAtual + ", Solicitado: " + quantidade);
          throw new RecursoNaoEncontrado("Estoque insuficiente para o produto: " + produto.getNome() +
              ". Em estoque: " + estoqueAtual + ", Solicitado: " + quantidade);
        }
        produto.setQuantidadeEstoque(estoqueAtual.subtract(quantidade));
        produtosParaAtualizarEstoque.add(produto);
        System.out.println("LOG: VendaService.registrarVenda - Estoque atualizado para produto: " + produto.getNome()
            + ". Novo estoque: "
            + produto.getQuantidadeEstoque());
      } else {
        System.out.println("LOG: VendaService.registrarVenda - Produto sem estoque controlado ou estoque zerado: "
            + produto.getNome());
      }
    }

    novaVenda.setValorTotal(valorTotalCalculado);
    System.out.println("LOG: VendaService.registrarVenda - Valor total da venda calculado: " + valorTotalCalculado);

    Venda vendaSalva = vendaRepository.save(novaVenda);
    System.out.println("LOG: VendaService.registrarVenda - Venda salva com ID: " + vendaSalva.getId());

    // Salva atualização de estoque dos produtos
    if (!produtosParaAtualizarEstoque.isEmpty()) {
      produtoRepository.saveAll(produtosParaAtualizarEstoque);
      System.out.println("LOG: VendaService.registrarVenda - Estoque dos produtos atualizado no banco de dados.");
    }

    // Atualiza saldo devedor do cliente em caso de venda fiado
    if (vendaSalva.getFormaPagamento() == FormaPagamento.FIADO && cliente != null) {
      System.out
          .println("LOG: VendaService.registrarVenda - Iniciando atualização de saldo devedor para cliente FIADO: "
              + cliente.getNome());
      BigDecimal novoSaldoDevedor = cliente.getSaldoDevedor().add(vendaSalva.getValorTotal());

      // Busca configurações globais para cálculo de prazo e juros
      Optional<ConfiguracaoGeral> configOpt = configuracaoService.buscarEntidadeConfiguracao();
      Integer prazoGlobal = configOpt.map(ConfiguracaoGeral::getPrazoPagamentoFiado).orElse(null);
      BigDecimal taxaGlobal = configOpt.map(ConfiguracaoGeral::getTaxaJurosAtraso).orElse(null);

      System.out.println("LOG: VendaService.registrarVenda - Configurações globais - Prazo: " + prazoGlobal
          + ", Taxa de Juros: " + taxaGlobal);
      System.out.println(
          "LOG: VendaService.registrarVenda - Saldo devedor atual: " + cliente.getSaldoDevedor() + ", Valor da venda: "
              + vendaSalva.getValorTotal() + ", Novo saldo: " + novoSaldoDevedor);

      if (cliente.getLimiteFiado() != null && novoSaldoDevedor.compareTo(cliente.getLimiteFiado()) > 0) {
        System.out.println(
            "LOG: VendaService.registrarVenda - Limite fiado excedido para cliente: " + cliente.getNome() + ". Limite: "
                + cliente.getLimiteFiado() + ", Saldo após venda: " + novoSaldoDevedor);
        throw new BusinessException("Limite fiado excedido para o cliente: " + cliente.getNome() +
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
  public PageResponse<VendaResponseDTO> listarVendas(
      LocalDateTime inicio,
      LocalDateTime fim,
      Long clienteId,
      String formaPagamentoString,
      Long produtoId,
      String orderBy,
      int page,
      int size) {
    System.out.println("LOG: VendaService.listarVendas - inicio: " + inicio + ", fim" + fim + ", clienteId: "
        + clienteId + ", formaPagamentoString: " + formaPagamentoString);

    FormaPagamento formaPagamentoFiltro = null;
    if (formaPagamentoString != null && !formaPagamentoString.isEmpty()) {
      try {
        formaPagamentoFiltro = FormaPagamento.valueOf(formaPagamentoString.toUpperCase());
        System.out.println("LOG: VendaService.listarVendas - Filtro de forma de pagamento bem sucedido");
      } catch (IllegalArgumentException e) {
        System.err.println(
            "LOG: VendaService.listarVendas - Forma de pagamento inválida recebida no filtro: " + formaPagamentoString
                + ". Ignorando filtro de forma de pagamento.");
      }
    }

    Sort sort;
    if (orderBy != null && !orderBy.isBlank()) {
      String[] parts = orderBy.split(",");
      String property = parts[0];
      Sort.Direction direction = (parts.length > 1 && parts[1].equalsIgnoreCase("desc")) ? Sort.Direction.DESC
          : Sort.Direction.ASC;

      switch (property) {
        case "dataVenda" -> property = "dataVenda";
        case "valorTotal" -> property = "valorTotal";
        case "cliente.nome" -> property = "cliente.nome";
        default -> {
          System.out.println("SERVICE LOG: orderBy não reconhecida: " + property + "Usando default");
          property = "dataVenda";
          direction = Sort.Direction.DESC;
        }
      }
      sort = Sort.by(direction, property);
    } else {
      sort = Sort.by(Sort.Direction.DESC, "dataVenda");
    }

    Pageable pageable = PageRequest.of(page, size, sort);

    System.out.println("SERVICE: VendaService.listarVendas - Usando sort: " + sort);
    LocalDateTime inicioQuery;
    LocalDateTime fimQuery;

    boolean algumFiltrodeConteudoAplicado = (inicio != null || fim != null || clienteId != null
        || formaPagamentoFiltro != null || produtoId != null);

    if (algumFiltrodeConteudoAplicado) {
      inicioQuery = (inicio != null) ? inicio : LocalDateTime.of(1900, 1, 1, 0, 0);
      fimQuery = (fim != null) ? fim : LocalDateTime.of(9999, 12, 31, 23, 59, 59);
      System.out.println("SERVICE: Filtros aplicados. Usando para query - Inicio: " + inicioQuery + ", Fim: " + fimQuery
          + ", ClienteId: " + clienteId + ", FormaPagamento: " + formaPagamentoFiltro);
    } else {
      inicioQuery = (inicio != null) ? inicio : LocalDateTime.of(1900, 1, 1, 0, 0);
      fimQuery = (fim != null) ? fim : LocalDateTime.of(9999, 12, 31, 23, 59, 59);
      System.out
          .println("SERVICE: Nenhum filtro aplicado. Usando para query - Inicio: " + inicioQuery + ", Fim: " + fimQuery
              + ", ClienteId: " + clienteId + ", FormaPagamento: " + formaPagamentoFiltro + ", Produto: " + produtoId);
    }

    Page<Venda> vendasPage = vendaRepository.findVendasComFiltros(
        inicioQuery,
        fimQuery,
        clienteId,
        formaPagamentoFiltro,
        produtoId,
        pageable);

    Page<VendaResponseDTO> dtoPage = vendasPage.map(VendaResponseDTO::new);

    System.out.println("LOG: VendaService.listarVendas - Vendas encontradas após filtros: " + vendasPage);
    return new PageResponse<>(dtoPage);
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
   * Lógica para buscar a venda, estornar estoque, estornar saldo fiado e deletar
   * a venda.
   * 
   * @param id Identificador da venda
   */
  @Transactional
  public void deletarVendaFisicamente(Long vendaId) {
    System.out
        .println("DELETE_START: VendaService.deletarVendaFisicamente - Buscando venda para deleção... - " + vendaId);

    Venda vendaParaDeletar = vendaRepository.findByIdComItensECliente(vendaId)
        .orElseThrow(() -> {
          System.err.println("ERROR: VendaService.deletarVendaFisicamente - Venda não encontrada: " + vendaId);
          return new RecursoNaoEncontrado("Venda não encontrada: " + vendaId);
        });

    System.out.println("DELETE_LOG_01: VendaService.deletarVendaFisicamente - Venda " + vendaId + " encontrada.");
    System.out.println("DELETE_LOG_02: VendaService.deletarVendaFisicamente - Estornando itens ao estoque... ");
    List<Produto> produtosParaAtualizarEstoque = new ArrayList<>();
    if (vendaParaDeletar.getItens() != null) {
      for (ItemVenda item : vendaParaDeletar.getItens()) {
        Produto produtoDoItem = item.getProduto();
        if (produtoDoItem != null) {
          Produto produtoParaEstornar = produtoRepository.findById(produtoDoItem.getId())
              .orElseThrow(() -> new RecursoNaoEncontrado(
                  "Produo associado ao item da venda não encontrado: ID " + produtoDoItem.getId()));
          if (produtoParaEstornar.getQuantidadeEstoque() != null) {
            System.out.println("DELETE_LOG_03: VendaService.deletarVendaFisicamente - Processando estoque do item "
                + produtoParaEstornar.getNome() + " de ID " + produtoParaEstornar.getId()
                + ". Quantia a ser processada: " + item.getQuantidade());
            BigDecimal novoEstoque = produtoParaEstornar.getQuantidadeEstoque().add(item.getQuantidade());
            produtoParaEstornar.setQuantidadeEstoque(novoEstoque);
            produtosParaAtualizarEstoque.add(produtoParaEstornar);
            System.out.println("DELETE_LOG_04: VendaService.deletarVendaFisicamente. SUCESSO ao PROCESSAR ESTOQUE de "
                + produtoDoItem.getNome() + ". Novo Estoque: " + novoEstoque);
          } else {
            System.out.println("DELETE_LOG_03_S/ESTOQUE - VendaService.deletarVendaFisicamente - Produto "
                + produtoParaEstornar.getNome() + "de ID: " + produtoParaEstornar.getId()
                + ", não possui controle de estoque. SUCESSO AO PROCESSAR.");
          }
        }
      }
    }
    if (!produtosParaAtualizarEstoque.isEmpty()) {
      produtoRepository.saveAll(produtosParaAtualizarEstoque);
      System.out.println("DELETE_LOG: VendaService.deletarVendaFisicamente. SUCESSO ao PROCESSAR ESTOQUE");
    }

    if (vendaParaDeletar.getFormaPagamento() == FormaPagamento.FIADO && vendaParaDeletar.getCliente() != null) {
      Cliente clienteDaVenda = clienteRepository.findById(vendaParaDeletar.getCliente().getId())
          .orElse(null);
      System.out
          .println("DELETE_LOG_FIADO: VendaService.deletarVendaFisicamente. Estornando crédito de fiado ao cliente"
              + clienteDaVenda.getNome() + "...");
      BigDecimal saldoAtual = clienteDaVenda.getSaldoDevedor();
      BigDecimal valorVenda = vendaParaDeletar.getValorTotal();

      if (clienteDaVenda != null) {
        clienteDaVenda.setSaldoDevedor(saldoAtual.subtract(valorVenda));

        System.out.println("DELETE_LOG_FIADO: SUCESSO ao estornar R$" + valorVenda + " para o cliente "
            + clienteDaVenda.getNome() + " totalizando no SALDO ATUAL: R$" + clienteDaVenda.getSaldoDevedor());

        if (clienteDaVenda.getSaldoDevedor().compareTo(BigDecimal.ZERO) <= 0
            && vendaParaDeletar.getDataVenda().equals(clienteDaVenda.getDataUltimaCompraFiado())) {
          System.out.println(
              "LOG: VendaService.deletarVendaFisicamente - Saldo do cliente zerado ou negativo. Data da última compra fiado pode precisar de reavaliação.");
        }
        clienteRepository.save(clienteDaVenda);
        System.out.println("LOG: VendaService.deletarVendaFisicamente - Saldo devedor do cliente ID " +
            clienteDaVenda.getId() + " estornado. Novo saldo: " + clienteDaVenda.getSaldoDevedor());
      } else {
        System.err.println("WARN: Cliente com ID " + vendaParaDeletar.getCliente().getId() +
            " associado à venda FIADO " + vendaId + " não foi encontrado para estorno de saldo.");
      }
    }
    System.out.println("DELETE_LOG: Deletando Venda...");
    vendaRepository.delete(vendaParaDeletar);
    System.out
        .println("LOG: VendaService.deletarVendaFisicamente - Venda ID " + vendaId + " deletada permanentemente.");
    System.out.println("DELETE_LOG: VENDA DELETADA COM SUCESSO");
  }
}