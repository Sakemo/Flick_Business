package br.com.king.flick_business.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.king.flick_business.dto.GeneralSettingsDTO;
import br.com.king.flick_business.entity.GeneralSettings;
import br.com.king.flick_business.repository.GeneralSettingsRepository;

@Service
public class GeneralSettingsService {
  private final GeneralSettingsRepository configuracaoRepository;

  // Identificação e Construtor //
  public GeneralSettingsService(GeneralSettingsRepository configuracaoRepository) {
    this.configuracaoRepository = configuracaoRepository;
  }

  // Buscar Configuração Geral //
  @Transactional(readOnly = true)
  public GeneralSettingsDTO searchConfiguracao() {
    GeneralSettings config = configuracaoRepository.findConfig()
        .orElseGet(() -> GeneralSettings.builder().id(1L).build());
    return mapEntityToDto(config);
  }

  // Buscar Entity Configuração Geral //
  @Transactional(readOnly = true)
  public Optional<GeneralSettings> searchEntityConfiguracao() {
    return configuracaoRepository.findConfig();
  }

  // save ou Update Configuração Geral //
  @Transactional
  public GeneralSettingsDTO saveOrUpdateConfiguracao(GeneralSettingsDTO dto) {
    GeneralSettings config = configuracaoRepository.findConfig()
        .orElseGet(() -> GeneralSettings.builder().id(1L).build());

    config.setTaxaJurosAtraso(dto.taxaJuros());
    config.setPrazoPagamentoFiado(dto.prazoPagamento());
    config.setNameNegocio(dto.nameNegocio());
    GeneralSettings configSalva = configuracaoRepository.save(config);

    return mapEntityToDto(configSalva);
  }

  // Mapper //
  private GeneralSettingsDTO mapEntityToDto(GeneralSettings entity) {
    return new GeneralSettingsDTO(
        entity.getTaxaJurosAtraso(),
        entity.getPrazoPagamentoFiado(),
        entity.getUpdatedAt(),
        entity.getNameNegocio());
  }

}