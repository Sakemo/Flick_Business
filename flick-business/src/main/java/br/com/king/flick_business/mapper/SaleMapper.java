package br.com.king.flick_business.mapper;

import java.time.ZonedDateTime;

import org.springframework.data.domain.Sort;

import br.com.king.flick_business.enums.PaymentMethod;

public class SaleMapper {

    public static PaymentMethod parsePaymentMethod(String paymentMethodString) {
        if (paymentMethodString == null || paymentMethodString.isEmpty()) {
            return null;
        }

        try {
            return PaymentMethod.valueOf(paymentMethodString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static ZonedDateTime[] buildDateRange(ZonedDateTime start, ZonedDateTime end) {
        ZonedDateTime dateInicio = (start != null) ? start
                : ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, java.time.ZoneId.systemDefault());
        ZonedDateTime dateFim = (end != null) ? end
                : ZonedDateTime.of(9999, 12, 31, 23, 59, 59, 999999999, java.time.ZoneId.systemDefault());

        return new ZonedDateTime[] { dateInicio, dateFim };
    }

    public static Sort buildSort(String orderBy) {
        if (orderBy != null && !orderBy.isBlank()) {
            String[] parts = orderBy.split(",");
            String property = parts[0];
            Sort.Direction direction = (parts.length > 1 && parts[1].equals("desc")) ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            switch (property) {
                case "dateSale", "totalValue", "customer.name" -> {
                }
                default -> {
                    System.out.println("SERVICE LOG: orderBy não reconhecida: " + property + ". Usando padrão.");
                    property = "dateSale";
                    direction = Sort.Direction.DESC;
                }
            }
            return Sort.by(direction, property);
        }
        return Sort.by(Sort.Direction.DESC, "dateSale");
    }

}