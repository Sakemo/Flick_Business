package br.com.king.flick_business.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConfiguracaoGeralDTO(
    @Digits(integer = 3, fraction = 2, message = "Inválido. (ex: 5.00 para 5%)") BigDecimal taxaJuros,

    @Min(value = 1, message = "Mínimo de prazo de pagamento é 1 mês") Integer prazoPagamento,

    LocalDateTime dataAtualizacao,

    @Size(max = 50, message = "Máximo de 50 caracteres") @NotBlank(message = "Campo obrigatório") @NotNull(message = "Campo obrigatório") @Pattern(regexp = "[A-Za-z0-9 ]+", message = "Inválido. (ex: Loja do João)") String nomeNegocio) {
}