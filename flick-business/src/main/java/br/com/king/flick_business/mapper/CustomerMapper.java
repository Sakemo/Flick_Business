package br.com.king.flick_business.mapper;

import br.com.king.flick_business.dto.request.CustomerRequestDTO;
import br.com.king.flick_business.dto.response.CustomerResponseDTO;
import br.com.king.flick_business.entity.Customer;

import java.util.List;
import java.util.stream.Collectors;

public class CustomerMapper {
  public static Customer toEntity(CustomerRequestDTO dto) {
    if (dto == null)
      return null;

    return Customer.builder()
        .name(dto.name())
        .taxId(dto.taxId())
        .phone(dto.phone())
        .adress(dto.adress())
        .creditManagement(dto.creditManagement())
        .creditLimit(dto.creditLimit())
        .build();
  }

  public static void updateEntityFromDTO(CustomerRequestDTO dto, Customer customerExistente) {
    if (dto == null || customerExistente == null)
      return;

    customerExistente.setName(dto.name());
    customerExistente.setTaxId(dto.taxId());
    customerExistente.setPhone(dto.phone());
    customerExistente.setAdress(dto.adress());
    customerExistente.setCreditManagement(dto.creditManagement());
    customerExistente.setCreditLimit(dto.creditLimit());

    if (dto.active() != null) {
      customerExistente.setActive(dto.active());
    }
  }

  public static CustomerResponseDTO toDto(Customer customer) {
    if (customer == null)
      return null;
    return new CustomerResponseDTO(customer);
  }

  public static List<CustomerResponseDTO> toDtoList(List<Customer> customers) {
    if (customers == null)
      return null;
    return customers.stream()
        .map(CustomerMapper::toDto)
        .collect(Collectors.toList());
  }
}