package br.com.king.flick_business.mapper;

import br.com.king.flick_business.dto.*;
import br.com.king.flick_business.entity.Category;
import br.com.king.flick_business.entity.Provider;
import br.com.king.flick_business.entity.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
  public Product toEntity(ProductRequestDTO requestDTO, Category category, Provider provider) {
    return Product.builder()
        .name(requestDTO.name())
        .description(requestDTO.description())
        .barcode(requestDTO.barcode())
        .active(requestDTO.active())
        .salePrice(requestDTO.salePrice())
        .costPrice(requestDTO.precoCustoUnitario())
        .stockQuantity(requestDTO.stockQuantity())
        .unitOfSale(requestDTO.UnitOfSale())
        .category(category)
        .provider(provider)
        .build();
  }

  public void updateEntityFromDTO(ProductRequestDTO requestDTO, Product productExtistente, Category category,
      Provider provider) {
    productExtistente.setName(requestDTO.name());
    productExtistente.setDescription(requestDTO.description());
    productExtistente.setBarcode(requestDTO.barcode());
    productExtistente.setActive(requestDTO.active());
    productExtistente.setSalePrice(requestDTO.salePrice());
    productExtistente.setCostPrice(requestDTO.precoCustoUnitario());
    productExtistente.setStockQuantity(requestDTO.stockQuantity());
    productExtistente.setUnitOfSale(requestDTO.UnitOfSale());
    productExtistente.setCategory(category);
    productExtistente.setProvider(provider);
  }

  public ProductResponseDTO toResponseDTO(Product product) {
    return new ProductResponseDTO(product);
  }

  public List<ProductResponseDTO> toResponseDTOList(List<Product> products) {
    return products.stream()
        .map(this::toResponseDTO)
        .collect(Collectors.toList());
  }
}