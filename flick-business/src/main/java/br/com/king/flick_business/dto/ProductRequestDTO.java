package br.com.king.flick_business.dto;

import java.math.BigDecimal;

import br.com.king.flick_business.enums.UnitOfSale;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequestDTO(
                @NotBlank(message = "Product Name is required") @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters long") String name,

                @Size(max = 300, message = "Description must not exceed 300 characters") String description,

                @Size(max = 50, message = "Barcode must not exceed 50 characters") String barcode,

                @DecimalMin(value = "0.0", inclusive = true, message = "The quantity cannot be negative") @Digits(integer = 10, fraction = 3, message = "Invalid Format") BigDecimal stockQuantity,

                @NotNull(message = "Preço de venda é obrigatório") @DecimalMin(value = "0.01", message = "Preço de venda deve ser maior que zero") @Digits(integer = 8, fraction = 2, message = "Preço de venda inválido") BigDecimal salePrice,

                @DecimalMin(value = "0.0", message = "Preço de custo não pode ser negactive") @Digits(integer = 8, fraction = 2, message = "Preço de custo inválido") BigDecimal precoCustoUnitario,

                @NotNull(message = "Unidade de venda é obrigatória") UnitOfSale UnitOfSale,

                @NotNull(message = "Campo 'active' é obrigatório") Boolean active,

                @NotNull(message = "Category é obrigatória") Long categoryId,

                Long providerId) {
}
