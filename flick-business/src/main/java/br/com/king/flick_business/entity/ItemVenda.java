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
  @NotNull(message = "Obrigatório")
  @DecimalMin(value = "0.01", message = "Deve ser maior que 0")
  @Digits(integer = 8, fraction = 2, message = "Inválido")
  @Column(nullable = false, precision = 11, scale = 3)
  private BigDecimal quantidade;

  // -- CALCULADO -- //
  @NotNull(message = "Obrigatório")
  @DecimalMin(value = "0.01", message = "Deve ser maior que 0")
  @Digits(integer = 8, fraction = 2, message = "Inválido")
  @Column(name = "preco_unitario_venda", nullable = false, precision = 12, scale = 2)
  private BigDecimal precoUnitarioVenda;

  @NotNull(message = "Obrigatório")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "venda_id", nullable = false)
  private Venda venda;

  @NotNull(message = "Obrigatório")
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