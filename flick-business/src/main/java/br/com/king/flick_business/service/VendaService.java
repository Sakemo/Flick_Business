package br.com.king.flick_business.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.king.flick_business.dto.ItemVendaRequestDTO;
import br.com.king.flick_business.dto.VendaRequestDTO;
import br.com.king.flick_business.dto.VendaResponseDTO;
import br.com.king.flick_business.entity.Cliente;
import br.com.king.flick_business.entity.ItemVenda;
import br.com.king.flick_business.entity.Produto;
import br.com.king.flick_business.entity.Venda;
import br.com.king.flick_business.enums.FormaPagamento;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.repository.ClienteRepository;
import br.com.king.flick_business.repository.ProdutoRepository;
import br.com.king.flick_business.repository.VendaRepository;

@Service
public class VendaService {
  private final VendaRepository vendaRepository;
  private final ClienteRepository clienteRepository;
  private final ProdutoRepository produtoRepository;

  public VendaService(VendaRepository vendaRepository, ClienteRepository clienteRepository,
      ProdutoRepository produtoRepository) {
    this.vendaRepository = vendaRepository;
    this.clienteRepository = clienteRepository;
    this.produtoRepository = produtoRepository;
  }

  @Transactional
  public VendaResponseDTO registrarVenda(VendaRequestDTO requestDTO) {

    Venda novaVenda = new Venda();
    novaVenda.setFormaPagamento(requestDTO.formaPagamento());
    novaVenda.setObservacoes(requestDTO.observacoes());

    Cliente cliente = null;

    if (requestDTO.formaPagamento() == FormaPagamento.FIADO) {
      if (requestDTO.idCliente() == null) {
        throw new RecursoNaoEncontrado("ID do Cliente é obrigatório para vendas FIADO.");
      }
      cliente = clienteRepository.findById(requestDTO.idCliente())
          .orElseThrow(() -> new RecursoNaoEncontrado("Cliente não encontrado com ID: " + requestDTO.idCliente()));
      if (!cliente.getAtivo()) {
        throw new RecursoNaoEncontrado("Cliente inativo não pode comprar fiado.");
      }
      if (cliente.getControleFiado() != null && !cliente.getControleFiado()) {
        throw new RecursoNaoEncontrado("Este cliente não está habilitado para compras fiado.");
      }
      novaVenda.setCliente(cliente);
    }

    BigDecimal valorTotalCalculado = BigDecimal.ZERO;
    List<Produto> produtosParaAtualizarEstoque = new ArrayList<>();

    for (ItemVendaRequestDTO itemDTO : requestDTO.itens()) {
      Produto produto = produtoRepository.findById(itemDTO.idProduto())
          .orElseThrow(() -> new RecursoNaoEncontrado("Produto não encontrado com ID: " + itemDTO.idProduto()));

      if (!produto.isAtivo()) {
        throw new RecursoNaoEncontrado("Produto inativo: " + produto.getNome());
      }

      BigDecimal quantidade = (BigDecimal) itemDTO.quantidade();
      BigDecimal precoUnitarioAtual = produto.getPrecoVenda();

      if (precoUnitarioAtual == null || precoUnitarioAtual.compareTo(BigDecimal.ZERO) <= 0) {
        throw new RecursoNaoEncontrado("Preço inválido ou não definido para o produto: " + produto.getNome());
      }

      ItemVenda itemVenda = ItemVenda.builder()
          .produto(produto)
          .quantidade(quantidade)
          .precoUnitarioVenda(precoUnitarioAtual)
          .build();

      novaVenda.adicionarItem(itemVenda);

      valorTotalCalculado = valorTotalCalculado.add(itemVenda.getValorTotalItem());

      BigDecimal estoqueAtual = produto.getQuantidadeEstoque();
      if (estoqueAtual != null && estoqueAtual.compareTo(BigDecimal.ZERO) > 0) {
        if (estoqueAtual.compareTo(quantidade) < 0) {
          throw new RecursoNaoEncontrado("Estoque insuficiente para o produto: " + produto.getNome() +
              ". Em estoque: " + estoqueAtual + ", Solicitado: " + quantidade);
        }
        produto.setQuantidadeEstoque(estoqueAtual.subtract(quantidade));
        produtosParaAtualizarEstoque.add(produto);
      }
    }

    novaVenda.setValorTotal(valorTotalCalculado);
    Venda vendaSalva = vendaRepository.save(novaVenda);

    if (!produtosParaAtualizarEstoque.isEmpty()) {
      produtoRepository.saveAll(produtosParaAtualizarEstoque);
    }

    if (vendaSalva.getFormaPagamento() == FormaPagamento.FIADO && cliente != null) {
      BigDecimal novoSaldoDevedor = cliente.getSaldoDevedor().add(vendaSalva.getValorTotal());
      if (cliente.getLimiteFiado() != null && novoSaldoDevedor.compareTo(cliente.getLimiteFiado()) > 0) {
        throw new RecursoNaoEncontrado("Limite fiado excedido para o cliente: " + cliente.getNome() +
            ". Limite: " + cliente.getLimiteFiado() + ", Saldo após venda: " + novoSaldoDevedor);
      }
      cliente.setSaldoDevedor(novoSaldoDevedor);
      clienteRepository.save(cliente);
    }

    return new VendaResponseDTO(vendaSalva);
  }

  @Transactional(readOnly = true)
  public List<VendaResponseDTO> listarVendas() {
    List<Venda> vendas = (List<Venda>) vendaRepository.FindAllComCliente();
    return vendas.stream()
        .map(VendaResponseDTO::new)
        .toList();
  }

  @Transactional(readOnly = true)
  public VendaResponseDTO buscarVendaPorId(Long id) {
    Venda venda = ((Optional<Venda>) vendaRepository.findByIdComItensECliente(id))
        .orElseThrow(() -> new RecursoNaoEncontrado("Venda não encontrada com ID: " + id));
    return new VendaResponseDTO(venda);
  }
}
