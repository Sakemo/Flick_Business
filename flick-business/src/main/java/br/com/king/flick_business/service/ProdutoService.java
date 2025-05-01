package br.com.king.flick_business.service;

import java.util.List;

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
    Categoria categoria = categoriaService.buscarEntidadePorId(requestDTO.categoriaId());
    Fornecedor fornecedor = fornecedorService.buscarEntidadePorId(requestDTO.fornecedorId());
    Produto produto = produtoMapper.toEntity(requestDTO, categoria, fornecedor);
    Produto produtoSalvo = produtoRepository.save(produto);
    return produtoMapper.toResponseDTO(produtoSalvo);
  }

  @Transactional
  public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO requestDTO) {
    Produto produtoExistente = produtoRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Produto não encontrado com ID: " + id));
    Categoria novaCategoria = categoriaService.buscarEntidadePorId(requestDTO.categoriaId());
    Fornecedor novoFornecedor = fornecedorService.buscarEntidadePorId(requestDTO.fornecedorId());
    produtoMapper.updateEntityFromDTO(requestDTO, produtoExistente, novaCategoria, novoFornecedor);
    Produto produtoAtualizado = produtoRepository.save(produtoExistente);
    return produtoMapper.toResponseDTO(produtoAtualizado);
  }

  @Transactional(readOnly = true)
  public List<ProdutoResponseDTO> listarTodos() {
    List<Produto> produtos = produtoRepository.findAll();
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
