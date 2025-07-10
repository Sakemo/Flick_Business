package br.com.king.flick_business.repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.entity.Expense;
import br.com.king.flick_business.enums.TipoExpense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor {
        List<Expense> findByTipoExpense(TipoExpense tipoExpense);

        List<Expense> findByDataExpenseBetweenOrderByDataExpenseDesc(ZonedDateTime inicio, ZonedDateTime fim);

        List<Expense> findByNameContainingIgnoreCase(String nameExpense);

        List<Expense> findByTipoExpenseAndDataExpenseBetweenOrderByDataExpenseDesc(TipoExpense tipoExpense,
                        ZonedDateTime inicio, ZonedDateTime fim);

        @Query("SELECT SUM(d.valor) FROM Expense d WHERE d.dataExpense BETWEEN :inicio AND :fim")
        BigDecimal sumValorByDataExpenseBetween(@Param("inicio") ZonedDateTime inicio, @Param("fim") ZonedDateTime fim);

        @Query("SELECT SUM(d.valor) FROM Expense d WHERE d.tipoExpense = :tipo AND d.dataExpense BETWEEN :inicio AND :fim")
        BigDecimal sumValorByTipoExpenseAndDataExpenseBetween(@Param("tipo") TipoExpense tipo,
                        @Param("inicio") ZonedDateTime inicio, @Param("fim") ZonedDateTime fim);

        // v.06.05.25
        // -- QUERRY PARA DASHBOARD -- //
        @Query("SELECT COALESCE(SUM(d.valor), 0) FROM Expense d WHERE d.dataExpense BETWEEN :inicio AND :fim")
        BigDecimal sumValorByDataExpenseBetweenDashboard(@Param("inicio") ZonedDateTime inicio,
                        @Param("fim") ZonedDateTime fim);
}