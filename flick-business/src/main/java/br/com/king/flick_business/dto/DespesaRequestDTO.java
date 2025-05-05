package br.com.king.flick_business.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.king.flick_business.enums.TipoDespesa;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record DespesaRequestDTO(
    @NotBlank(message = "O nome da despesa não pode ser vazio") @Size(min = 3, max = 100, message = "O nome da despesa deve ter entre 3 e 100 caracteres") String nome,

    @NotNull(message = "O valor da despesa não pode ser nulo") @DecimalMin(value = "0.01", message = "O valor da despesa deve ser maior que 0") @Digits(integer = 10, fraction = 2, message = "O valor da despesa deve ter no máximo 10 dígitos inteiros e 2 decimais") BigDecimal valor,

    @NotNull(message = "A data da despesa não pode ser nula") @PastOrPresent(message = "A data da despesa deve ser no passado ou presente") LocalDateTime dataDespesa,

    @NotNull(message = "O tipo da despesa não pode ser nulo") TipoDespesa tipoDespesa) {
}