package br.com.king.flick_business.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import br.com.king.flick_business.entity.Expense;
import br.com.king.flick_business.enums.TipoExpense;

public record ExpenseResponseDTO(
    Long id,
    String name,
    BigDecimal valor,
    ZonedDateTime dataExpense,
    TipoExpense tipoExpense,
    ZonedDateTime dataCriacao,
    ZonedDateTime dataAtualizacao) {

  public ExpenseResponseDTO(Expense expense) {
    this(
        expense.getId(),
        expense.getName(),
        expense.getValor(),
        expense.getDataExpense(),
        expense.getTipoExpense(),
        expense.getDataCriacao(),
        expense.getDataAtualizacao());
  }
}