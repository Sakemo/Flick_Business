package br.com.king.flick_business.entity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import br.com.king.flick_business.enums.FormaPagamento;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vendas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venda {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  // -- CALCULADO -- //
  @NotNull(message = "O value total é obrigatório")
  @Column(name = "value_total", nullable = false, precision = 10, scale = 2)
  private BigDecimal valueTotal;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_cliente", nullable = true)
  private Cliente cliente;

  @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private List<ItemVenda> itens = new ArrayList<>();

  // -- INFORMADO -- //
  @NotNull(message = "A forma de pagamento é obrigatória")
  @Enumerated(EnumType.STRING)
  @Column(name = "forma_pagamento", nullable = false, length = 20)
  private FormaPagamento formaPagamento;

  @Column(name = "observacoes", length = 500)
  private String observacoes;

  // -- AUXILIAR -- //
  public void adicionarItem(ItemVenda item) {
    this.itens.add(item);
    item.setVenda(this);
  }

  public void removerItem(ItemVenda item) {
    this.itens.remove(item);
    item.setVenda(null);
  }

  // -- METADADOS -- //
  @NotNull(message = "A data da venda é obrigatória")
  @PastOrPresent(message = "A data da venda não pode ser futura")
  @Column(name = "data_venda", nullable = false, updatable = false)
  @CreationTimestamp
  private ZonedDateTime dataVenda;
}