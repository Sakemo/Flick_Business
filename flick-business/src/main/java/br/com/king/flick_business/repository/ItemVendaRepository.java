package br.com.king.flick_business.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.entity.ItemVenda;

@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {
  List<ItemVenda> findByVendaId(Long vendaId);

  List<ItemVenda> findByProdutoId(Long produtoId);
}