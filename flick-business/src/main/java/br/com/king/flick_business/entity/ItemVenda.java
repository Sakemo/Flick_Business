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
@Table(name = "itens_venda")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemVenda {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // -- INFORMADO -- //
  @NotNull(message = "A quantidade é obrigatória.")
  @DecimalMin(value = "0.01", message = "A quantidade deve ser maior que zero.")
  @Digits(integer = 8, fraction = 2, message = "Quantidade inválida: máximo de 8 dígitos inteiros e 2 decimais.")
  @Column(nullable = false, precision = 11, scale = 3)
  private BigDecimal quantidade;

  // -- CALCULADO -- //
  @NotNull(message = "O preço unitário de venda é obrigatório.")
  @DecimalMin(value = "0.01", message = "O preço unitário deve ser maior que zero.")
  @Digits(integer = 8, fraction = 2, message = "Preço inválido: máximo de 8 dígitos inteiros e 2 decimais.")
  @Column(name = "preco_unitario_venda", nullable = false, precision = 12, scale = 2)
  private BigDecimal precoUnitarioVenda;

  // Venda associada
  @NotNull(message = "A venda é obrigatória.")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "venda_id", nullable = false)
  private Venda venda;

  // Produto associado
  @NotNull(message = "O produto é obrigatório.")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "produto_id", nullable = false)
  private Produto produto;

  // -- AUXILIAR -- //
  @Transient
  public BigDecimal getValorTotalItem() {
    if (quantidade != null && precoUnitarioVenda != null) {
      return quantidade.multiply(precoUnitarioVenda);
    }
    return BigDecimal.ZERO;
  }

}