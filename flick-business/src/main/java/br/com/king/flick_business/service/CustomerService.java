package br.com.king.flick_business.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.king.flick_business.dto.request.CustomerRequestDTO;
import br.com.king.flick_business.dto.response.CustomerResponseDTO;
import br.com.king.flick_business.entity.Customer;
import br.com.king.flick_business.exception.RecursoJaCadastrado;
import br.com.king.flick_business.exception.RecursoNaoDeletavel;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.mapper.CustomerMapper;
import br.com.king.flick_business.repository.CustomerRepository;

@Service
public class CustomerService {
  private final CustomerRepository customerRepository;

  public CustomerService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  @Transactional
  public CustomerResponseDTO save(CustomerRequestDTO requestDTO) {
    validarTaxId(requestDTO.taxId(), null);

    Customer customer = CustomerMapper.toEntity(requestDTO);
    Customer customerSaved = customerRepository.save(customer);

    return CustomerMapper.toDto(customerSaved);
  }

  @Transactional
  public CustomerResponseDTO update(Long id, CustomerRequestDTO requestDTO) {
    Customer customerExistente = customerRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Customer não encontrado com o ID: " + id));

    validarTaxId(requestDTO.taxId(), id);

    CustomerMapper.updateEntityFromDTO(requestDTO, customerExistente);
    Customer customerUpdated = customerRepository.save(customerExistente);
    return CustomerMapper.toDto(customerUpdated);
  }

  @Transactional(readOnly = true)
  public List<CustomerResponseDTO> listAll(
      Boolean apenasActivesParam,
      Boolean devedores,
      String orderBy,
      String nameContains) {
    System.out
        .println("LOG: CustomerService.listAll - apenasActives: " + apenasActivesParam + ", devedores: " + devedores
            + ", orderBy: " + orderBy + ", nameContains: " + nameContains);
    Sort sort;
    if (orderBy != null && !orderBy.isBlank()) {
      sort = switch (orderBy) {
        case "nameAsc" -> Sort.by(Sort.Direction.ASC, "name");
        case "nameDesc" -> Sort.by(Sort.Direction.DESC, "name");
        case "saldoDesc" -> Sort.by(Sort.Direction.DESC, "debitBalance");
        case "saldoAsc" -> Sort.by(Sort.Direction.ASC, "debitBalance");
        case "registerRecente" -> Sort.by(Sort.Direction.DESC, "createdAt");
        case "registerAntigo" -> Sort.by(Sort.Direction.ASC, "createdAt");
        default -> Sort.by(Sort.Direction.DESC, "createdAt");
      };
    } else {
      sort = Sort.by(Sort.Direction.DESC, "createdAt");
    }

    // Boolean filtrarByActive = apenasActivesParam;

    String filtroName = (nameContains != null && !nameContains.trim().isEmpty()) ? nameContains.trim() : "";

    List<Customer> customers = customerRepository.findCustomerComFiltros(
        filtroName, apenasActivesParam, devedores, sort);
    System.out.println("LOG: CustomerService.listAll - Customers encontrados após filtros: " + customers.size());
    return CustomerMapper.toDtoList(customers);

  }

  @Transactional(readOnly = true)
  public CustomerResponseDTO searchById(Long id) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Customer não encontrado com o ID: " + id));
    return CustomerMapper.toDto(customer);
  }

  @Transactional
  public void deletar(Long id) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Customer não encontrado com o ID: " + id));

    if (customer.getDebitBalance() != null && customer.getDebitBalance().compareTo(java.math.BigDecimal.ZERO) > 0) {
      throw new RecursoNaoDeletavel("Customer com saldo devedor não pode ser deletado");
    }

    customer.setActive(false);
    customerRepository.save(customer);
  }

  @Transactional
  public void deletarFisicamente(Long id) {
    if (!customerRepository.existsById(id)) {
      throw new RecursoNaoEncontrado("Product não encontrado com ID: " + id + "para deleção física");
    }
    customerRepository.deleteById(id);
    // TODO: Adicionar validações ANTES de deletar caso ele esteja associado a uma
    // sale
  }

  private void validarTaxId(String taxId, Long id) {
    if (taxId != null && !taxId.isBlank()) {
      Optional<Customer> customerExistente = customerRepository.findByTaxId(taxId);
      if (customerExistente.isPresent() && (id == null || !customerExistente.get().getId().equals(id))) {
        throw new RecursoJaCadastrado("Já existe um customer cadastrado com o CPF: " + taxId);
      }
    }
  }

  @Transactional
  public CustomerResponseDTO ativarInativar(Long id, boolean active) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Customer não encontrado com o ID: " + id));

    if (customer.getDebitBalance() != null && customer.getDebitBalance().compareTo(java.math.BigDecimal.ZERO) > 0) {
      throw new RecursoNaoDeletavel("Customer com saldo devedor não pode ser inativado");
    }

    customer.setActive(active);
    Customer customerUpdated = customerRepository.save(customer);
    return CustomerMapper.toDto(customerUpdated);
  }

}