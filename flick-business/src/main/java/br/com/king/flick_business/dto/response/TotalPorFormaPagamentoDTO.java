package br.com.king.flick_business.dto.response;

import br.com.king.flick_business.enums.FormaPagamento;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TotalPorFormaPagamentoDTO {
    private FormaPagamento formaPagamento;
    private BigDecimal total;
}