package br.com.king.flick_business.repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.entity.Venda;
import br.com.king.flick_business.enums.FormaPagamento;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {
        // Busca vendas de um cliente específico, ordenadas da mais recente para a mais
        // antiga
        List<Venda> findByClienteIdOrderByDataVendaDesc(Long clienteId);

        // Busca uma venda pelo ID, incluindo seus itens e o cliente associado (fetch
        // join)
        @Query("SELECT DISTINCT v FROM Venda v " + "LEFT JOIN FETCH v.itens i " + "LEFT JOIN FETCH v.cliente c "
                        + "WHERE v.id = :id")
        Optional<Venda> findByIdComItensECliente(Long id);

        // Busca todas as vendas, incluindo cliente e itens associados, com ordenação
        @Query("SELECT DISTINCT v FROM Venda v LEFT JOIN FETCH v.cliente c LEFT JOIN FETCH v.itens i")
        List<Venda> findAllWithClienteAndItens(Sort sort);

        // Busca vendas filtrando por período, cliente e forma de pagamento, incluindo
        // cliente e itens (fetch join)
        @Query("SELECT DISTINCT v FROM Venda v LEFT JOIN FETCH v.cliente c LEFT JOIN FETCH v.itens i WHERE "
                        + "(v.dataVenda >= :inicio) AND "
                        + "(v.dataVenda <= :fim) AND "
                        + "(:clienteId IS NULL OR c.id = :clienteId) AND "
                        + "(:formaPagamento IS NULL OR v.formaPagamento = :formaPagamento) AND "
                        + "(:productId IS NULL OR i.product.id = :productId)")
        Page<Venda> findVendasComFilters(
                        @Param("inicio") ZonedDateTime inicio,
                        @Param("fim") ZonedDateTime fim,
                        @Param("clienteId") Long clienteId,
                        @Param("formaPagamento") FormaPagamento formaPagamento,
                        @Param("productId") Long productId,
                        Pageable pageable);

        // Retorna uma lista de Object[], onde cada array é [Data (como String), Total
        // (BigDecimal)]
        @Query("SELECT FUNCTION('TO_CHAR', v.dataVenda, 'YYYY-MM-DD'), SUM(v.valueTotal) " +
                        "FROM Venda v WHERE     " + "v.dataVenda BETWEEN :inicio AND :fim AND "
                        + "(:clienteId IS NULL OR v.cliente.id = :clienteId) AND "
                        + "(:formaPagamento IS NULL OR v.formaPagamento = :formaPagamento) AND "
                        + "(:productId IS NULL OR EXISTS (SELECT 1 FROM ItemVenda i WHERE i.venda = v AND i.product.id = :productId)) "
                        + "GROUP BY FUNCTION('TO_CHAR', v.dataVenda, 'YYYY-MM-DD')")
        List<Object[]> sumTotalGroupByDay(@Param("inicio") ZonedDateTime inicio, @Param("fim") ZonedDateTime fim,
                        @Param("clienteId") Long clienteId, @Param("formaPagamento") FormaPagamento formaPagamento,
                        @Param("productId") Long productId);

        // Retorna uma lista de Object[], onde cada array é [ID do Cliente (Long), Name
        // do Cliente (String), Total (BigDecimal)]
        @Query("SELECT v.cliente.id, v.cliente.name, SUM(v.valueTotal) " +
                        "FROM Venda v WHERE " +
                        "v.dataVenda BETWEEN :inicio AND :fim AND " +
                        "(:clienteId IS NULL OR v.cliente.id = :clienteId) AND " +
                        "(:formaPagamento IS NULL OR v.formaPagamento = :formaPagamento) AND " +
                        "(:productId IS NULL OR EXISTS (SELECT 1 FROM ItemVenda i WHERE i.venda = v AND i.product.id = :productId)) "
                        +
                        "GROUP BY v.cliente.id, v.cliente.name")
        List<Object[]> sumTotalGroupByCliente(@Param("inicio") ZonedDateTime inicio, @Param("fim") ZonedDateTime fim,
                        @Param("clienteId") Long clienteId, @Param("formaPagamento") FormaPagamento formaPagamento,
                        @Param("productId") Long productId);

        @Query("SELECT COALESCE(SUM(v.valueTotal), 0) FROM Venda v " +
                        "WHERE v.dataVenda BETWEEN :inicio AND :fim " +
                        "AND (:clienteId IS NULL OR v.cliente.id = :clienteId) " +
                        "AND (:formaPagamento IS NULL OR v.formaPagamento = :formaPagamento) " +
                        "AND (:productId IS NULL OR EXISTS (SELECT 1 FROM ItemVenda i WHERE i.venda = v AND i.product.id = :productId))")
        BigDecimal sumValueTotalComFilters(
                        @Param("inicio") ZonedDateTime inicio,
                        @Param("fim") ZonedDateTime fim,
                        @Param("clienteId") Long clientId,
                        @Param("formaPagamento") FormaPagamento formaPagamento,
                        @Param("productId") Long productId);

        // =========================
        // MÉTODOS PARA RELATÓRIOS
        // =========================

        // Soma o value total das vendas em um determinado período
        @Query("SELECT COALESCE(SUM(v.valueTotal), 0) FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim")
        BigDecimal sumValueTotalByDataVendaBetween(@Param("inicio") ZonedDateTime inicio,
                        @Param("fim") ZonedDateTime fim);

        // Conta o número de vendas realizadas em um determinado período
        @Query("SELECT COUNT(v) FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim")
        Long countVendasByDataVendaBetween(@Param("inicio") ZonedDateTime inicio,
                        @Param("fim") ZonedDateTime fim);

        // Agrupa e soma o value total das vendas por forma de pagamento em um período
        // Retorna uma lista de Object[]: [FormaPagamento, BigDecimal]
        @Query("SELECT v.formaPagamento, SUM(v.valueTotal) FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim GROUP BY v.formaPagamento")
        List<Object[]> sumValueTotalGroupByFormaPagamentoBetween(@Param("inicio") ZonedDateTime inicio,
                        @Param("fim") ZonedDateTime fim);

        // Retorna dados diários para gráficos: soma do value total das vendas por dia
        // no período informado
        // Cada Object[] contém: [data (DATE), soma (BigDecimal)]
        @Query(value = "SELECT CAST(v.data_venda AS DATE) as dias, SUM(v.value_total) as total " +
                        "FROM vendas v " +
                        "WHERE v.data_venda BETWEEN :inicio AND :fim " +
                        "GROUP BY dias " +
                        "ORDER BY dias ASC", nativeQuery = true)
        List<Object[]> sumValueTotalGroupByDayBetweenNative(@Param("inicio") ZonedDateTime inicio,
                        @Param("fim") ZonedDateTime fim);
}
