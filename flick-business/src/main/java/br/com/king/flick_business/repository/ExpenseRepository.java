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
import br.com.king.flick_business.enums.ExpenseType;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor {
        List<Expense> findByExpenseType(ExpenseType expenseType);

        List<Expense> findByDataExpenseBetweenOrderByDataExpenseDesc(ZonedDateTime inicio, ZonedDateTime fim);

        List<Expense> findByNameContainingIgnoreCase(String nameExpense);

        List<Expense> findByExpenseTypeAndDateExpenseBetweenOrderByDateExpenseDesc(ExpenseType expenseType,
                        ZonedDateTime inicio, ZonedDateTime fim);

        @Query("SELECT SUM(d.value) FROM Expense d WHERE d.dateExpense BETWEEN :inicio AND :fim")
        BigDecimal sumValueByDataExpenseBetween(@Param("inicio") ZonedDateTime inicio, @Param("fim") ZonedDateTime fim);

        @Query("SELECT SUM(d.value) FROM Expense d WHERE d.expenseType = :tipo AND d.dateExpense BETWEEN :inicio AND :fim")
        BigDecimal sumValueByExpenseTypeAndDataExpenseBetween(@Param("tipo") ExpenseType tipo,
                        @Param("inicio") ZonedDateTime inicio, @Param("fim") ZonedDateTime fim);

        // v.06.05.25
        // -- QUERRY PARA DASHBOARD -- //
        @Query("SELECT COALESCE(SUM(d.value), 0) FROM Expense d WHERE d.dateExpense BETWEEN :inicio AND :fim")
        BigDecimal sumValueByDataExpenseBetweenDashboard(@Param("inicio") ZonedDateTime inicio,
                        @Param("fim") ZonedDateTime fim);
}