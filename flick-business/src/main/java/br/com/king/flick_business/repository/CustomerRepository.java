package br.com.king.flick_business.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
        Optional<Customer> findByTaxId(String taxId);

        List<Customer> findByActiveTrue(Sort sort);

        @Override
        List<Customer> findAll(Sort sort);

        List<Customer> findByDebitBalanceGreaterThan(BigDecimal value, Sort sort);

        List<Customer> findByDebitBalanceLessThanEqual(BigDecimal value, Sort sort);

        List<Customer> findByNameContainingIgnoreCaseAndActiveTrue(String name, Sort sort);

        List<Customer> findByNameContainingIgnoreCaseAndActiveFalse(String name, Sort sort);

        List<Customer> findByNameContainingIgnoreCaseAndDebitBalanceGreaterThan(String name, BigDecimal value,
                        Sort sort);

        List<Customer> findByNameContainingIgnoreCaseAndDebitBalanceLessThanEqual(String name, BigDecimal value,
                        Sort sort);

        List<Customer> findByActiveTrueAndDebitBalanceGreaterThan(BigDecimal value, Sort sort);

        List<Customer> findByActiveTrueAndDebitBalanceLessThanEqual(BigDecimal value, Sort sort);

        @Query("SELECT c FROM Customer c WHERE " +
                        "(:nameContains IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :nameContains, '%'))) AND "
                        +
                        "(:apenasActives IS NULL OR c.active = :apenasActives) AND " +
                        "(:isDevedor IS NULL OR " +
                        "(:isDevedor = true AND c.debitBalance > 0) OR " +
                        "(:isDevedor = false AND c.debitBalance <= 0))")
        List<Customer> findCustomerComFiltros(
                        @Param("nameContains") String nameContains,
                        @Param("apenasActives") Boolean apenasActives,
                        @Param("isDevedor") Boolean isDevedor,
                        Sort sort);
}