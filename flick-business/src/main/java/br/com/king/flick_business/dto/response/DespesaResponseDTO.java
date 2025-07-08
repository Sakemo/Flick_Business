package br.com.king.flick_business.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import br.com.king.flick_business.entity.Despesa;
import br.com.king.flick_business.enums.TipoDespesa;

public record DespesaResponseDTO(
    Long id,
    String nome,
    BigDecimal valor,
    ZonedDateTime dataDespesa,
    TipoDespesa tipoDespesa,
    ZonedDateTime dataCriacao,
    ZonedDateTime dataAtualizacao) {

  public DespesaResponseDTO(Despesa despesa) {
    this(
        despesa.getId(),
        despesa.getNome(),
        despesa.getValor(),
        despesa.getDataDespesa(),
        despesa.getTipoDespesa(),
        despesa.getDataCriacao(),
        despesa.getDataAtualizacao());
  }
}