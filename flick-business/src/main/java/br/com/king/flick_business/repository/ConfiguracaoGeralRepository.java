package br.com.king.flick_business.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.entity.ConfiguracaoGeral;

@Repository
public interface ConfiguracaoGeralRepository extends JpaRepository<ConfiguracaoGeral, Long> {
  default Optional<ConfiguracaoGeral> findConfig() {
    return findById(1L);
  }
}