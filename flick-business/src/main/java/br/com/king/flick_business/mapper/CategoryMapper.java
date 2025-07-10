package br.com.king.flick_business.mapper;

import br.com.king.flick_business.entity.Category;
import br.com.king.flick_business.dto.CategoryDTO;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryMapper {

    public static CategoryDTO toDto(Category category) {
        if (category == null)
            return null;
        return new CategoryDTO(
                category.getId(),
                category.getName());
    }

    public static Category toEntity(CategoryDTO dto) {
        if (dto == null)
            return null;
        return Category.builder()
                .id(dto.id())
                .name(dto.name())
                .build();
    }

    public static List<CategoryDTO> toDtoList(List<Category> categorys) {
        return categorys.stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public static List<Category> toEntityList(List<CategoryDTO> dtos) {
        return dtos.stream()
                .map(CategoryMapper::toEntity)
                .collect(Collectors.toList());
    }
}
