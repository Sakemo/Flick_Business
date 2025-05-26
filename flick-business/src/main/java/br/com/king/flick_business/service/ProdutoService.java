package br.com.king.flick_business.service;

import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.king.flick_business.dto.ProdutoRequestDTO;
import br.com.king.flick_business.dto.ProdutoResponseDTO;
import br.com.king.flick_business.entity.Categoria;
import br.com.king.flick_business.entity.Fornecedor;
import br.com.king.flick_business.entity.Produto;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.mapper.ProdutoMapper;
import br.com.king.flick_business.repository.ProdutoRepository;

@Service
public class ProdutoService {

  private final ProdutoRepository produtoRepository;
  private final ProdutoMapper produtoMapper;
  private final CategoriaService categoriaService;
  private final FornecedorService fornecedorService;

  public ProdutoService(ProdutoRepository produtoRepository,
      ProdutoMapper produtoMapper,
      CategoriaService categoriaService,
      FornecedorService fornecedorService) {
    this.produtoRepository = produtoRepository;
    this.produtoMapper = produtoMapper;
    this.categoriaService = categoriaService;
    this.fornecedorService = fornecedorService;
  }

  @Transactional
  public ProdutoResponseDTO salvar(ProdutoRequestDTO requestDTO) {
    System.out.println("LOG: ProdutoService.salvar - categoriaId recebido: " + requestDTO.categoriaId());
    Categoria categoria = categoriaService.buscarEntidadePorId(requestDTO.categoriaId());
    System.out
        .println(
            "LOG: ProdutoService.salvar - Categoria buscada: " + (categoria != null ? categoria.getNome() : "NULA"));
    Fornecedor fornecedor = null;
    if (requestDTO.fornecedorId() != null) {
      fornecedor = fornecedorService.buscarEntidadePorId(requestDTO.fornecedorId());
    }
    Produto produto = produtoMapper.toEntity(requestDTO, categoria, fornecedor);
    System.out.println("LOG: ProdutoService.salvar - Produto mapeado par salvar, categoria do produto: "
        + (produto.getCategoria() != null ? produto.getCategoria().getNome() : "NULA"));
    Produto produtoSalvo = produtoRepository.save(produto);
    System.out.println(
        "LOG: ProdutoService.salvar - Produto salvo, categoria do produtoSalvo: "
            + (produtoSalvo.getCategoria() != null ? produtoSalvo.getCategoria().getNome() : "NULA"));
    return produtoMapper.toResponseDTO(produtoSalvo);
  }

  @Transactional
  public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO requestDTO) {
    Produto produtoExistente = produtoRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Produto não encontrado com ID: " + id));
    System.out.println("LOG: ProdutoService.atualizar - categoriaId recebido: " + (requestDTO.categoriaId()));
    Categoria novaCategoria = categoriaService.buscarEntidadePorId(requestDTO.categoriaId());
    System.out.println("LOG: ProdutoService.atualizar - categoria buscada: "
        + (novaCategoria != null ? novaCategoria.getNome() : "NULA"));
    Fornecedor novoFornecedor;
    if (requestDTO.fornecedorId() != null) {
      novoFornecedor = fornecedorService.buscarEntidadePorId(requestDTO.fornecedorId());
    } else if (produtoExistente.getFornecedor() != null) {
      novoFornecedor = produtoExistente.getFornecedor();
    } else {
      novoFornecedor = null;
    }
    produtoMapper.updateEntityFromDTO(requestDTO, produtoExistente, novaCategoria, novoFornecedor);
    Produto produtoAtualizado = produtoRepository.save(produtoExistente);

    System.out.println("LOG: ProdutoService.atualizar - produtoMapper: " + (produtoMapper));
    System.out.println("LOG: ProdutoService.atualizar - produto: " + (produtoAtualizado));
    return produtoMapper.toResponseDTO(produtoAtualizado);
  }

  @Transactional(readOnly = true)
  public List<ProdutoResponseDTO> listarTodos(Long categoriaId) {
    List<Produto> produtos;
    if (categoriaId != null) {
      produtos = produtoRepository.findByCategoriaIdWithFornecedor(categoriaId);
    } else {
      produtos = produtoRepository.findAllWithCategoriaAndFornecedor();
    }
    return produtoMapper.toResponseDTOList(produtos);
  }

  @Transactional(readOnly = true)
  public ProdutoResponseDTO buscarPorId(Long id) {
    Produto produto = produtoRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Produto não encontrado com ID: " + id));
    return produtoMapper.toResponseDTO(produto);
  }

  @Transactional
  public void deletarLogicamente(Long id) {
    Produto produto = produtoRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Produto não encontrado com ID: " + id));
    produto.setAtivo(false);
    produtoRepository.save(produto);
  }
}
