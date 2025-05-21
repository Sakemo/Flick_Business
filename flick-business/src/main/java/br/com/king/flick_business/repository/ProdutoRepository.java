package br.com.king.flick_business.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.entity.Produto;
import br.com.king.flick_business.enums.TipoUnidadeVenda;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
  @Query("SELECT p FROM Produto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.fornecedor")
  List<Produto> findAllWithCategoriaAndFornecedor();

  @Query("SELECT p FROM Produto p LEFT JOIN FETCH p.categoria c LEFT JOIN FETCH p.fornecedor WHERE c.id = :categoriaId")
  List<Produto> findByCategoriaIdWithFornecedor(@Param("categoriaId") Long categoriaId);

  List<Produto> findByCategoriaId(Long categoriaId);

  List<Produto> findByFornecedorId(Long fornecedorId);

  List<Produto> findByAtivoTrueAndCategoriaId(Long categoriaId);

  List<Produto> findByTipoUnidadeVenda(TipoUnidadeVenda tipoUnidadeVenda);
}
