package br.com.king.flick_business.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import br.com.king.flick_business.enums.FormaPagamento;

public record DashboardSummaryDTO(
    BigDecimal totalVendasBruto,
    Map<FormaPagamento, BigDecimal> totalVendasPorFormaPagamento,
    BigDecimal totalDespesas, // Soma de despesa.valor
    BigDecimal lucroBrutoEstimado, // Vendas - Despsas
    BigDecimal ticketMedio,
    Long quantidadeVendas,
    ProdutoMaisVendidoDTO produtoMaisVendido,
    List<DataPointDTO> graficoVendasDiarias) {
}