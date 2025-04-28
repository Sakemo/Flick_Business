package br.com.king.flick_business.service;

import br.com.king.flick_business.dto.CategoriaDTO;
import br.com.king.flick_business.entity.Categoria;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.mapper.CategoriaMapper;
import br.com.king.flick_business.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {
  private final CategoriaRepository categoriaRepository;

  public CategoriaService(CategoriaRepository categoriaRepository) {
    this.categoriaRepository = categoriaRepository;
  }

  @Transactional
  public CategoriaDTO salvar(CategoriaDTO dto) {
    Categoria categoria = CategoriaMapper.toEntity(dto);
    Categoria categoriaSalva = categoriaRepository.save(categoria);
    return CategoriaMapper.toDto(categoriaSalva);
  }

  @Transactional(readOnly = true)
  public List<CategoriaDTO> listarTodas() {
    List<Categoria> categorias = categoriaRepository.findAll();
    return CategoriaMapper.toDtoList(categorias);
  }

  @Transactional(readOnly = true)
  public CategoriaDTO buscarDtoPorId(Long id) {
    Categoria categoria = buscarEntidadePorId(id);
    return CategoriaMapper.toDto(categoria);
  }

  @Transactional
  public CategoriaDTO atualizar(Long id, CategoriaDTO dto) {
    Categoria categoriaExistente = buscarEntidadePorId(id);
    categoriaExistente.setNome(dto.nome());
    Categoria categoriaAtualizada = categoriaRepository.save(categoriaExistente);
    return CategoriaMapper.toDto(categoriaAtualizada);
  }

  @Transactional
  public void deletar(Long id) {
    Categoria categoria = buscarEntidadePorId(id);
    categoriaRepository.delete(categoria);
  }

  public Categoria buscarEntidadePorId(Long id) {
    return categoriaRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Categoria não encontrada com ID: " + id));
  }

}
