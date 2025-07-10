package br.com.king.flick_business.entity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import br.com.king.flick_business.enums.UnitOfSale;
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
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name do Product é obrigatório")
    @Size(min = 2, max = 100, message = "Name do product deter ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Size(max = 300, message = "Descrição não deve exceder 500 caracteres")
    @Column(length = 300)
    private String description;

    @Size(max = 50, message = "Código não deve exceder 50 caracteres")
    @Column(name = "barcode", length = 50, unique = true)
    private String barcode;

    @DecimalMin(value = "0.0", inclusive = true, message = "A quantidade não pode ser negativa")
    @Digits(integer = 10, fraction = 3, message = "Formato inválido")
    @Column(name = "quantidade_estoque", precision = 13, scale = 3, nullable = false)
    private BigDecimal stockQuantity;

    @NotNull(message = "Preço de venda é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço de venda deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Preço de venda inválido")
    @Column(name = "preco_venda", nullable = false, precision = 10, scale = 2)
    private BigDecimal salePrice;

    @DecimalMin(value = "0.0", message = "Preço de custo não pode ser negactive")
    @Digits(integer = 8, fraction = 2, message = "Preço de custo inválido")
    @Column(name = "preco_custo_unitario", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @NotNull(message = "Unidade de venda é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_unidade_venda", nullable = false, length = 10)
    private UnitOfSale unitOfSale;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private ZonedDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = true)
    private Provider provider;

}
