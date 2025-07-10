package br.com.king.flick_business.dto.response;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import br.com.king.flick_business.entity.Customer;

public record CustomerResponseDTO(
    Long id,

    // -- IDENTIFICAÇÃO -- //
    String name,
    String taxId,
    String phone,
    String adress,
    Boolean creditManagement,

    // -- FIADO -- //
    BigDecimal creditLimit,
    BigDecimal debitBalance,
    ZonedDateTime dateLastPurchaseOnCredit,

    // -- METADADOS -- //
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt,
    Boolean active)

{
  public CustomerResponseDTO(Customer customer) {
    this(
        customer.getId(),
        customer.getName(),
        customer.getTaxId(),
        customer.getPhone(),
        customer.getAdress(),
        customer.getCreditManagement(),
        customer.getCreditLimit(),
        customer.getDebitBalance(),
        customer.getDateLastPurchaseOnCredit(),
        customer.getCreatedAt(),
        customer.getUpdatedAt(),
        customer.getActive() != null && customer.getActive());

  }
}