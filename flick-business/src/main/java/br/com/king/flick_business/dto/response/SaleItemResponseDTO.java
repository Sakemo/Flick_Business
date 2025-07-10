package br.com.king.flick_business.dto.response;

import java.math.BigDecimal;

import br.com.king.flick_business.dto.ProductResponseDTO;
import br.com.king.flick_business.entity.SaleItem;

public record SaleItemResponseDTO(
    Long id,
    ProductResponseDTO product,
    BigDecimal quantidade,
    BigDecimal priceUnitarioSale,
    BigDecimal totalValueItem) {

  public SaleItemResponseDTO(SaleItem item) {
    this(
        item.getId(),
        item.getProduct() != null ? new ProductResponseDTO(item.getProduct()) : null,
        item.getQuantidade(),
        item.getPriceUnitarioSale(),
        item.getTotalValueItem());
  }
}