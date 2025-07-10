package br.com.king.flick_business.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.dto.ProductMaisVendidoDTO;
import br.com.king.flick_business.entity.ItemVenda;

@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {
  List<ItemVenda> findByVendaId(Long vendaId);

  List<ItemVenda> findByProductId(Long productId);

  // v.06.05.25
  @Query("SELECT new br.com.king.flick_business.dto.ProductMaisVendidoDTO(iv.product.id, iv.product.name, SUM(iv.quantidade) as qtdTotal, SUM(iv.quantidade * iv.precoUnitarioVenda) as valorTotal) "
      +
      "FROM ItemVenda iv JOIN iv.venda v " +
      "WHERE v.dataVenda BETWEEN :inicio AND :fim " +
      "GROUP BY iv.product.id, iv.product.name " +
      "ORDER BY qtdTotal DESC, valorTotal DESC " +
      "LIMIT 1")
  ProductMaisVendidoDTO findProductMaisVendidoBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}