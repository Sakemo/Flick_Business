package br.com.king.flick_business.service;

import br.com.king.flick_business.dto.FornecedorDTO;
import br.com.king.flick_business.entity.Fornecedor;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.mapper.FornecedorMapper;
import br.com.king.flick_business.repository.FornecedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FornecedorService {
  private final FornecedorRepository fornecedorRepository;

  public FornecedorService(FornecedorRepository fornecedorRepository) {
    this.fornecedorRepository = fornecedorRepository;
  }

  @Transactional
  public FornecedorDTO salvar(FornecedorDTO dto) {
    Fornecedor fornecedor = FornecedorMapper.toEntity(dto);
    Fornecedor fornecedorSalvo = fornecedorRepository.save(fornecedor);
    return FornecedorMapper.toDto(fornecedorSalvo);
  }

  @Transactional(readOnly = true)
  public List<FornecedorDTO> listarTodos() {
    return FornecedorMapper.toDtoList(fornecedorRepository.findAll());
  }

  @Transactional(readOnly = true)
  public FornecedorDTO buscarDtoPorId(Long id) {
    return FornecedorMapper.toDto(buscarEntidadePorId(id));
  }

  public Fornecedor buscarEntidadePorId(Long id) {
    return fornecedorRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Fornecedor não encontrado com ID: " + id));
  }

  @Transactional
  public FornecedorDTO atualizar(Long id, FornecedorDTO dto) {
    Fornecedor fornecedorExistente = buscarEntidadePorId(id);

    fornecedorExistente.setNome(dto.nome());
    fornecedorExistente.setTipoPessoa(dto.tipoPessoa());
    fornecedorExistente.setCnpjCpf(dto.cnpjCpf());
    fornecedorExistente.setTelefone(dto.telefone());
    fornecedorExistente.setEmail(dto.email());
    fornecedorExistente.setNotas(dto.notas());

    Fornecedor fornecedorAtualizado = fornecedorRepository.save(fornecedorExistente);
    return FornecedorMapper.toDto(fornecedorAtualizado);
  }

  @Transactional
  public void deletar(Long id) {
    Fornecedor fornecedor = buscarEntidadePorId(id);
    fornecedorRepository.delete(fornecedor);
  }
}
