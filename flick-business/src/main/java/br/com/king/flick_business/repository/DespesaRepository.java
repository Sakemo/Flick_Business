package br.com.king.flick_business.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.com.king.flick_business.entity.Despesa;
import br.com.king.flick_business.enums.TipoDespesa;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {
  List<Despesa> findByTipoDespesa(TipoDespesa tipoDespesa);

  List<Despesa> findByDataDespesaBetweenOrderByDataDespesaDesc(LocalDateTime inicio, LocalDateTime fim);

  List<Despesa> findByNomeContainingIgnoreCase(String nomeDespesa);

  @Query("SELECT SUM(d.valor) FROM Despesa d WHERE d.dataDespesa BETWEEN :inicio AND :fim")
  BigDecimal sumValorByDataDespesaBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

  @Query("SELECT SUM(d.valor) FROM Despesa d WHERE d.tipoDespesa = :tipo AND d.dataDespesa BETWEEN :inicio AND :fim")
  BigDecimal sumValorByTipoDespesaAndDataDespesaBetween(@Param("tipo") TipoDespesa tipo,
      @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}