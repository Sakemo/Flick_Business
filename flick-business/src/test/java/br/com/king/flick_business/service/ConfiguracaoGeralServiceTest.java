package br.com.king.flick_business.service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals; // Asserções do JUnit
import static org.junit.jupiter.api.Assertions.assertFalse; // Funções estáticas do Mockito
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName; // Usar @Captor
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.king.flick_business.dto.ConfiguracaoGeralDTO;
import br.com.king.flick_business.entity.ConfiguracaoGeral;
import br.com.king.flick_business.repository.ConfiguracaoGeralRepository;

@ExtendWith(MockitoExtension.class) // Habilita Mockito com JUnit 5
class ConfiguracaoGeralServiceTest {

  @Mock // Mock para o repositório
  private ConfiguracaoGeralRepository configuracaoRepositoryMock;

  @InjectMocks // Instância real do serviço com o mock injetado
  private ConfiguracaoGeralService configuracaoService;

  @Captor // Captor para verificar o objeto salvo
  private ArgumentCaptor<ConfiguracaoGeral> configCaptor;

  // Dados de teste
  private ConfiguracaoGeral configExistente;
  private ConfiguracaoGeralDTO configDtoParaSalvar;
  private final Long CONFIG_ID = 1L; // ID fixo usado pelo serviço

  @BeforeEach
  @SuppressWarnings("unused")
  void setUp() {
    // Configuração que simularemos existir no banco
    configExistente = ConfiguracaoGeral.builder()
        .id(CONFIG_ID)
        .taxaJurosAtraso(new BigDecimal("5.50"))
        .prazoPagamentoFiado(2)
        .nameNegocio("Loja Existente") // Incluindo o name do negócio
        .updatedAt(ZonedDateTime.now().minusHours(1))
        .build();

    // DTO com novos dados para save/atualizar
    configDtoParaSalvar = new ConfiguracaoGeralDTO(
        new BigDecimal("7.00"), // Nova taxa
        3, // Novo prazo
        null, // Data de atualização não vem no DTO de request
        "Loja Atualizada LTDA" // Novo name
    );
  }

  // --- Testes para buscarConfiguracao / buscarEntidadeConfiguracao ---

  @Test
  @DisplayName("buscarConfiguracao: Deve retornar DTO com dados quando configuração existe")
  void buscarConfiguracao_quandoExiste_deveRetornarDtoMapeado() {
    // Arrange
    // Simula que o repositório encontra a configuração com ID 1
    when(configuracaoRepositoryMock.findConfig()).thenReturn(Optional.of(configExistente));
    // when(configuracaoRepositoryMock.findById(CONFIG_ID)).thenReturn(Optional.of(configExistente));
    // // Alternativa

    // Act
    ConfiguracaoGeralDTO resultado = configuracaoService.buscarConfiguracao();

    // Assert
    assertNotNull(resultado);
    assertEquals(configExistente.getTaxaJurosAtraso(), resultado.taxaJuros());
    assertEquals(configExistente.getPrazoPagamentoFiado(), resultado.prazoPagamento());
    assertEquals(configExistente.getNameNegocio(), resultado.nameNegocio()); // Verificar name
    assertEquals(configExistente.getUpdatedAt(), resultado.updatedAt());

    // Verify
    verify(configuracaoRepositoryMock).findConfig(); // Verifica a chamada ao método default
  }

  @Test
  @DisplayName("buscarConfiguracao: Deve retornar DTO default (campos nulos/default) quando configuração não existe")
  void buscarConfiguracao_quandoNaoExiste_deveRetornarDtoDefault() {
    // Arrange
    // Simula que o repositório NÃO encontra a configuração com ID 1
    when(configuracaoRepositoryMock.findConfig()).thenReturn(Optional.empty());
    // when(configuracaoRepositoryMock.findById(CONFIG_ID)).thenReturn(Optional.empty());
    // // Alternativa

    // Act
    ConfiguracaoGeralDTO resultado = configuracaoService.buscarConfiguracao();

    // Assert
    assertNotNull(resultado);
    // Verifica se os campos que podem ser nulos estão nulos
    assertNull(resultado.taxaJuros());
    assertNull(resultado.prazoPagamento());
    assertNull(resultado.nameNegocio()); // Verificar name
    // Data de atualização também será nula pois veio do builder default
    assertNull(resultado.updatedAt());

    // Verify
    verify(configuracaoRepositoryMock).findConfig();
  }

  @Test
  @DisplayName("buscarEntidadeConfiguracao: Deve retornar Optional com entidade quando existe")
  void buscarEntidadeConfiguracao_quandoExiste_deveRetornarOptionalComEntidade() {
    // Arrange
    when(configuracaoRepositoryMock.findConfig()).thenReturn(Optional.of(configExistente));

    // Act
    Optional<ConfiguracaoGeral> resultadoOpt = configuracaoService.buscarEntidadeConfiguracao();

    // Assert
    assertTrue(resultadoOpt.isPresent());
    assertEquals(configExistente, resultadoOpt.get());

    // Verify
    verify(configuracaoRepositoryMock).findConfig();
  }

