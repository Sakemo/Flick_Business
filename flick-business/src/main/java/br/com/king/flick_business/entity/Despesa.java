package br.com.king.flick_business.entity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.king.flick_business.enums.TipoDespesa;
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
@Table(name = "despesas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Despesa {
  /*
   * @Id - Identificador único da despesa
   *
   * @Nome - Nome da despesa
   *
   * @Valor - Valor da despesa
   *
   * @DataDespesa - Data da despesa
   *
   * @TipoDespesa - Tipo da despesa (ex: alimentação, transporte, etc)
   *
   * @CreationTimestamp - Data de criação da despesa (METADADOS)
   *
   * @UpdateTimestamp - Data de atualização da despesa (METADADOS)
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Nome da despesa
  @NotBlank(message = "O nome da despesa não pode ser vazio")
  @Size(min = 3, max = 100, message = "O nome da despesa deve ter entre 3 e 100 caracteres")
  @Column(nullable = false, length = 100)
  private String nome;

  // Valor da despesa
  @NotNull(message = "O valor da despesa não pode ser nulo")
  @DecimalMin(value = "0.01", message = "O valor da despesa deve ser maior que zero")
  @Digits(integer = 10, fraction = 2, message = "O valor da despesa deve ter no máximo 10 dígitos inteiros e 2 dígitos decimais")
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal valor;

  // Data da despesa
  @NotNull(message = "A data da despesa não pode ser nula")
  @PastOrPresent(message = "A data da despesa deve ser no passado ou presente")
  @Column(nullable = false, name = "data_despesa")
  private ZonedDateTime dataDespesa;

  // Tipo de despesa
  @NotNull(message = "O tipo de despesa não pode ser nulo")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "tipo_despesa", length = 20)
  private TipoDespesa tipoDespesa;

  // -- METADADOS -- //
  @CreationTimestamp
  @Column(name = "data_criacao", updatable = false, nullable = false)
  private ZonedDateTime dataCriacao;

  @UpdateTimestamp
  @Column(name = "data_atualizacao", nullable = false)
  private ZonedDateTime dataAtualizacao;

}