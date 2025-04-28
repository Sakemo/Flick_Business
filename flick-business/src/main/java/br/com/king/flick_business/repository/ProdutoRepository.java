package br.com.king.flick_business.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.king.flick_business.entity.Produto;
import br.com.king.flick_business.enums.TipoUnidadeVenda;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
  List<Produto> findByCategoriaId(Long categoriaId);

  List<Produto> findByFornecedorId(Long fornecedorId);

  List<Produto> findByAtivoTrueAndCategoriaId(Long categoriaId);

  List<Produto> findByTipoUnidadeVenda(TipoUnidadeVenda tipoUnidadeVenda);
}
