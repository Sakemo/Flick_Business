package br.com.king.flick_business.dto;

import java.math.BigDecimal;

public record ProdutoMaisVendidoDTO(
    Long produtoId, String nomeProduto,
    BigDecimal quantidadeTotalVendida, BigDecimal valorTotalVendidoProduto) {
}