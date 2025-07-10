package br.com.king.flick_business.entity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import br.com.king.flick_business.enums.PaymentMethod;
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
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  // -- CALCULADO -- //
  @NotNull(message = "O value total é obrigatório")
  @Column(name = "value_total", nullable = false, precision = 10, scale = 2)
  private BigDecimal totalValue;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_customer", nullable = true)
  private Customer customer;

  @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private List<SaleItem> items = new ArrayList<>();

  // -- INFORMADO -- //
  @NotNull(message = "A forma de pagamento é obrigatória")
  @Enumerated(EnumType.STRING)
  @Column(name = "forma_pagamento", nullable = false, length = 20)
  private PaymentMethod paymentMethod;

  @Column(name = "description", length = 500)
  private String description;

  // -- AUXILIAR -- //
  public void adicionarItem(SaleItem item) {
    this.items.add(item);
    item.setSale(this);
  }

  public void removerItem(SaleItem item) {
    this.items.remove(item);
    item.setSale(null);
  }

  // -- METADADOS -- //
  @NotNull(message = "A date da sale é obrigatória")
  @PastOrPresent(message = "A date da sale não pode ser futura")
  @Column(name = "date_sale", nullable = false, updatable = false)
  @CreationTimestamp
  private ZonedDateTime dateSale;
}