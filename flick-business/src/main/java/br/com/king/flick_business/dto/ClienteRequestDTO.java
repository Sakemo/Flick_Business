package br.com.king.flick_business.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(
        @NotBlank(message = "Obrigatório") @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres") String nome,

        @Size(max = 20, message = "Deve ter no máximo 20 caracteres") String cpf,

        @Size(max = 11, message = "Deve ter no máximo 11 caracteres") String telefone,

        @Size(max = 150) String endereco,

        Boolean controleFiado,

        @DecimalMin(value = "0.00", message = "Não pode ser negativo") BigDecimal limiteFiado,

        Boolean ativo) {
}