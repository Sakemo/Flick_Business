package br.com.king.flick_business.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import br.com.king.flick_business.entity.Expense;
import br.com.king.flick_business.enums.ExpenseType;

public record ExpenseResponseDTO(
    Long id,
    String name,
    BigDecimal value,
    ZonedDateTime dateExpense,
    ExpenseType expenseType,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt) {

  public ExpenseResponseDTO(Expense expense) {
    this(
        expense.getId(),
        expense.getName(),
        expense.getValue(),
        expense.getDateExpense(),
        expense.getExpenseType(),
        expense.getCreatedAt(),
        expense.getUpdatedAt());
  }
}