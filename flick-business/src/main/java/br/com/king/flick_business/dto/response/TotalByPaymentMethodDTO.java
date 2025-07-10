package br.com.king.flick_business.dto.response;

import br.com.king.flick_business.enums.PaymentMethod;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TotalByPaymentMethodDTO {
	private PaymentMethod paymentMethod;
	private BigDecimal total;
}
