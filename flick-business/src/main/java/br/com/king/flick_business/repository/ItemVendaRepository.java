package br.com.king.flick_business.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.dto.ProdutoMaisVendidoDTO;
import br.com.king.flick_business.entity.ItemVenda;

@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {
  List<ItemVenda> findByVendaId(Long vendaId);

  List<ItemVenda> findByProdutoId(Long produtoId);

  // v.06.05.25
  @Query("SELECT new br.com.king.flick_business.dto.ProdutoMaisVendidoDTO(iv.produto.id, iv.produto.nome, SUM(iv.quantidade) as qtdTotal, SUM(iv.quantidade * iv.precoUnitarioVenda) as valorTotal) "
      +
      "FROM ItemVenda iv JOIN iv.venda v " +
      "WHERE v.dataVenda BETWEEN :inicio AND :fim " +
      "GROUP BY iv.produto.id, iv.produto.nome " +
      "ORDER BY qtdTotal DESC, valorTotal DESC " +
      "LIMIT 1")
  ProdutoMaisVendidoDTO findProdutoMaisVendidoBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}