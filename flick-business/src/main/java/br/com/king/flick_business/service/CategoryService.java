package br.com.king.flick_business.service;

import br.com.king.flick_business.dto.CategoryDTO;
import br.com.king.flick_business.entity.Category;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.mapper.CategoryMapper;
import br.com.king.flick_business.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
  private final CategoryRepository categoryRepository;

  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  @Transactional
  public CategoryDTO save(CategoryDTO dto) {
    Category category = CategoryMapper.toEntity(dto);
    Category categorySalva = categoryRepository.save(category);
    return CategoryMapper.toDto(categorySalva);
  }

  @Transactional(readOnly = true)
  public List<CategoryDTO> listTodas() {
    List<Category> categorys = categoryRepository.findAll();
    return CategoryMapper.toDtoList(categorys);
  }

  @Transactional(readOnly = true)
  public CategoryDTO buscarDtoPorId(Long id) {
    Category category = searchEntityById(id);
    return CategoryMapper.toDto(category);
  }

  @Transactional
  public CategoryDTO atualizar(Long id, CategoryDTO dto) {
    Category categoryExistente = searchEntityById(id);
    categoryExistente.setName(dto.name());
    Category categoryAtualizada = categoryRepository.save(categoryExistente);
    return CategoryMapper.toDto(categoryAtualizada);
  }

  @Transactional
  public void delete(Long id) {
    Category category = searchEntityById(id);
    categoryRepository.delete(category);
  }

  public Category searchEntityById(Long id) {
    return categoryRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Category não encontrada com ID: " + id));
  }

}
