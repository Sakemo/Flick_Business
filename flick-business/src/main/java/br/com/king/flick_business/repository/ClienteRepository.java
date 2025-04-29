package br.com.king.flick_business.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.entity.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long>{
  Optional<Cliente> findByCpf(String cpf);
  Optional<Cliente> findByNomeContainingIgnoreCase(String nome);
  List<Cliente> findByAtivoTrue();
}