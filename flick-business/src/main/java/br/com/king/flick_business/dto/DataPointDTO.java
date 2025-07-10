package br.com.king.flick_business.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DataPointDTO(
        LocalDate data,
        BigDecimal value) {
}