package br.com.king.flick_business.mapper;

import br.com.king.flick_business.entity.Provider;
import br.com.king.flick_business.dto.ProviderDTO;

import java.util.List;
import java.util.stream.Collectors;

public class ProviderMapper {

  public static ProviderDTO toDto(Provider provider) {
    if (provider == null)
      return null;

    return new ProviderDTO(
        provider.getId(),
        provider.getName(),
        provider.getTipoPessoa(),
        provider.getCnpjCpf(),
        provider.getTelefone(),
        provider.getEmail(),
        provider.getNotas());
  }

  public static Provider toEntity(ProviderDTO dto) {
    if (dto == null)
      return null;

    return Provider.builder()
        .id(dto.id())
        .name(dto.name())
        .tipoPessoa(dto.tipoPessoa())
        .cnpjCpf(dto.cnpjCpf())
        .telefone(dto.telefone())
        .email(dto.email())
        .notas(dto.notas())
        .build();
  }

  public static List<ProviderDTO> toDtoList(List<Provider> providers) {
    return providers.stream()
        .map(ProviderMapper::toDto)
        .collect(Collectors.toList());
  }

  public static List<Provider> toEntityList(List<ProviderDTO> dtos) {
    return dtos.stream()
        .map(ProviderMapper::toEntity)
        .collect(Collectors.toList());
  }
}
