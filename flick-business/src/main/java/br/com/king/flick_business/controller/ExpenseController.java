package br.com.king.flick_business.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import br.com.king.flick_business.dto.request.ExpenseRequestDTO;
import br.com.king.flick_business.dto.response.ExpenseResponseDTO;
import br.com.king.flick_business.service.ExpenseService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
  private final ExpenseService expenseService;

  public ExpenseController(ExpenseService expenseService) {
    this.expenseService = expenseService;
  }

  // Criar Expense
  @PostMapping
  public ResponseEntity<ExpenseResponseDTO> criarExpense(
      @Valid @RequestBody ExpenseRequestDTO dto,
      UriComponentsBuilder uriBuilder) {
    ExpenseResponseDTO expenseSalva = expenseService.saveExpense(dto);

    URI uri = uriBuilder.path("/api/expenses/{id}").buildAndExpand(expenseSalva.id()).toUri();

    return ResponseEntity.created(uri).body(expenseSalva);
  }

  // List Expenses
  @GetMapping
  public ResponseEntity<List<ExpenseResponseDTO>> listExpenses(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end,
      @RequestParam(name = "expenseType", required = false) String expenseType,
      @RequestParam(required = false) String name) {

    List<ExpenseResponseDTO> expenses = expenseService.listExpenses(
        start != null ? start : null,
        end != null ? end : null,
        expenseType, name);

    return ResponseEntity.ok(expenses);
  }

  // Total Expenses
  @GetMapping("/total")
  public ResponseEntity<BigDecimal> getTotalExpenses(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime begin,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end) {
    BigDecimal total = expenseService.calcTotalExpensesPerPeriod(
        begin != null ? begin.toLocalDateTime() : null,
        end != null ? end.toLocalDateTime() : null);
    return ResponseEntity.ok(total);
  }

  // Buscar Expense por ID
  @GetMapping("/{id}")
  public ResponseEntity<ExpenseResponseDTO> buscarExpensePorId(@PathVariable Long id) {
    ExpenseResponseDTO expense = expenseService.buscarExpensePorId(id);
    return ResponseEntity.ok(expense);
  }

  // Atualizar Expense
  @PutMapping("/{id}")
  public ResponseEntity<ExpenseResponseDTO> updateExpense(
      @PathVariable Long id,
      @Valid @RequestBody ExpenseRequestDTO dto) {
    ExpenseResponseDTO expenseAtualizada = expenseService.updateExpense(id, dto);
    return ResponseEntity.ok(expenseAtualizada);
  }

  // Deletar Expense
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
    expenseService.deleteExpense(id);
    return ResponseEntity.noContent().build();
  }
}