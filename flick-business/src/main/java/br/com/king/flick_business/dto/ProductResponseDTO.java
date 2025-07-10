package br.com.king.flick_business.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import br.com.king.flick_business.entity.Category;
import br.com.king.flick_business.entity.Provider;
import br.com.king.flick_business.entity.Product;
import br.com.king.flick_business.enums.UnitOfSale;

public record ProductResponseDTO(
    Long id,
    String name,
    String description,
    String barcode,
    BigDecimal stockQuantity,
    BigDecimal salePrice,
    BigDecimal costPrice,
    UnitOfSale unitOfSale,
    boolean active,
    Category category,
    Provider provider,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt) {
  public ProductResponseDTO(Product product) {
    this(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getBarcode(),
        product.getStockQuantity(),
        product.getSalePrice(),
        product.getCostPrice(),
        product.getUnitOfSale(),
        product.isActive(),
        product.getCategory() != null ? product.getCategory() : null,
        product.getProvider() != null ? product.getProvider() : null,
        product.getCreatedAt(),
        product.getUpdatedAt());
  }
}
