package br.com.king.flick_business.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.king.flick_business.dto.request.ClienteRequestDTO;
import br.com.king.flick_business.dto.response.ClienteResponseDTO;
import br.com.king.flick_business.entity.Cliente;
import br.com.king.flick_business.exception.RecursoJaCadastrado;
import br.com.king.flick_business.exception.RecursoNaoDeletavel;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.mapper.ClienteMapper;
import br.com.king.flick_business.repository.ClienteRepository;

@Service
public class ClienteService {
  private final ClienteRepository clienteRepository;

  public ClienteService(ClienteRepository clienteRepository) {
    this.clienteRepository = clienteRepository;
  }

  @Transactional
  public ClienteResponseDTO save(ClienteRequestDTO requestDTO) {
    validarCpf(requestDTO.cpf(), null);

    Cliente cliente = ClienteMapper.toEntity(requestDTO);
    Cliente clienteSalvo = clienteRepository.save(cliente);

    return ClienteMapper.toDto(clienteSalvo);
  }

  @Transactional
  public ClienteResponseDTO atualizar(Long id, ClienteRequestDTO requestDTO) {
    Cliente clienteExistente = clienteRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Cliente não encontrado com o ID: " + id));

    validarCpf(requestDTO.cpf(), id);

    ClienteMapper.updateEntityFromDTO(requestDTO, clienteExistente);
    Cliente clienteAtualizado = clienteRepository.save(clienteExistente);
    return ClienteMapper.toDto(clienteAtualizado);
  }

  @Transactional(readOnly = true)
  public List<ClienteResponseDTO> listTodos(
      Boolean apenasActivesParam,
      Boolean devedores,
      String orderBy,
      String nameContains) {
    System.out
        .println("LOG: ClienteService.listTodos - apenasActives: " + apenasActivesParam + ", devedores: " + devedores
            + ", orderBy: " + orderBy + ", nameContains: " + nameContains);
    Sort sort;
    if (orderBy != null && !orderBy.isBlank()) {
      sort = switch (orderBy) {
        case "nameAsc" -> Sort.by(Sort.Direction.ASC, "name");
        case "nameDesc" -> Sort.by(Sort.Direction.DESC, "name");
        case "saldoDesc" -> Sort.by(Sort.Direction.DESC, "saldoDevedor");
        case "saldoAsc" -> Sort.by(Sort.Direction.ASC, "saldoDevedor");
        case "cadastroRecente" -> Sort.by(Sort.Direction.DESC, "createdAt");
        case "cadastroAntigo" -> Sort.by(Sort.Direction.ASC, "createdAt");
        default -> Sort.by(Sort.Direction.DESC, "createdAt");
      };
    } else {
      sort = Sort.by(Sort.Direction.DESC, "createdAt");
    }

    // Boolean filtrarPorActive = apenasActivesParam;

    String filtroName = (nameContains != null && !nameContains.trim().isEmpty()) ? nameContains.trim() : "";

    List<Cliente> clientes = clienteRepository.findClienteComFilters(
        filtroName, apenasActivesParam, devedores, sort);
    System.out.println("LOG: ClienteService.listTodos - Clientes encontrados após filtros: " + clientes.size());
    return ClienteMapper.toDtoList(clientes);

  }

  @Transactional(readOnly = true)
  public ClienteResponseDTO searchById(Long id) {
    Cliente cliente = clienteRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Cliente não encontrado com o ID: " + id));
    return ClienteMapper.toDto(cliente);
  }

  @Transactional
  public void delete(Long id) {
    Cliente cliente = clienteRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Cliente não encontrado com o ID: " + id));

    if (cliente.getSaldoDevedor() != null && cliente.getSaldoDevedor().compareTo(java.math.BigDecimal.ZERO) > 0) {
      throw new RecursoNaoDeletavel("Cliente com saldo devedor não pode ser deletado");
    }

    cliente.setActive(false);
    clienteRepository.save(cliente);
  }

  @Transactional
  public void deleteFisicamente(Long id) {
    if (!clienteRepository.existsById(id)) {
      throw new RecursoNaoEncontrado("Product não encontrado com ID: " + id + "para deleção física");
    }
    clienteRepository.deleteById(id);
    // TODO: Adicionar validações ANTES de delete caso ele esteja associado a uma
    // venda
  }

  private void validarCpf(String cpf, Long id) {
    if (cpf != null && !cpf.isBlank()) {
      Optional<Cliente> clienteExistente = clienteRepository.findByCpf(cpf);
      if (clienteExistente.isPresent() && (id == null || !clienteExistente.get().getId().equals(id))) {
        throw new RecursoJaCadastrado("Já existe um cliente cadastrado com o CPF: " + cpf);
      }
    }
  }

  @Transactional
  public ClienteResponseDTO ativarInativar(Long id, boolean active) {
    Cliente cliente = clienteRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Cliente não encontrado com o ID: " + id));

    if (cliente.getSaldoDevedor() != null && cliente.getSaldoDevedor().compareTo(java.math.BigDecimal.ZERO) > 0) {
      throw new RecursoNaoDeletavel("Cliente com saldo devedor não pode ser inativado");
    }

    cliente.setActive(active);
    Cliente clienteAtualizado = clienteRepository.save(cliente);
    return ClienteMapper.toDto(clienteAtualizado);
  }

}