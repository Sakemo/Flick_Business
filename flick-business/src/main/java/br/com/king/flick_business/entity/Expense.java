package br.com.king.flick_business.entity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.king.flick_business.enums.ExpenseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {
  /*
   * @Id - Identificador único da expense
   *
   * @Name - Name da expense
   *
   * @Value - Value da expense
   *
   * @DataExpense - Data da expense
   *
   * @ExpenseType - Tipo da expense (ex: alimentação, transporte, etc)
   *
   * @CreationTimestamp - Data de criação da expense (METADADOS)
   *
   * @UpdateTimestamp - Data de atualização da expense (METADADOS)
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Name da expense
  @NotBlank(message = "O name da expense não pode ser vazio")
  @Size(min = 3, max = 100, message = "O name da expense deve ter entre 3 e 100 caracteres")
  @Column(nullable = false, length = 100)
  private String name;

  // Value da expense
  @NotNull(message = "O value da expense não pode ser nulo")
  @DecimalMin(value = "0.01", message = "O value da expense deve ser maior que zero")
  @Digits(integer = 10, fraction = 2, message = "O value da expense deve ter no máximo 10 dígitos inteiros e 2 dígitos decimais")
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal value;

  // Data da expense
  @NotNull(message = "A data da expense não pode ser nula")
  @PastOrPresent(message = "A data da expense deve ser no passado ou presente")
  @Column(nullable = false, name = "data_expense")
  private ZonedDateTime dateExpense;

  // Tipo de expense
  @NotNull(message = "O tipo de expense não pode ser nulo")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "tipo_expense", length = 20)
  private ExpenseType expenseType;

  // -- METADADOS -- //
  @CreationTimestamp
  @Column(name = "data_criacao", updatable = false, nullable = false)
  private ZonedDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "data_atualizacao", nullable = false)
  private ZonedDateTime updatedAt;

}