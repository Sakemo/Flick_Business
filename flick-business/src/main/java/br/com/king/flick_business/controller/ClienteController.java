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

import br.com.king.flick_business.dto.request.ClienteRequestDTO;
import br.com.king.flick_business.dto.response.ClienteResponseDTO;
import br.com.king.flick_business.service.ClienteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
  private final ClienteService clienteService;

  public ClienteController(ClienteService clienteService) {
    this.clienteService = clienteService;
  }

  @PostMapping
  public ResponseEntity<ClienteResponseDTO> criarCliente(
      @Valid @RequestBody ClienteRequestDTO requestDTO,
      UriComponentsBuilder uriBuilder) {
    ClienteResponseDTO clienteSalvo = clienteService.save(requestDTO);
    URI uri = uriBuilder.path("/api/clientes/{id}").buildAndExpand(clienteSalvo.id()).toUri();
    return ResponseEntity.created(uri).body(clienteSalvo);
  }

  @GetMapping
  public ResponseEntity<List<ClienteResponseDTO>> listTodosOsClientes(
      @RequestParam(name = "apenasActives", required = false) Boolean apenasActives,
      @RequestParam(name = "devedores", required = false) Boolean devedores,
      @RequestParam(name = "orderBy", required = false) String orderBy,
      @RequestParam(name = "nameContains", required = false) String nameContains) {
    System.out.println("LOG: ClienteController - apenasActives: " + apenasActives + ", devedores: " + devedores
        + ", orderBy: " + orderBy + ", nameContains: " + nameContains);
    List<ClienteResponseDTO> clientes = clienteService.listTodos(
        apenasActives,
        devedores,
        orderBy,
        nameContains);
    return ResponseEntity.ok(clientes);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ClienteResponseDTO> buscarClientePorId(@PathVariable Long id) {
    ClienteResponseDTO cliente = clienteService.searchById(id);
    return ResponseEntity.ok(cliente);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ClienteResponseDTO> atualizarCliente(
      @PathVariable Long id,
      @Valid @RequestBody ClienteRequestDTO requestDTO) {
    ClienteResponseDTO clienteAtualizado = clienteService.atualizar(id, requestDTO);
    return ResponseEntity.ok(clienteAtualizado);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
    clienteService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/permanente")
  public ResponseEntity<Void> deleteClienteFisicamente(@PathVariable Long id) {
    clienteService.deleteFisicamente(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/active")
  public ResponseEntity<ClienteResponseDTO> ativarInativarCliente(
      @PathVariable Long id, @RequestBody boolean active) {
    ClienteResponseDTO clienteAtualizado = clienteService.ativarInativar(id, active);
    return ResponseEntity.ok(clienteAtualizado);
  }
}