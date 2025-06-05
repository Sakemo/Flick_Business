package br.com.king.flick_business.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.king.flick_business.dto.ClienteRequestDTO;
import br.com.king.flick_business.dto.ClienteResponseDTO;
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
  public ClienteResponseDTO salvar(ClienteRequestDTO requestDTO) {
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
  public List<ClienteResponseDTO> listarTodos(
      Boolean apenasAtivosParam,
      Boolean devedores,
      String orderBy,
      String nomeContains) {
    System.out
        .println("LOG: ClienteService.listarTodos - apenasAtivos: " + apenasAtivosParam + ", devedores: " + devedores
            + ", orderBy: " + orderBy + ", nomeContains: " + nomeContains);
    Sort sort;
    if (orderBy != null && !orderBy.isBlank()) {
      sort = switch (orderBy) {
        case "nomeAsc" -> Sort.by(Sort.Direction.ASC, "nome");
        case "nomeDesc" -> Sort.by(Sort.Direction.DESC, "nome");
        case "saldoDesc" -> Sort.by(Sort.Direction.DESC, "saldoDevedor");
        case "saldoAsc" -> Sort.by(Sort.Direction.ASC, "saldoDevedor");
        case "cadastroRecente" -> Sort.by(Sort.Direction.DESC, "dataCadastro");
        case "cadastroAntigo" -> Sort.by(Sort.Direction.ASC, "dataCadastro");
        default -> Sort.by(Sort.Direction.DESC, "dataCadastro");
      };
    } else {
      sort = Sort.by(Sort.Direction.DESC, "dataCadastro");
    }

    // Boolean filtrarPorAtivo = apenasAtivosParam;

    String filtroNome = (nomeContains != null && !nomeContains.trim().isEmpty()) ? nomeContains.trim() : "";

    List<Cliente> clientes = clienteRepository.findClienteComFiltros(
        filtroNome, apenasAtivosParam, devedores, sort);
    System.out.println("LOG: ClienteService.listarTodos - Clientes encontrados após filtros: " + clientes.size());
    return ClienteMapper.toDtoList(clientes);

  }

  @Transactional(readOnly = true)
  public ClienteResponseDTO buscarPorId(Long id) {
    Cliente cliente = clienteRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Cliente não encontrado com o ID: " + id));
    return ClienteMapper.toDto(cliente);
  }

  @Transactional
  public void deletar(Long id) {
    Cliente cliente = clienteRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Cliente não encontrado com o ID: " + id));

    if (cliente.getSaldoDevedor() != null && cliente.getSaldoDevedor().compareTo(java.math.BigDecimal.ZERO) > 0) {
      throw new RecursoNaoDeletavel("Cliente com saldo devedor não pode ser deletado");
    }

    cliente.setAtivo(false);
    clienteRepository.save(cliente);
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
  public ClienteResponseDTO ativarInativar(Long id, boolean ativo) {
    Cliente cliente = clienteRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Cliente não encontrado com o ID: " + id));

    if (cliente.getSaldoDevedor() != null && cliente.getSaldoDevedor().compareTo(java.math.BigDecimal.ZERO) > 0) {
      throw new RecursoNaoDeletavel("Cliente com saldo devedor não pode ser inativado");
    }

    cliente.setAtivo(ativo);
    Cliente clienteAtualizado = clienteRepository.save(cliente);
    return ClienteMapper.toDto(clienteAtualizado);
  }

}