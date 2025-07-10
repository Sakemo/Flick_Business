package br.com.king.flick_business.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.entity.Product;
import br.com.king.flick_business.enums.UnitOfSale;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
  @Query(value = "SELECT p.* FROM products p " +
      "LEFT JOIN itens_venda iv ON p.id = iv.product_id " +
      "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
      "AND (:categoryId IS NULL OR p.category_id = :categoryId) " +
      "GROUP BY p.id " +
      "ORDER BY COALESCE(SUM(iv.quantidade), 0) DESC, p.name ASC", countQuery = "SELECT count(*) FROM products p " +
          "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
          "AND (:categoryId IS NULL OR p.category_id = :categoryId)", nativeQuery = true)
  List<Product> findWithFiltersAndSortByVendas(@Param("name") String name,
      @Param("categoryId") Long categoryId);

  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.provider")
  List<Product> findAllWithCategoryAndProvider();

  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c LEFT JOIN FETCH p.provider WHERE c.id = :categoryId")
  List<Product> findByCategoryIdWithProvider(@Param("categoryId") Long categoryId);

  List<Product> findByCategoryId(Long categoryId);

  List<Product> findByProviderId(Long providerId);

  List<Product> findByActiveTrueAndCategoryId(Long categoryId);

  List<Product> findByUnitOfSale(UnitOfSale UnitOfSale);

  List<Product> findByNameStartingWith(String prefixo);
}
