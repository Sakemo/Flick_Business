package br.com.king.flick_business.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.king.flick_business.dto.request.SaleItemRequestDTO;
import br.com.king.flick_business.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaleRequestDTO(
                @JsonProperty("customerId") Long idCustomer,

                @NotEmpty(message = "A lista de items não pode ser vazia") @Valid List<SaleItemRequestDTO> items,

                @NotNull(message = "A forma de pagamento não pode ser nula") PaymentMethod paymentMethod,

                @Size(max = 500, message = "Deve ter no máximo 500 caracteres") String description) {
}