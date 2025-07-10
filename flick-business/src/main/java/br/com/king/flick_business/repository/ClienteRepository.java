package br.com.king.flick_business.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.entity.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
        Optional<Cliente> findByCpf(String cpf);

        List<Cliente> findByActiveTrue(Sort sort);

        @Override
        List<Cliente> findAll(Sort sort);

        List<Cliente> findBySaldoDevedorGreaterThan(BigDecimal value, Sort sort);

        List<Cliente> findBySaldoDevedorLessThanEqual(BigDecimal value, Sort sort);

        List<Cliente> findByNameContainingIgnoreCaseAndActiveTrue(String name, Sort sort);

        List<Cliente> findByNameContainingIgnoreCaseAndActiveFalse(String name, Sort sort);

        List<Cliente> findByNameContainingIgnoreCaseAndSaldoDevedorGreaterThan(String name, BigDecimal value,
                        Sort sort);

        List<Cliente> findByNameContainingIgnoreCaseAndSaldoDevedorLessThanEqual(String name, BigDecimal value,
                        Sort sort);

        List<Cliente> findByActiveTrueAndSaldoDevedorGreaterThan(BigDecimal value, Sort sort);

        List<Cliente> findByActiveTrueAndSaldoDevedorLessThanEqual(BigDecimal value, Sort sort);

        @Query("SELECT c FROM Cliente c WHERE " +
                        "(:nameContains IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :nameContains, '%'))) AND "
                        +
                        "(:apenasActives IS NULL OR c.active = :apenasActives) AND " +
                        "(:isDevedor IS NULL OR " +
                        "(:isDevedor = true AND c.saldoDevedor > 0) OR " +
                        "(:isDevedor = false AND c.saldoDevedor <= 0))")
        List<Cliente> findClienteComFilters(
                        @Param("nameContains") String nameContains,
                        @Param("apenasActives") Boolean apenasActives,
                        @Param("isDevedor") Boolean isDevedor,
                        Sort sort);
}