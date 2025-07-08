package br.com.king.flick_business.mapper;

import java.time.ZonedDateTime;

import org.springframework.data.domain.Sort;

import br.com.king.flick_business.enums.FormaPagamento;

public class VendaMapper {

    public static FormaPagamento parseFormaPagamento(String formaPagamentoString) {
        if (formaPagamentoString == null || formaPagamentoString.isEmpty()) {
            return null;
        }

        try {
            return FormaPagamento.valueOf(formaPagamentoString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static ZonedDateTime[] buildDataRange(ZonedDateTime inicio, ZonedDateTime fim) {
        ZonedDateTime dataInicio = (inicio != null) ? inicio
                : ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, java.time.ZoneId.systemDefault());
        ZonedDateTime dataFim = (fim != null) ? fim
                : ZonedDateTime.of(9999, 12, 31, 23, 59, 59, 999999999, java.time.ZoneId.systemDefault());

        return new ZonedDateTime[] { dataInicio, dataFim };
    }

    public static Sort buildSort(String orderBy) {
        if (orderBy != null && !orderBy.isBlank()) {
            String[] parts = orderBy.split(",");
            String property = parts[0];
            Sort.Direction direction = (parts.length > 1 && parts[1].equals("desc")) ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            switch (property) {
                case "dataVenda", "valorTotal", "cliente.nome" -> {
                }
                default -> {
                    System.out.println("SERVICE LOG: orderBy não reconhecida: " + property + ". Usando padrão.");
                    property = "dataVenda";
                    direction = Sort.Direction.DESC;
                }
            }
            return Sort.by(direction, property);
        }
        return Sort.by(Sort.Direction.DESC, "dataVenda");
    }

}