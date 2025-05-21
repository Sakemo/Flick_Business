package br.com.king.flick_business.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.king.flick_business.entity.Categoria;
import br.com.king.flick_business.entity.Fornecedor;
import br.com.king.flick_business.entity.Produto;
import br.com.king.flick_business.enums.TipoUnidadeVenda;

public record ProdutoResponseDTO(
    Long id,
    String nome,
    String descricao,
    String codigoBarras,
    BigDecimal quantidadeEstoque,
    BigDecimal precoVenda,
    BigDecimal precoCustoUnitario,
    TipoUnidadeVenda tipoUnidadeVenda,
    boolean ativo,
    Categoria categoria,
    Fornecedor fornecedor,
    LocalDateTime criadoEm,
    LocalDateTime atualizadoEm) {
  public ProdutoResponseDTO(Produto produto) {
    this(
        produto.getId(),
        produto.getNome(),
        produto.getDescricao(),
        produto.getCodigoBarras(),
        produto.getQuantidadeEstoque(),
        produto.getPrecoVenda(),
        produto.getPrecoCustoUnitario(),
        produto.getTipoUnidadeVenda(),
        produto.isAtivo(),
        produto.getCategoria() != null ? produto.getCategoria() : null,
        produto.getFornecedor() != null ? produto.getFornecedor() : null,
        produto.getCriadoEm(),
        produto.getAtualizadoEm());
  }
}
