package br.com.king.flick_business.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.king.flick_business.dto.ClienteRequestDTO;
import br.com.king.flick_business.dto.ClienteResponseDTO;
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
    ClienteResponseDTO clienteSalvo = clienteService.salvar(requestDTO);
    URI uri = uriBuilder.path("/api/clientes/{id}").buildAndExpand(clienteSalvo.id()).toUri();
    return ResponseEntity.created(uri).body(clienteSalvo);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ClienteResponseDTO> buscarClientePorId(@PathVariable Long id) {
    ClienteResponseDTO cliente = clienteService.buscarPorId(id);
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
  public ResponseEntity<Void> deletarCliente(@PathVariable Long id) {
    clienteService.deletar(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/ativo")
  public ResponseEntity<ClienteResponseDTO> ativarInativarCliente(
      @PathVariable Long id, @RequestBody boolean ativo) {
    ClienteResponseDTO clienteAtualizado = clienteService.ativarInativar(id, ativo);
    return ResponseEntity.ok(clienteAtualizado);
  }
}