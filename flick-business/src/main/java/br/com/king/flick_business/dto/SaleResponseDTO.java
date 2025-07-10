package br.com.king.flick_business.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import br.com.king.flick_business.dto.response.CustomerResponseDTO;
import br.com.king.flick_business.dto.response.SaleItemResponseDTO;
import br.com.king.flick_business.entity.Sale;
import br.com.king.flick_business.enums.PaymentMethod;

public record SaleResponseDTO(
    Long id,
    ZonedDateTime dateSale,
    BigDecimal totalValue,
    CustomerResponseDTO customer,
    List<SaleItemResponseDTO> items,
    PaymentMethod paymentMethod,
    String description) {
  public SaleResponseDTO(Sale sale) {
    this(
        sale.getId(),
        sale.getDateSale(),
        sale.getTotalValue(),
        sale.getCustomer() != null ? new CustomerResponseDTO(sale.getCustomer()) : null,
        sale.getItems().stream()
            .map(SaleItemResponseDTO::new)
            .collect(Collectors.toList()),
        sale.getPaymentMethod(),
        sale.getDescription());
  }
}