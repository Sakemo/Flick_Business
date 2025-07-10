package br.com.king.flick_business.entity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

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
public class GeneralSettings {
  @Id
  @Builder.Default
  private Long id = 1L;

  @Digits(integer = 3, fraction = 2, message = "Inválido")
  private BigDecimal taxaJurosAtraso;

  @Min(value = 1, message = "Prazo de pagamento deve ser de no mínimo 1 mês.")
  @Column(name = "prazo_pagamento_fiado")
  private Integer prazoPagamentoFiado;

  @UpdateTimestamp
  @Column(name = "date_update", nullable = false)
  private ZonedDateTime updatedAt;

  // TODO: configurar resto do código para lidar com o name do negócio
  @Column(name = "name_negocio", nullable = true, length = 100)
  private String nameNegocio;
}