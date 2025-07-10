package br.com.king.flick_business.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List; // Para alternativa do product mais vendido
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.king.flick_business.dto.DashboardSummaryDTO; // Para divisão do ticket médio
import br.com.king.flick_business.dto.DataPointDTO; // Para converter resultado da query nativa
import br.com.king.flick_business.dto.ProductMaisVendidoDTO;
import br.com.king.flick_business.enums.FormaPagamento;
import br.com.king.flick_business.repository.ExpenseRepository; // Importar ArrayList
import br.com.king.flick_business.repository.ItemVendaRepository; // Usar EnumMap para performance
import br.com.king.flick_business.repository.VendaRepository;

@Service
public class DashboardService {
  private final VendaRepository vendaRepository;
  private final ExpenseRepository expenseRepository;
  private final ItemVendaRepository itemVendaRepository;

  public DashboardService(VendaRepository vendaRepository, ExpenseRepository expenseRepository,
      ItemVendaRepository itemVendaRepository) {
    this.vendaRepository = vendaRepository;
    this.expenseRepository = expenseRepository;
    this.itemVendaRepository = itemVendaRepository;
  }

  @Transactional(readOnly = true)
  public DashboardSummaryDTO getDashboardSummary(ZonedDateTime inicio, ZonedDateTime fim) {
    BigDecimal totalVendasBruto = vendaRepository.sumValorTotalByDataVendaBetween(inicio, fim);
    Long quantidadeVendas = vendaRepository.countVendasByDataVendaBetween(inicio, fim);
    List<Object[]> vendasPorFormaPgtoRaw = vendaRepository.sumValorTotalGroupByFormaPagamentoBetween(inicio, fim);
    BigDecimal totalExpenses = expenseRepository.sumValorByDataExpenseBetween(inicio, fim);
    ProductMaisVendidoDTO productMaisVendido = itemVendaRepository.findProductMaisVendidoBetween(inicio.toLocalDate(),
        fim.toLocalDate());
    List<Object[]> vendasDiariasRaw = vendaRepository.sumValorTotalGroupByDayBetweenNative(inicio, fim);

    // Processar dados e calcular métricas
    BigDecimal lucroBrutoEstimado = totalVendasBruto.subtract(totalExpenses);

    BigDecimal ticketMedio = BigDecimal.ZERO;
    if (quantidadeVendas > 0) {
      ticketMedio = totalVendasBruto.divide(BigDecimal.valueOf(quantidadeVendas), 2, RoundingMode.HALF_UP);
    }

    Map<FormaPagamento, BigDecimal> totalVendasPorFormaPagamento = new EnumMap<>(FormaPagamento.class);
    for (Object[] row : vendasPorFormaPgtoRaw) {
      FormaPagamento forma = (FormaPagamento) row[0];
      BigDecimal total = (BigDecimal) row[1];
      if (forma != null && total != null) {
        totalVendasPorFormaPagamento.put(forma, total);
      }
    }
    for (FormaPagamento forma : FormaPagamento.values()) {
      totalVendasPorFormaPagamento.putIfAbsent(forma, BigDecimal.ZERO);
    }

    List<DataPointDTO> graficoVendasDiarias = new ArrayList<>();
    for (Object[] row : vendasDiariasRaw) {
      LocalDate dia;
      switch (row[0]) {
        case Date date -> dia = date.toLocalDate();
        case LocalDate localDate -> dia = localDate;
        default -> {
          System.err.println("Formato de data inesperado: " + row[0].getClass());
          continue;
        }
      }

      BigDecimal valor = (BigDecimal) row[1];
      if (valor != null) {
        graficoVendasDiarias.add(new DataPointDTO(dia, valor));
      }
    }

    // DTO
    return new DashboardSummaryDTO(
        totalVendasBruto,
        totalVendasPorFormaPagamento,
        totalExpenses,
        lucroBrutoEstimado,
        ticketMedio,
        quantidadeVendas,
        productMaisVendido,
        graficoVendasDiarias);
  }
}