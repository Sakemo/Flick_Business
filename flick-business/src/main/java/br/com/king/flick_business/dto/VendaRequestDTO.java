package br.com.king.flick_business.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.king.flick_business.dto.request.ItemVendaRequestDTO;
import br.com.king.flick_business.enums.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VendaRequestDTO(
        @JsonProperty("clienteId") Long idCliente,

        @NotEmpty(message = "A lista de itens não pode ser vazia") @Valid List<ItemVendaRequestDTO> itens,

        @NotNull(message = "A forma de pagamento não pode ser nula") FormaPagamento formaPagamento,

        @Size(max = 500, message = "Deve ter no máximo 500 caracteres") String observacoes) {
}