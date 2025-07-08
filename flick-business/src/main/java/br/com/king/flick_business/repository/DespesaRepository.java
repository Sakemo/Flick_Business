package br.com.king.flick_business.repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.entity.Despesa;
import br.com.king.flick_business.enums.TipoDespesa;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {
        List<Despesa> findByTipoDespesa(TipoDespesa tipoDespesa);

        List<Despesa> findByDataDespesaBetweenOrderByDataDespesaDesc(ZonedDateTime inicio, ZonedDateTime fim);

        List<Despesa> findByNomeContainingIgnoreCase(String nomeDespesa);

        List<Despesa> findByTipoDespesaAndDataDespesaBetweenOrderByDataDespesaDesc(TipoDespesa tipoDespesa,
                        ZonedDateTime inicio, ZonedDateTime fim);

        @Query("SELECT SUM(d.valor) FROM Despesa d WHERE d.dataDespesa BETWEEN :inicio AND :fim")
        BigDecimal sumValorByDataDespesaBetween(@Param("inicio") ZonedDateTime inicio, @Param("fim") ZonedDateTime fim);

        @Query("SELECT SUM(d.valor) FROM Despesa d WHERE d.tipoDespesa = :tipo AND d.dataDespesa BETWEEN :inicio AND :fim")
        BigDecimal sumValorByTipoDespesaAndDataDespesaBetween(@Param("tipo") TipoDespesa tipo,
                        @Param("inicio") ZonedDateTime inicio, @Param("fim") ZonedDateTime fim);

        // v.06.05.25
        // -- QUERRY PARA DASHBOARD -- //
        @Query("SELECT COALESCE(SUM(d.valor), 0) FROM Despesa d WHERE d.dataDespesa BETWEEN :inicio AND :fim")
        BigDecimal sumValorByDataDespesaBetweenDashboard(@Param("inicio") ZonedDateTime inicio,
                        @Param("fim") ZonedDateTime fim);
}