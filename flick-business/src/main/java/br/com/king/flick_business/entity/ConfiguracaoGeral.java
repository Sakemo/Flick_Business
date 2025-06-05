package br.com.king.flick_business.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuracoes_gerais")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracaoGeral {
  @Id
  @Builder.Default
  private Long id = 1L;

  @Digits(integer = 3, fraction = 2, message = "Inválido")
  private BigDecimal taxaJurosAtraso;

  @Min(value = 1, message = "Prazo de pagamento deve ser de no mínimo 1 mês.")
  @Column(name = "prazo_pagamento_fiado")
  private Integer prazoPagamentoFiado;

  @UpdateTimestamp
  @Column(name = "data_atualizacao", nullable = false)
  private LocalDateTime dataAtualizacao;

  // TODO: configurar resto do código para lidar com o nome do negócio
  @Column(name = "nome_negocio", nullable = true, length = 100)
  private String nomeNegocio;
}