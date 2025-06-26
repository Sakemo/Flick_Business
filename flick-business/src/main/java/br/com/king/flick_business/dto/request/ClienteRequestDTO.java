package br.com.king.flick_business.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(
        @NotBlank(message = "O nome é obrigatório.") @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres.") String nome,

        @Size(max = 20, message = "O CPF deve ter no máximo 20 caracteres.") String cpf,

        @Size(max = 11, message = "O telefone deve ter no máximo 11 caracteres.") String telefone,

        @Size(max = 150, message = "O endereço deve ter no máximo 150 caracteres.") String endereco,

        Boolean controleFiado,

        @DecimalMin(value = "0.00", message = "O limite de fiado não pode ser negativo.") BigDecimal limiteFiado,

        Boolean ativo) {
}