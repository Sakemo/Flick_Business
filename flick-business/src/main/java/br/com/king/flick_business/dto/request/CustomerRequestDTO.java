package br.com.king.flick_business.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequestDTO(
        @NotBlank(message = "O name é obrigatório.") @Size(min = 2, max = 100, message = "O name deve ter entre 2 e 100 caracteres.") String name,

        @Size(max = 20, message = "O CPF deve ter no máximo 20 caracteres.") String taxId,

        @Size(max = 11, message = "O phone deve ter no máximo 11 caracteres.") String phone,

        @Size(max = 150, message = "O endereço deve ter no máximo 150 caracteres.") String adress,

        Boolean creditManagement,

        @DecimalMin(value = "0.00", message = "O limite de fiado não pode ser negactive.") BigDecimal creditLimit,

        Boolean active) {
}