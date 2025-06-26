package br.com.king.flick_business.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record ItemVendaRequestDTO(
                @NotNull(message = "O id do produto não pode ser nulo") Long idProduto,

                @NotNull(message = "A quantidade não pode ser nula") @DecimalMin(value = "0.01", message = "A quantidade deve ser maior que 0") @Digits(integer = 8, fraction = 3, message = "A quantidade deve ter no máximo 8 dígitos inteiros e 3 dígitos decimais") BigDecimal quantidade) {

}