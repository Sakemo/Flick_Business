package br.com.king.flick_business.service;

import br.com.king.flick_business.dto.ProviderDTO;
import br.com.king.flick_business.entity.Provider;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.mapper.ProviderMapper;
import br.com.king.flick_business.repository.ProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProviderService {
  private final ProviderRepository providerRepository;

  public ProviderService(ProviderRepository providerRepository) {
    this.providerRepository = providerRepository;
  }

  @Transactional
  public ProviderDTO save(ProviderDTO dto) {
    Provider provider = ProviderMapper.toEntity(dto);
    Provider providerSalvo = providerRepository.save(provider);
    return ProviderMapper.toDto(providerSalvo);
  }

  @Transactional(readOnly = true)
  public List<ProviderDTO> listTodos() {
    return ProviderMapper.toDtoList(providerRepository.findAll());
  }

  @Transactional(readOnly = true)
  public ProviderDTO buscarDtoPorId(Long id) {
    return ProviderMapper.toDto(searchEntityById(id));
  }

  public Provider searchEntityById(Long id) {
    return providerRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Provider não encontrado com ID: " + id));
  }

  @Transactional
  public ProviderDTO atualizar(Long id, ProviderDTO dto) {
    Provider providerExistente = searchEntityById(id);

    providerExistente.setName(dto.name());
    providerExistente.setTipoPessoa(dto.tipoPessoa());
    providerExistente.setCnpjCpf(dto.cnpjCpf());
    providerExistente.setTelefone(dto.telefone());
    providerExistente.setEmail(dto.email());
    providerExistente.setNotas(dto.notas());

    Provider providerAtualizado = providerRepository.save(providerExistente);
    return ProviderMapper.toDto(providerAtualizado);
  }

  @Transactional
  public void delete(Long id) {
    Provider provider = searchEntityById(id);
    providerRepository.delete(provider);
  }
}
