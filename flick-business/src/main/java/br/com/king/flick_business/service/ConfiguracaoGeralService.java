package br.com.king.flick_business.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.king.flick_business.dto.ConfiguracaoGeralDTO;
import br.com.king.flick_business.entity.ConfiguracaoGeral;
import br.com.king.flick_business.repository.ConfiguracaoGeralRepository;

@Service
public class ConfiguracaoGeralService {
  private final ConfiguracaoGeralRepository configuracaoRepository;

  // Identificação e Construtor //
  public ConfiguracaoGeralService(ConfiguracaoGeralRepository configuracaoRepository) {
    this.configuracaoRepository = configuracaoRepository;
  }

  // Buscar Configuração Geral //
  @Transactional(readOnly = true)
  public ConfiguracaoGeralDTO buscarConfiguracao() {
    ConfiguracaoGeral config = configuracaoRepository.findConfig()
        .orElseGet(() -> ConfiguracaoGeral.builder().id(1L).build());
    return mapEntityToDto(config);
  }

  // Buscar Entidade Configuração Geral //
  @Transactional(readOnly = true)
  public Optional<ConfiguracaoGeral> buscarEntidadeConfiguracao() {
    return configuracaoRepository.findConfig();
  }

  // Salvar ou Atualizar Configuração Geral //
  @Transactional
  public ConfiguracaoGeralDTO salvarOuAtualizarConfiguracao(ConfiguracaoGeralDTO dto) {
    ConfiguracaoGeral config = configuracaoRepository.findConfig()
        .orElseGet(() -> ConfiguracaoGeral.builder().id(1L).build());

    config.setTaxaJurosAtraso(dto.taxaJuros());
    config.setPrazoPagamentoFiado(dto.prazoPagamento());
    config.setNameNegocio(dto.nameNegocio());
    ConfiguracaoGeral configSalva = configuracaoRepository.save(config);

    return mapEntityToDto(configSalva);
  }

  // Mapper //
  private ConfiguracaoGeralDTO mapEntityToDto(ConfiguracaoGeral entity) {
    return new ConfiguracaoGeralDTO(
        entity.getTaxaJurosAtraso(),
        entity.getPrazoPagamentoFiado(),
        entity.getDataAtualizacao(),
        entity.getNameNegocio());
  }

}