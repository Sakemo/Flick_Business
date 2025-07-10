package br.com.king.flick_business.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.dto.ProductMaisVendidoDTO;
import br.com.king.flick_business.entity.SaleItem;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
  List<SaleItem> findBySaleId(Long saleId);

  List<SaleItem> findByProductId(Long productId);

  // v.06.05.25
  @Query("SELECT new br.com.king.flick_business.dto.ProductMaisVendidoDTO(iv.product.id, iv.product.name, SUM(iv.quantidade) as qtdTotal, SUM(iv.quantidade * iv.priceUnitarioSale) as totalValue) "
      +
      "FROM SaleItem iv JOIN iv.sale v " +
      "WHERE v.dateSale BETWEEN :start AND :end " +
      "GROUP BY iv.product.id, iv.product.name " +
      "ORDER BY qtdTotal DESC, totalValue DESC " +
      "LIMIT 1")
  ProductMaisVendidoDTO findProductMaisVendidoBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}