  @Test
  @DisplayName("buscarEntidadeConfiguracao: Deve retornar Optional vazio quando não existe")
  void buscarEntidadeConfiguracao_quandoNaoExiste_deveRetornarOptionalVazio() {
    // Arrange
    when(configuracaoRepositoryMock.findConfig()).thenReturn(Optional.empty());

    // Act
    Optional<ConfiguracaoGeral> resultadoOpt = configuracaoService.buscarEntidadeConfiguracao();

    // Assert
    assertFalse(resultadoOpt.isPresent());

    // Verify
    verify(configuracaoRepositoryMock).findConfig();
  }

  // --- Testes para saveOuAtualizarConfiguracao ---

  @Test
  @DisplayName("saveOuAtualizar: Deve ATUALIZAR configuração existente com dados do DTO")
  void saveOuAtualizarConfiguracao_quandoExiste_deveAtualizarCamposESalvar() {
    // Arrange
    // 1. Simula que a configuração já existe
    when(configuracaoRepositoryMock.findConfig()).thenReturn(Optional.of(configExistente));
    // 2. Simula o save retornando a entidade modificada (que será capturada)
    when(configuracaoRepositoryMock.save(any(ConfiguracaoGeral.class))).thenAnswer(inv -> {
      ConfiguracaoGeral c = inv.getArgument(0);
      c.setUpdatedAt(ZonedDateTime.now()); // Simula o @UpdateTimestamp
      return c;
    });

    // Act
    ConfiguracaoGeralDTO resultadoDTO = configuracaoService.saveOuAtualizarConfiguracao(configDtoParaSalvar);

    // Assert DTO Retornado
    assertNotNull(resultadoDTO);
    assertEquals(configDtoParaSalvar.taxaJuros(), resultadoDTO.taxaJuros());
    assertEquals(configDtoParaSalvar.prazoPagamento(), resultadoDTO.prazoPagamento());
    assertEquals(configDtoParaSalvar.nameNegocio(), resultadoDTO.nameNegocio()); // Verificar name
    assertNotNull(resultadoDTO.updatedAt()); // Data deve ter sido atualizada

    // Assert Entidade Salva usando Captor
    verify(configuracaoRepositoryMock).save(configCaptor.capture());
    ConfiguracaoGeral configSalva = configCaptor.getValue();

    assertEquals(CONFIG_ID, configSalva.getId()); // ID deve ser 1L
    assertEquals(configDtoParaSalvar.taxaJuros(), configSalva.getTaxaJurosAtraso());
    assertEquals(configDtoParaSalvar.prazoPagamento(), configSalva.getPrazoPagamentoFiado());
    assertEquals(configDtoParaSalvar.nameNegocio(), configSalva.getNameNegocio()); // Verificar name
    // Verifica se a entidade existente foi modificada (opcional, mas bom)
    assertSame(configExistente, configSalva, "A entidade existente deveria ter sido atualizada");

    // Verify
    verify(configuracaoRepositoryMock).findConfig(); // Verifica se buscou antes de save
  }

  @Test
  @DisplayName("saveOuAtualizar: Deve CRIAR configuração com ID 1 quando não existe")
  void saveOuAtualizarConfiguracao_quandoNaoExiste_deveCriarComId1ESalvar() {
    // Arrange
    // 1. Simula que a configuração NÃO existe
    when(configuracaoRepositoryMock.findConfig()).thenReturn(Optional.empty());
    // 2. Simula o save (retorna o objeto que foi passado, com data atualizada)
    when(configuracaoRepositoryMock.save(any(ConfiguracaoGeral.class))).thenAnswer(inv -> {
      ConfiguracaoGeral c = inv.getArgument(0);
      c.setUpdatedAt(ZonedDateTime.now()); // Simula @UpdateTimestamp
      return c;
    });

    // Act
    ConfiguracaoGeralDTO resultadoDTO = configuracaoService.saveOuAtualizarConfiguracao(configDtoParaSalvar);

    // Assert DTO Retornado
    assertNotNull(resultadoDTO);
    assertEquals(configDtoParaSalvar.taxaJuros(), resultadoDTO.taxaJuros());
    assertEquals(configDtoParaSalvar.prazoPagamento(), resultadoDTO.prazoPagamento());
    assertEquals(configDtoParaSalvar.nameNegocio(), resultadoDTO.nameNegocio()); // Verificar name
    assertNotNull(resultadoDTO.updatedAt());

    // Assert Entidade Salva usando Captor
    verify(configuracaoRepositoryMock).save(configCaptor.capture());
    ConfiguracaoGeral configSalva = configCaptor.getValue();

    assertEquals(CONFIG_ID, configSalva.getId()); // Garante que o ID é 1L
    assertEquals(configDtoParaSalvar.taxaJuros(), configSalva.getTaxaJurosAtraso());
    assertEquals(configDtoParaSalvar.prazoPagamento(), configSalva.getPrazoPagamentoFiado());
    assertEquals(configDtoParaSalvar.nameNegocio(), configSalva.getNameNegocio()); // Verificar name

    // Verify
    verify(configuracaoRepositoryMock).findConfig(); // Verifica se tentou buscar antes
  }
}