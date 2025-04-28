package br.com.king.flick_business.entity;
import br.com.king.flick_business.enums.TipoUnidadeVenda;
// JAKARTA SETUP
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
// LOMBOK SETUP
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

// HIBERNATE SETUP
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
// MATH SETUP
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @Column(name = "quantidade_estoque", precision = 13, scale = 3)
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
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;
}
