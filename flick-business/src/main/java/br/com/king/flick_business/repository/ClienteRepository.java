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

        List<Cliente> findByAtivoTrue(Sort sort);

        @Override
        List<Cliente> findAll(Sort sort);

        List<Cliente> findBySaldoDevedorGreaterThan(BigDecimal valor, Sort sort);

        List<Cliente> findBySaldoDevedorLessThanEqual(BigDecimal valor, Sort sort);

        List<Cliente> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome, Sort sort);

        List<Cliente> findByNomeContainingIgnoreCaseAndAtivoFalse(String nome, Sort sort);

        List<Cliente> findByNomeContainingIgnoreCaseAndSaldoDevedorGreaterThan(String nome, BigDecimal valor,
                        Sort sort);

        List<Cliente> findByNomeContainingIgnoreCaseAndSaldoDevedorLessThanEqual(String nome, BigDecimal valor,
                        Sort sort);

        List<Cliente> findByAtivoTrueAndSaldoDevedorGreaterThan(BigDecimal valor, Sort sort);

        List<Cliente> findByAtivoTrueAndSaldoDevedorLessThanEqual(BigDecimal valor, Sort sort);

        @Query("SELECT c FROM Cliente c WHERE " +
                        "(:nomeContains IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :nomeContains, '%'))) AND "
                        +
                        "(:apenasAtivos IS NULL OR c.ativo = :apenasAtivos) AND " +
                        "(:isDevedor IS NULL OR " +
                        "(:isDevedor = true AND c.saldoDevedor > 0) OR " +
                        "(:isDevedor = false AND c.saldoDevedor <= 0))")
        List<Cliente> findClienteComFiltros(
                        @Param("nomeContains") String nomeContains,
                        @Param("apenasAtivos") Boolean apenasAtivos,
                        @Param("isDevedor") Boolean isDevedor,
                        Sort sort);
}