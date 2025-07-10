package br.com.king.flick_business.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import br.com.king.flick_business.enums.FormaPagamento;

public record DashboardSummaryDTO(
                BigDecimal totalVendasBruto,
                Map<FormaPagamento, BigDecimal> totalVendasPorFormaPagamento,
                BigDecimal totalExpenses, // Soma de expense.valor
                BigDecimal lucroBrutoEstimado, // Vendas - Despsas
                BigDecimal ticketMedio,
                Long quantidadeVendas,
                ProductMaisVendidoDTO productMaisVendido,
                List<DataPointDTO> graficoVendasDiarias) {
}