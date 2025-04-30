package br.com.king.flick_business.dto;

import java.math.BigDecimal;

import br.com.king.flick_business.entity.ItemVenda;

public record ItemVendaResponseDTO(
    Long id,
    ProdutoResponseDTO produto,
    BigDecimal quantidade,
    BigDecimal precoUnitarioVenda,
    BigDecimal valorTotalItem) {

  public ItemVendaResponseDTO(ItemVenda item) {
    this(
        item.getId(),
        item.getProduto() != null ? new ProdutoResponseDTO(item.getProduto()) : null,
        item.getQuantidade(),
        item.getPrecoUnitarioVenda(),
        item.getValorTotalItem());
  }
}