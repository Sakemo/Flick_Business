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

import br.com.king.flick_business.entity.Sale;
import br.com.king.flick_business.enums.PaymentMethod;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
        // Busca sales de um customer específico, ordenadas da mais recente para a mais
        // antiga
        List<Sale> findByCustomerIdOrderByDateSaleDesc(Long customerId);

        // Busca uma sale pelo ID, incluindo seus items e o customer associado (fetch
        // join)
        @Query("SELECT DISTINCT v FROM Sale v " + "LEFT JOIN FETCH v.items i " + "LEFT JOIN FETCH v.customer c "
                        + "WHERE v.id = :id")
        Optional<Sale> findByIdComItemsECustomer(Long id);

        // Busca todas as sales, incluindo customer e items associados, com ordenação
        @Query("SELECT DISTINCT v FROM Sale v LEFT JOIN FETCH v.customer c LEFT JOIN FETCH v.items i")
        List<Sale> findAllWithCustomerAndItems(Sort sort);

        // Busca sales filtrando por período, customer e forma de pagamento, incluindo
        // customer e items (fetch join)
        @Query("SELECT DISTINCT v FROM Sale v LEFT JOIN FETCH v.customer c LEFT JOIN FETCH v.items i WHERE "
                        + "(v.dateSale >= :start) AND "
                        + "(v.dateSale <= :end) AND "
                        + "(:customerId IS NULL OR c.id = :customerId) AND "
                        + "(:paymentMethod IS NULL OR v.paymentMethod = :paymentMethod) AND "
                        + "(:productId IS NULL OR i.product.id = :productId)")
        Page<Sale> findSalesComFiltros(
                        @Param("start") ZonedDateTime start,
                        @Param("end") ZonedDateTime end,
                        @Param("customerId") Long customerId,
                        @Param("paymentMethod") PaymentMethod paymentMethod,
                        @Param("productId") Long productId,
                        Pageable pageable);

        // Retorna uma lista de Object[], onde cada array é [Date (como String), Total
        // (BigDecimal)]
        @Query("SELECT FUNCTION('TO_CHAR', v.dateSale, 'YYYY-MM-DD'), SUM(v.totalValue) " +
                        "FROM Sale v WHERE     " + "v.dateSale BETWEEN :start AND :end AND "
                        + "(:customerId IS NULL OR v.customer.id = :customerId) AND "
                        + "(:paymentMethod IS NULL OR v.paymentMethod = :paymentMethod) AND "
                        + "(:productId IS NULL OR EXISTS (SELECT 1 FROM SaleItem i WHERE i.sale = v AND i.product.id = :productId)) "
                        + "GROUP BY FUNCTION('TO_CHAR', v.dateSale, 'YYYY-MM-DD')")
        List<Object[]> sumTotalGroupByDay(@Param("start") ZonedDateTime start, @Param("end") ZonedDateTime end,
                        @Param("customerId") Long customerId, @Param("paymentMethod") PaymentMethod paymentMethod,
                        @Param("productId") Long productId);

        // Retorna uma lista de Object[], onde cada array é [ID do Customer (Long), Name
        // do Customer (String), Total (BigDecimal)]
        @Query("SELECT v.customer.id, v.customer.name, SUM(v.totalValue) " +
                        "FROM Sale v WHERE " +
                        "v.dateSale BETWEEN :start AND :end AND " +
                        "(:customerId IS NULL OR v.customer.id = :customerId) AND " +
                        "(:paymentMethod IS NULL OR v.paymentMethod = :paymentMethod) AND " +
                        "(:productId IS NULL OR EXISTS (SELECT 1 FROM SaleItem i WHERE i.sale = v AND i.product.id = :productId)) "
                        +
                        "GROUP BY v.customer.id, v.customer.name")
        List<Object[]> sumTotalGroupByCustomer(@Param("start") ZonedDateTime start, @Param("end") ZonedDateTime end,
                        @Param("customerId") Long customerId, @Param("paymentMethod") PaymentMethod paymentMethod,
                        @Param("productId") Long productId);

        @Query("SELECT COALESCE(SUM(v.totalValue), 0) FROM Sale v " +
                        "WHERE v.dateSale BETWEEN :start AND :end " +
                        "AND (:customerId IS NULL OR v.customer.id = :customerId) " +
                        "AND (:paymentMethod IS NULL OR v.paymentMethod = :paymentMethod) " +
                        "AND (:productId IS NULL OR EXISTS (SELECT 1 FROM SaleItem i WHERE i.sale = v AND i.product.id = :productId))")
        BigDecimal sumTotalValueComFiltros(
                        @Param("start") ZonedDateTime start,
                        @Param("end") ZonedDateTime end,
                        @Param("customerId") Long clientId,
                        @Param("paymentMethod") PaymentMethod paymentMethod,
                        @Param("productId") Long productId);

        // =========================
        // MÉTODOS PARA RELATÓRIOS
        // =========================

        // Soma o value total das sales em um determinado período
        @Query("SELECT COALESCE(SUM(v.totalValue), 0) FROM Sale v WHERE v.dateSale BETWEEN :start AND :end")
        BigDecimal sumTotalValueByDateSaleBetween(@Param("start") ZonedDateTime start,
                        @Param("end") ZonedDateTime end);

        // Conta o número de sales realizadas em um determinado período
        @Query("SELECT COUNT(v) FROM Sale v WHERE v.dateSale BETWEEN :start AND :end")
        Long countSalesByDateSaleBetween(@Param("start") ZonedDateTime start,
                        @Param("end") ZonedDateTime end);

        // Agrupa e soma o value total das sales por forma de pagamento em um período
        // Retorna uma lista de Object[]: [PaymentMethod, BigDecimal]
        @Query("SELECT v.paymentMethod, SUM(v.totalValue) FROM Sale v WHERE v.dateSale BETWEEN :start AND :end GROUP BY v.paymentMethod")
        List<Object[]> sumTotalValueGroupByPaymentMethodBetween(@Param("start") ZonedDateTime start,
                        @Param("end") ZonedDateTime end);

        // Retorna dados diários para gráficos: soma do value total das sales por dia
        // no período informado
        // Cada Object[] contém: [date (DATE), soma (BigDecimal)]
        @Query(value = "SELECT CAST(v.date_sale AS DATE) as dias, SUM(v.value_total) as total " +
                        "FROM sales v " +
                        "WHERE v.date_sale BETWEEN :start AND :end " +
                        "GROUP BY dias " +
                        "ORDER BY dias ASC", nativeQuery = true)
        List<Object[]> sumTotalValueGroupByDayBetweenNative(@Param("start") ZonedDateTime start,
                        @Param("end") ZonedDateTime end);
}
