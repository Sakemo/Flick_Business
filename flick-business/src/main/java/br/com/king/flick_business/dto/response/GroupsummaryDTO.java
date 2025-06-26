package br.com.king.flick_business.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupsummaryDTO {
	private String groupKey;
	private String groupTitle;
	private BigDecimal totalValue;
}
