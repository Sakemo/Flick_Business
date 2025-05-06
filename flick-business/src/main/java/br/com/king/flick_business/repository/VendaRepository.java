package br.com.king.flick_business.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.entity.Venda;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {
  List<Venda> findByClienteIdOrderByDataVendaDesc(Long clienteId);

  List<Venda> findByDataVendaBetween(LocalDateTime inicio, LocalDateTime fim);

  @Query("SELECT DISTINCT v FROM Venda v JOIN FETCH v.itens i JOIN FETCH v.cliente c WHERE v.id = :id")
  Optional<Venda> findByIdComItensECliente(Long id);

  @Query("SELECT v FROM Venda v JOIN FETCH v.cliente c")
  List<Venda> FindAllComCliente();

  // v.06.05.25 update
  // -- QUERIES PARA RELATÓRIOS -- //

  // Soma o valor total das vendas no período
  @Query("SELECT COALESCE(SUM(v.valorTotal), 0) FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim")
  BigDecimal sumValorTotalByDataVendaBetween(@Param("inicio") LocalDateTime inicio,
      @Param("fim") LocalDateTime fim);

  // Conta o numero de vendas no período
  @Query("SELECT COUNT(v) FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim")
  Long countVendasByDataVendaBetween(@Param("inicio") LocalDateTime inicio,
      @Param("fim") LocalDateTime fim);

  // Agrupa a soma de valorTotal por forma de pagamento no período
  // Retorna uma lista de Object[], onde cada Object[] tem [FormaPagamento,
  // BigDecimal]
  @Query("SELECT v.formaPagamento, SUM(v.valorTotal) FROM Venda v WHERE v.dataVenda BETWEEN :inicio AND :fim GROUP BY v.formaPagamento")
  List<Object[]> sumValorTotalGroupByFormaPagamentoBetween(@Param("inicio") LocalDateTime inicio,
      @Param("fim") LocalDateTime fim);

  // Query para dados do gráfico diário
  // Converte dataVenda para DATE e agrupa/soma
  @Query(value = "SELECT CAST(v.data_venda AS DATE) as dias, SUM(v.valor_total) as total " +
      "FROM vendas v " +
      "WHERE v.data_venda BETWEEN :inicio AND :fim " +
      "GROUP BY dias " +
      "ORDER BY dias ASC", nativeQuery = true)
  List<Object[]> sumValorTotalGroupByDayBetweenNative(@Param("inicio") LocalDateTime inicio,
      @Param("fim") LocalDateTime fim);

}
