package br.com.king.flick_business.dto.request;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import br.com.king.flick_business.enums.TipoExpense;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record ExpenseRequestDTO(
        @NotBlank(message = "O name da expense não pode ser vazio") @Size(min = 3, max = 100, message = "O name da expense deve ter entre 3 e 100 caracteres") String name,

        @NotNull(message = "O valor da expense não pode ser nulo") @DecimalMin(value = "0.01", message = "O valor da expense deve ser maior que 0") @Digits(integer = 10, fraction = 2, message = "O valor da expense deve ter no máximo 10 dígitos inteiros e 2 decimais") BigDecimal valor,

        @NotNull(message = "A data da expense não pode ser nula") @PastOrPresent(message = "A data da expense deve ser no passado ou presente") ZonedDateTime dataExpense,

        @NotNull(message = "O tipo da expense não pode ser nulo") TipoExpense tipoExpense,

        String observacao) {
}