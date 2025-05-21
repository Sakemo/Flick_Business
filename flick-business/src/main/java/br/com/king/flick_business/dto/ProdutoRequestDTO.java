package br.com.king.flick_business.dto;

import java.math.BigDecimal;

import br.com.king.flick_business.enums.TipoUnidadeVenda;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProdutoRequestDTO(
        @NotBlank(message = "Nome do Produto é obrigatório") @Size(min = 2, max = 100, message = "Nome do produto deve ter entre 2 e 100 caracteres") String nome,

        @Size(max = 300, message = "Descrição não deve exceder 300 caracteres") String descricao,

        @Size(max = 50, message = "Código de barras não deve exceder 50 caracteres") String codigoBarras,

        @DecimalMin(value = "0.0", inclusive = true, message = "A quantidade não pode ser negativa") @Digits(integer = 10, fraction = 3, message = "Formato inválido") BigDecimal quantidadeEstoque,

        @NotNull(message = "Preço de venda é obrigatório") @DecimalMin(value = "0.01", message = "Preço de venda deve ser maior que zero") @Digits(integer = 8, fraction = 2, message = "Preço de venda inválido") BigDecimal precoVenda,

        @DecimalMin(value = "0.0", message = "Preço de custo não pode ser negativo") @Digits(integer = 8, fraction = 2, message = "Preço de custo inválido") BigDecimal precoCustoUnitario,

        @NotNull(message = "Unidade de venda é obrigatória") TipoUnidadeVenda tipoUnidadeVenda,

        @NotNull(message = "Campo 'ativo' é obrigatório") Boolean ativo,

        @NotNull(message = "Categoria é obrigatória") Long categoriaId,

        Long fornecedorId) {
}
