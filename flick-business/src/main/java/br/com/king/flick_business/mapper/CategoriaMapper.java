package br.com.king.flick_business.mapper;

import br.com.king.flick_business.entity.Categoria;
import br.com.king.flick_business.dto.CategoriaDTO;
import java.util.List;
import java.util.stream.Collectors;

public class CategoriaMapper {

    public static CategoriaDTO toDto(Categoria categoria) {
        if (categoria == null)
            return null;
        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNome());
    }

    public static Categoria toEntity(CategoriaDTO dto) {
        if (dto == null)
            return null;
        return Categoria.builder()
                .id(dto.id())
                .nome(dto.nome())
                .build();
    }

    public static List<CategoriaDTO> toDtoList(List<Categoria> categorias) {
        return categorias.stream()
                .map(CategoriaMapper::toDto)
                .collect(Collectors.toList());
    }

    public static List<Categoria> toEntityList(List<CategoriaDTO> dtos) {
        return dtos.stream()
                .map(CategoriaMapper::toEntity)
                .collect(Collectors.toList());
    }
}
