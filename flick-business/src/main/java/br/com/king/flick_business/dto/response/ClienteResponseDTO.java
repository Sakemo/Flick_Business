package br.com.king.flick_business.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import br.com.king.flick_business.entity.Cliente;

public record ClienteResponseDTO(
    Long id,

    // -- IDENTIFICAÇÃO -- //
    String nome,
    String cpf,
    String telefone,
    String endereco,
    Boolean controleFiado,

    // -- FIADO -- //
    BigDecimal limiteFiado,
    BigDecimal saldoDevedor,
    ZonedDateTime dataUltimaCompraFiado,

    // -- METADADOS -- //
    ZonedDateTime dataCadastro,
    ZonedDateTime dataAtualizacao,
    Boolean ativo)

{
  public ClienteResponseDTO(Cliente cliente) {
    this(
        cliente.getId(),
        cliente.getNome(),
        cliente.getCpf(),
        cliente.getTelefone(),
        cliente.getEndereco(),
        cliente.getControleFiado(),
        cliente.getLimiteFiado(),
        cliente.getSaldoDevedor(),
        cliente.getDataUltimaCompraFiado(),
        cliente.getDataCadastro(),
        cliente.getDataAtualizacao(),
        cliente.getAtivo() != null && cliente.getAtivo());

  }
}