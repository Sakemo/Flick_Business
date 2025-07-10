package br.com.king.flick_business.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.king.flick_business.dto.request.CustomerRequestDTO;
import br.com.king.flick_business.dto.response.CustomerResponseDTO;
import br.com.king.flick_business.service.CustomerService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
  private final CustomerService customerService;

  public CustomerController(CustomerService customerService) {
    this.customerService = customerService;
  }

  @PostMapping
  public ResponseEntity<CustomerResponseDTO> criarCustomer(
      @Valid @RequestBody CustomerRequestDTO requestDTO,
      UriComponentsBuilder uriBuilder) {
    CustomerResponseDTO customerSaved = customerService.save(requestDTO);
    URI uri = uriBuilder.path("/api/customers/{id}").buildAndExpand(customerSaved.id()).toUri();
    return ResponseEntity.created(uri).body(customerSaved);
  }

  @GetMapping
  public ResponseEntity<List<CustomerResponseDTO>> listAllOsCustomers(
      @RequestParam(name = "apenasActives", required = false) Boolean apenasActives,
      @RequestParam(name = "devedores", required = false) Boolean devedores,
      @RequestParam(name = "orderBy", required = false) String orderBy,
      @RequestParam(name = "nameContains", required = false) String nameContains) {
    System.out.println("LOG: CustomerController - apenasActives: " + apenasActives + ", devedores: " + devedores
        + ", orderBy: " + orderBy + ", nameContains: " + nameContains);
    List<CustomerResponseDTO> customers = customerService.listAll(
        apenasActives,
        devedores,
        orderBy,
        nameContains);
    return ResponseEntity.ok(customers);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CustomerResponseDTO> searchCustomerById(@PathVariable Long id) {
    CustomerResponseDTO customer = customerService.searchById(id);
    return ResponseEntity.ok(customer);
  }

  @PutMapping("/{id}")
  public ResponseEntity<CustomerResponseDTO> updateCustomer(
      @PathVariable Long id,
      @Valid @RequestBody CustomerRequestDTO requestDTO) {
    CustomerResponseDTO customerUpdated = customerService.update(id, requestDTO);
    return ResponseEntity.ok(customerUpdated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletarCustomer(@PathVariable Long id) {
    customerService.deletar(id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/permanente")
  public ResponseEntity<Void> deletarCustomerFisicamente(@PathVariable Long id) {
    customerService.deletarFisicamente(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/active")
  public ResponseEntity<CustomerResponseDTO> ativarInativarCustomer(
      @PathVariable Long id, @RequestBody boolean active) {
    CustomerResponseDTO customerUpdated = customerService.ativarInativar(id, active);
    return ResponseEntity.ok(customerUpdated);
  }
}