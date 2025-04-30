package br.com.king.flick_business.dto;

import java.util.List;

import br.com.king.flick_business.enums.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VendaRequestDTO(
        @NotNull(message = "O id do cliente não pode ser nulo") Long idCliente,

        @NotEmpty(message = "A lista de itens não pode ser vazia") @Valid List<ItemVendaRequestDTO> itens,

        @NotNull(message = "A forma de pagamento não pode ser nula") FormaPagamento formaPagamento,

        @Size(max = 500, message = "Deve ter no máximo 500 caracteres") String observacoes) {
}