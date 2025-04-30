package br.com.king.flick_business.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
