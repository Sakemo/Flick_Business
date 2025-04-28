package br.com.king.flick_business.mapper;

import br.com.king.flick_business.entity.Fornecedor;
import br.com.king.flick_business.dto.FornecedorDTO;

import java.util.List;
import java.util.stream.Collectors;

public class FornecedorMapper {

  public static FornecedorDTO toDto(Fornecedor fornecedor) {
    if (fornecedor == null)
      return null;

    return new FornecedorDTO(
        fornecedor.getId(),
        fornecedor.getNome(),
        fornecedor.getTipoPessoa(),
        fornecedor.getCnpjCpf(),
        fornecedor.getTelefone(),
        fornecedor.getEmail(),
        fornecedor.getNotas());
  }

  public static Fornecedor toEntity(FornecedorDTO dto) {
    if (dto == null)
      return null;

    return Fornecedor.builder()
        .id(dto.id())
        .nome(dto.nome())
        .tipoPessoa(dto.tipoPessoa())
        .cnpjCpf(dto.cnpjCpf())
        .telefone(dto.telefone())
        .email(dto.email())
        .notas(dto.notas())
        .build();
  }

  public static List<FornecedorDTO> toDtoList(List<Fornecedor> fornecedores) {
    return fornecedores.stream()
        .map(FornecedorMapper::toDto)
        .collect(Collectors.toList());
  }

  public static List<Fornecedor> toEntityList(List<FornecedorDTO> dtos) {
    return dtos.stream()
        .map(FornecedorMapper::toEntity)
        .collect(Collectors.toList());
  }
}
