package br.com.king.flick_business.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.king.flick_business.entity.Despesa;
import br.com.king.flick_business.enums.TipoDespesa;

public record DespesaResponseDTO(
    Long id,
    String nome,
    BigDecimal valor,
    LocalDateTime dataDespesa,
    TipoDespesa tipoDespesa,
    LocalDateTime dataCriacao,
    LocalDateTime dataAtualizacao) {

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