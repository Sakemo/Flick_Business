package br.com.king.flick_business.dto;

import java.math.BigDecimal;

public record ProductMaisVendidoDTO(
                Long productId, String nameProduct,
                BigDecimal quantidadeTotalVendida, BigDecimal valueTotalVendidoProduct) {
}