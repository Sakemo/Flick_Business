package br.com.king.flick_business.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "items_sale")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // -- INFORMADO -- //
  @NotNull(message = "A quantidade é obrigatória.")
  @DecimalMin(value = "0.001", message = "A quantidade deve ser maior que zero.")
  @Digits(integer = 8, fraction = 3, message = "Quantidade inválida: máximo de 8 dígitos inteiros e 3 decimais.")
  @Column(nullable = false, precision = 11, scale = 3)
  private BigDecimal quantidade;

  // -- CALCULADO -- //
  @NotNull(message = "O preço unitário de sale é obrigatório.")
  @DecimalMin(value = "0.01", message = "O preço unitário deve ser maior que zero.")
  @Digits(integer = 8, fraction = 2, message = "Preço inválido: máximo de 8 dígitos inteiros e 2 decimais.")
  @Column(name = "price_unitario_sale", nullable = false, precision = 12, scale = 2)
  private BigDecimal priceUnitarioSale;

  // Sale associada
  @NotNull(message = "A sale é obrigatória.")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sale_id", nullable = false)
  private Sale sale;

  // Product associado
  @NotNull(message = "O product é obrigatório.")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  // -- AUXILIAR -- //
  @Transient
  public BigDecimal getTotalValueItem() {
    if (quantidade != null && priceUnitarioSale != null) {
      return quantidade.multiply(priceUnitarioSale);
    }
    return BigDecimal.ZERO;
  }

}