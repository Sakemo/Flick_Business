package br.com.king.flick_business.dto.response;

import java.math.BigDecimal;

import br.com.king.flick_business.dto.ProductResponseDTO;
import br.com.king.flick_business.entity.ItemVenda;

public record ItemVendaResponseDTO(
    Long id,
    ProductResponseDTO product,
    BigDecimal quantidade,
    BigDecimal precoUnitarioVenda,
    BigDecimal valueTotalItem) {

  public ItemVendaResponseDTO(ItemVenda item) {
    this(
        item.getId(),
        item.getProduct() != null ? new ProductResponseDTO(item.getProduct()) : null,
        item.getQuantidade(),
        item.getPrecoUnitarioVenda(),
        item.getValueTotalItem());
  }
}