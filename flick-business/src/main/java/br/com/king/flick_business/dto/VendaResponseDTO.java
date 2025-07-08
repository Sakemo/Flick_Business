package br.com.king.flick_business.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import br.com.king.flick_business.dto.response.ClienteResponseDTO;
import br.com.king.flick_business.dto.response.ItemVendaResponseDTO;
import br.com.king.flick_business.entity.Venda;
import br.com.king.flick_business.enums.FormaPagamento;

public record VendaResponseDTO(
    Long id,
    ZonedDateTime dataVenda,
    BigDecimal valorTotal,
    ClienteResponseDTO cliente,
    List<ItemVendaResponseDTO> itens,
    FormaPagamento formaPagamento,
    String observacoes) {
  public VendaResponseDTO(Venda venda) {
    this(
        venda.getId(),
        venda.getDataVenda(),
        venda.getValorTotal(),
        venda.getCliente() != null ? new ClienteResponseDTO(venda.getCliente()) : null,
        venda.getItens().stream()
            .map(ItemVendaResponseDTO::new)
            .collect(Collectors.toList()),
        venda.getFormaPagamento(),
        venda.getObservacoes());
  }
}