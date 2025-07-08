package br.com.king.flick_business.entity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.king.flick_business.enums.TipoUnidadeVenda;
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

@Data
@Entity
@Table(name = "produtos")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome do Produto é obrigatório")
    @Size(min = 2, max = 100, message = "Nome do produto deter ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100, unique = true)
    private String nome;

    @Size(max = 300, message = "Descrição não deve exceder 500 caracteres")
    @Column(length = 300)
    private String descricao;

    @Size(max = 50, message = "Código não deve exceder 50 caracteres")
    @Column(name = "barcode", length = 50, unique = true)
    private String codigoBarras;

    @DecimalMin(value = "0.0", inclusive = true, message = "A quantidade não pode ser negativa")
    @Digits(integer = 10, fraction = 3, message = "Formato inválido")
    @Column(name = "quantidade_estoque", precision = 13, scale = 3, nullable = false)
    private BigDecimal quantidadeEstoque;

    @NotNull(message = "Preço de venda é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço de venda deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Preço de venda inválido")
    @Column(name = "preco_venda", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoVenda;

    @DecimalMin(value = "0.0", message = "Preço de custo não pode ser negativo")
    @Digits(integer = 8, fraction = 2, message = "Preço de custo inválido")
    @Column(name = "preco_custo_unitario", precision = 10, scale = 2)
    private BigDecimal precoCustoUnitario;

    @NotNull(message = "Unidade de venda é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_unidade_venda", nullable = false, length = 10)
    private TipoUnidadeVenda tipoUnidadeVenda;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativo = true;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private ZonedDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private ZonedDateTime atualizadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id", nullable = true)
    private Fornecedor fornecedor;
}
