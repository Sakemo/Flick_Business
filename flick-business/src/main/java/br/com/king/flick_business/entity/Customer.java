package br.com.king.flick_business.entity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

  // Chave Primaria
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  // Name
  @NotBlank(message = "O name é obrigatório.")
  @Size(min = 2, max = 100, message = "O name deve ter entre 2 e 100 caracteres.")
  @Column(nullable = false, length = 100, columnDefinition = "VARCHAR(100)")
  private String name;

  // CPF
  @Size(max = 11, message = "O CPF deve conter exatamente 11 dígitos numéricos.")
  @Column(length = 11, unique = true)
  private String taxId;

  // Phone
  @Size(max = 11, message = "O phone deve conter até 11 dígitos, incluindo o DDD.")
  @Column(length = 11)
  private String phone;

  // Endereço
  @Size(max = 150, message = "O endereço deve ter no máximo 150 caracteres.")
  @Column(length = 150)
  private String adress;

  // Controle de Fiado
  @NotNull(message = "O campo 'Controle de Fiado' é obrigatório.")
  @Column(name = "controle_fiado", nullable = false)
  @Builder.Default
  private Boolean creditManagement = false;

  // Limite de Fiado
  @DecimalMin(value = "0.00", message = "O limite de fiado deve ser maior ou igual a 0.")
  @Digits(integer = 8, fraction = 2, message = "Formato inválido: máximo de 8 dígitos inteiros e 2 decimais.")
  @Column(name = "limite_fiado", precision = 10, scale = 2)
  private BigDecimal creditLimit;

  // Saldo Devedor
  @Digits(integer = 8, fraction = 2, message = "Formato inválido: máximo de 8 dígitos inteiros e 2 decimais.")
  @Column(name = "saldo_devedor", precision = 10, scale = 2, nullable = false)
  @Builder.Default
  private BigDecimal debitBalance = BigDecimal.ZERO;

  // Date de Última Compra Fiado
  @Column(name = "date_ultima_compra_fiado")
  private ZonedDateTime dateLastPurchaseOnCredit;

  // Metadados
  @CreationTimestamp
  @Column(name = "date_register", updatable = false)
  private ZonedDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "date_update", nullable = false)
  private ZonedDateTime updatedAt;

  // Exclusão Lógica
  @Builder.Default
  @Column(name = "active", nullable = false)
  private Boolean active = true;

}