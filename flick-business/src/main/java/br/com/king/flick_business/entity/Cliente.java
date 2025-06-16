package br.com.king.flick_business.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

  // Chave Primaria
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  // Nome
  @NotBlank(message = "O nome é obrigatório.")
  @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres.")
  @Column(nullable = false, length = 100, columnDefinition = "VARCHAR(100)")
  private String nome;

  // CPF
  @Size(max = 11, message = "O CPF deve conter exatamente 11 dígitos numéricos.")
  @Column(length = 11, unique = true)
  private String cpf;

  // Telefone
  @Size(max = 11, message = "O telefone deve conter até 11 dígitos, incluindo o DDD.")
  @Column(length = 11)
  private String telefone;

  // Endereço
  @Size(max = 150, message = "O endereço deve ter no máximo 150 caracteres.")
  @Column(length = 150)
  private String endereco;

  // Controle de Fiado
  @NotNull(message = "O campo 'Controle de Fiado' é obrigatório.")
  @Column(name = "controle_fiado", nullable = false)
  @Builder.Default
  private Boolean controleFiado = false;

  // Limite de Fiado
  @DecimalMin(value = "0.00", message = "O limite de fiado deve ser maior ou igual a 0.")
  @Digits(integer = 8, fraction = 2, message = "Formato inválido: máximo de 8 dígitos inteiros e 2 decimais.")
  @Column(name = "limite_fiado", precision = 10, scale = 2)
  private BigDecimal limiteFiado;

  // Saldo Devedor
  @Digits(integer = 8, fraction = 2, message = "Formato inválido: máximo de 8 dígitos inteiros e 2 decimais.")
  @Column(name = "saldo_devedor", precision = 10, scale = 2, nullable = false)
  @Builder.Default
  private BigDecimal saldoDevedor = BigDecimal.ZERO;

  // Data de Última Compra Fiado
  @Column(name = "data_ultima_compra_fiado")
  private LocalDateTime dataUltimaCompraFiado;

  // Metadados
  @CreationTimestamp
  @Column(name = "data_cadastro", updatable = false)
  private LocalDateTime dataCadastro;

  @UpdateTimestamp
  @Column(name = "data_atualizacao", nullable = false)
  private LocalDateTime dataAtualizacao;

  // Exclusão Lógica
  @Builder.Default
  @Column(name = "ativo", nullable = false)
  private Boolean ativo = true;

}