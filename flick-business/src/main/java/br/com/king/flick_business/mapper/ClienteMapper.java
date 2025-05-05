package br.com.king.flick_business.mapper;

import br.com.king.flick_business.dto.ClienteRequestDTO;
import br.com.king.flick_business.dto.ClienteResponseDTO;
import br.com.king.flick_business.entity.Cliente;

import java.util.List;
import java.util.stream.Collectors;

public class ClienteMapper {
  public static Cliente toEntity(ClienteRequestDTO dto) {
    if (dto == null)
      return null;

    return Cliente.builder()
        .nome(dto.nome())
        .cpf(dto.cpf())
        .telefone(dto.telefone())
        .endereco(dto.endereco())
        .controleFiado(dto.controleFiado())
        .limiteFiado(dto.limiteFiado())
        .build();
  }

  public static void updateEntityFromDTO(ClienteRequestDTO dto, Cliente clienteExistente) {
    if (dto == null || clienteExistente == null)
      return;

    clienteExistente.setNome(dto.nome());
    clienteExistente.setCpf(dto.cpf());
    clienteExistente.setTelefone(dto.telefone());
    clienteExistente.setEndereco(dto.endereco());
    clienteExistente.setControleFiado(dto.controleFiado());
    clienteExistente.setLimiteFiado(dto.limiteFiado());

    if (dto.ativo() != null) {
      clienteExistente.setAtivo(dto.ativo());
    }
  }

  public static ClienteResponseDTO toDto(Cliente cliente) {
    if (cliente == null)
      return null;
    return new ClienteResponseDTO(cliente);
  }

  public static List<ClienteResponseDTO> toDtoList(List<Cliente> clientes) {
    if (clientes == null)
      return null;
    return clientes.stream()
        .map(ClienteMapper::toDto)
        .collect(Collectors.toList());
  }
}