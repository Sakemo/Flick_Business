package br.com.king.flick_business.service;

import br.com.king.flick_business.dto.DespesaRequestDTO;
import br.com.king.flick_business.dto.DespesaResponseDTO;
import br.com.king.flick_business.entity.Despesa;
import br.com.king.flick_business.enums.TipoDespesa;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.repository.DespesaRepository;

import static org.junit.jupiter.api.Assertions.*; // JUnit Assertions
import static org.mockito.Mockito.*; // Mockito static functions

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime; // Para criar início/fim do dia
import java.util.Collections; // Para lista vazia
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class) // Habilita Mockito com JUnit 5
class DespesaServiceTest {

  @Mock // Mock do repositório
  private DespesaRepository despesaRepositoryMock;

  @InjectMocks // Instância real do serviço com mocks injetados
  private DespesaService despesaService;

  @Captor // Captor para verificar a entidade salva/atualizada
  private ArgumentCaptor<Despesa> despesaCaptor;

  // --- Dados de Teste Reutilizáveis ---
  private Despesa despesaExistente;
  private DespesaRequestDTO despesaRequestDTO; // Para criar nova
  private DespesaRequestDTO despesaRequestAtualizacaoDTO; // Para atualizar
  private final Long idExistente = 1L;
  private final Long idInexistente = 99L;
  private LocalDateTime dataRef; // Data de referência para os testes

  @BeforeEach
  void setUp() {
    dataRef = LocalDateTime.now().with(LocalTime.NOON); // Meio-dia de hoje como referência

    despesaExistente = Despesa.builder()
        .id(idExistente).nome("Almoço Reunião")

        .valor(new BigDecimal("75.50"))
        .dataDespesa(dataRef.minusDays(1)) // Ontem
        .tipoDespesa(TipoDespesa.EMPRESARIAL)
        .dataCriacao(LocalDateTime.now().minusDays(2))
        .dataAtualizacao(LocalDateTime.now().minusDays(1))
        .build();

    // DTO para criar uma nova despesa
    despesaRequestDTO = new DespesaRequestDTO(
        "Café da Manhã",
        new BigDecimal("15.80"),
        dataRef, // Hoje
        TipoDespesa.PESSOAL);

    // DTO para atualizar a despesa existente
    despesaRequestAtualizacaoDTO = new DespesaRequestDTO(
        "Almoço Reunião C/ Cliente", // Nome atualizado
        new BigDecimal("82.00"), // Valor atualizado
        dataRef.minusDays(1), // Data mantida
        TipoDespesa.EMPRESARIAL // Tipo mantido
    );
  }

  // --- Testes salvarDespesa ---

  @Test
  @DisplayName("Deve salvar despesa com dados válidos e retornar DTO salvo")
  void salvarDespesa_comDadosValidos_deveRetornarDtoSalvo() {

    // Simula
    when(despesaRepositoryMock.save(any(Despesa.class))).thenAnswer(invocation -> {
      Despesa d = invocation.getArgument(0);
      d.setId(2L); // Simula novo ID
      d.setDataCriacao(LocalDateTime.now()); // Simula @CreationTimestamp
      d.setDataAtualizacao(LocalDateTime.now()); // Simula @UpdateTimestamp
      return d;
    });

    // Act
    DespesaResponseDTO resultado = despesaService.salvarDespesa(despesaRequestDTO);

    // Assert
    assertNotNull(resultado);
    assertNotNull(resultado.id()); // Verifica se ID foi gerado
    assertEquals(despesaRequestDTO.nome(), resultado.nome());
    assertEquals(0, despesaRequestDTO.valor().compareTo(resultado.valor())); // Comparar BigDecimals
    assertEquals(despesaRequestDTO.dataDespesa(), resultado.dataDespesa());
    assertEquals(despesaRequestDTO.tipoDespesa(), resultado.tipoDespesa());
    assertNotNull(resultado.dataCriacao());
    assertNotNull(resultado.dataAtualizacao());

    // Verify
    verify(despesaRepositoryMock).save(despesaCaptor.capture());
    Despesa despesaSalva = despesaCaptor.getValue();
    // Verifica se os dados do DTO foram mapeados corretamente para a entidade antes
    // de salvar
    assertEquals(despesaRequestDTO.nome(), despesaSalva.getNome());
    assertEquals(0, despesaRequestDTO.valor().compareTo(despesaSalva.getValor()));
  }

  // --- Testes atualizarDespesa ---
  @Test
  @DisplayName("Deve atualizar despesa existente com sucesso")
  void atualizarDespesa_quandoExiste_deveRetornarDtoAtualizado() {
    // Arrange
    // 1. Simula encontrar a despesa pelo ID
    when(despesaRepositoryMock.findById(idExistente)).thenReturn(Optional.of(despesaExistente));

    // 2. Cria um objeto mock *separado* para representar o estado APÓS o save
    LocalDateTime dataAtualizadaSimulada = despesaExistente.getDataAtualizacao().plusSeconds(5); // Garante 5 segundos
                                                                                                 // depois
    Despesa despesaSalvaMock = Despesa.builder()
        .id(idExistente)
        .nome(despesaRequestAtualizacaoDTO.nome()) // Nome atualizado
        .valor(despesaRequestAtualizacaoDTO.valor()) // Valor atualizado
        .dataDespesa(despesaRequestAtualizacaoDTO.dataDespesa())
        .tipoDespesa(despesaRequestAtualizacaoDTO.tipoDespesa())
        .dataCriacao(despesaExistente.getDataCriacao()) // Mantém data de criação
        .dataAtualizacao(dataAtualizadaSimulada) // Usa a data simulada posterior
        .build();
    // Simula que o save retorna este novo objeto mockado
    when(despesaRepositoryMock.save(any(Despesa.class))).thenReturn(despesaSalvaMock);

    // Act
    DespesaResponseDTO resultado = despesaService.atualizarDespesa(idExistente, despesaRequestAtualizacaoDTO);

    // Assert DTO retornado
    assertNotNull(resultado);
    assertEquals(idExistente, resultado.id());
    assertEquals(despesaRequestAtualizacaoDTO.nome(), resultado.nome());
    assertEquals(0, despesaRequestAtualizacaoDTO.valor().compareTo(resultado.valor()));
    // Verifica a data de atualização no DTO retornado
    assertEquals(dataAtualizadaSimulada, resultado.dataAtualizacao()); // Compara com a data simulada
    // A asserção original pode ser removida ou mantida se quiser garantir que é
    // posterior à inicial
    // assertTrue(resultado.dataAtualizacao().isAfter(despesaExistente.getDataAtualizacao()));
    // // Deve passar agora

    // Assert Entidade Capturada pelo save (O que FOI passado para o save)
    verify(despesaRepositoryMock).save(despesaCaptor.capture());
    Despesa despesaPassadaParaSave = despesaCaptor.getValue();
    // Verifica se a entidade passada para save tinha os dados atualizados do DTO
    assertEquals(despesaRequestAtualizacaoDTO.nome(), despesaPassadaParaSave.getNome());

    // (porque é o @UpdateTimestamp que deveria fazer isso, ou o nosso mock do save)
    assertEquals(despesaExistente.getDataAtualizacao(), despesaPassadaParaSave.getDataAtualizacao());

    // Verify findById
    verify(despesaRepositoryMock).findById(idExistente);
  }

  @Test
  @DisplayName("Deve lançar exceção ao atualizar despesa inexistente")
  void atualizarDespesa_quandoNaoExiste_deveLancarRecursoNaoEncontrado() {
    // Arrange
    when(despesaRepositoryMock.findById(idInexistente)).thenReturn(Optional.empty());

    // Act & Assert
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
      despesaService.atualizarDespesa(idInexistente, despesaRequestAtualizacaoDTO);
    });
    assertEquals("Despesa não encontrada com o ID: " + idInexistente, exception.getMessage());

    // Verify
    verify(despesaRepositoryMock).findById(idInexistente);
    verify(despesaRepositoryMock, never()).save(any(Despesa.class)); // Save não deve ser chamado
  }

  // --- Testes listarDespesas ---

  @Test
  @DisplayName("Deve retornar lista filtrada por data quando início e fim são fornecidos")
  void listarDespesas_comFiltroData_deveChamarFindByDataDespesaBetween() {
    // Arrange
    LocalDateTime inicio = dataRef.minusDays(2).with(LocalTime.MIN); // Dois dias atrás
    LocalDateTime fim = dataRef.minusDays(1).with(LocalTime.MAX); // Ontem até fim do dia
    // Simula o repositório retornando a despesaExistente (que ocorreu ontem)
    when(despesaRepositoryMock.findByDataDespesaBetweenOrderByDataDespesaDesc(inicio, fim))
        .thenReturn(List.of(despesaExistente));

    // Act
    List<DespesaResponseDTO> resultado = despesaService.listarDespesas(inicio, fim);

    // Assert
    assertNotNull(resultado);
    assertEquals(1, resultado.size());
    assertEquals(despesaExistente.getId(), resultado.get(0).id());

    // Verify
    verify(despesaRepositoryMock).findByDataDespesaBetweenOrderByDataDespesaDesc(inicio, fim);
    verify(despesaRepositoryMock, never()).findAll(); // Não deve chamar findAll
  }

  @Test
  @DisplayName("Deve retornar lista do dia atual quando início e fim são nulos")
  void listarDespesas_semFiltroData_deveBuscarPeloDiaAtual() {
    // Arrange
    LocalDateTime inicioHoje = LocalDateTime.now().toLocalDate().atStartOfDay();
    LocalDateTime fimHoje = inicioHoje.plusDays(1).minusNanos(1);
    // Simula que não há despesas hoje
    when(despesaRepositoryMock.findByDataDespesaBetweenOrderByDataDespesaDesc(eq(inicioHoje), any(LocalDateTime.class))) // Usar
                                                                                                                         // eq()
                                                                                                                         // para
                                                                                                                         // data
                                                                                                                         // exata
        .thenReturn(Collections.emptyList());

    // Act
    List<DespesaResponseDTO> resultado = despesaService.listarDespesas(null, null);

    // Assert
    assertNotNull(resultado);
    assertTrue(resultado.isEmpty());

    // Verify
    // Verifica se a busca foi feita com as datas calculadas para hoje
    verify(despesaRepositoryMock).findByDataDespesaBetweenOrderByDataDespesaDesc(eq(inicioHoje),
        any(LocalDateTime.class));
    verify(despesaRepositoryMock, never()).findAll();
  }

  // --- Testes buscarDespesaPorId ---

  @Test
  @DisplayName("Deve retornar DTO quando ID existe")
  void buscarDespesaPorId_quandoExiste_deveRetornarDto() {
    // Arrange
    when(despesaRepositoryMock.findById(idExistente)).thenReturn(Optional.of(despesaExistente));

    // Act
    DespesaResponseDTO resultado = despesaService.buscarDespesaPorId(idExistente);

    // Assert
    assertNotNull(resultado);
    assertEquals(idExistente, resultado.id());
    assertEquals(despesaExistente.getNome(), resultado.nome());

    // Verify
    verify(despesaRepositoryMock).findById(idExistente);
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar por ID inexistente")
  void buscarDespesaPorId_quandoNaoExiste_deveLancarRecursoNaoEncontrado() {
    // Arrange
    when(despesaRepositoryMock.findById(idInexistente)).thenReturn(Optional.empty());

    // Act & Assert
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
      despesaService.buscarDespesaPorId(idInexistente);
    });
    assertEquals("Despesa não encontrada com o ID: " + idInexistente, exception.getMessage());

    // Verify
    verify(despesaRepositoryMock).findById(idInexistente);
  }

  // --- Testes deletarDespesa ---

  @Test
  @DisplayName("Deve chamar delete no repositório quando ID existe")
  void deletarDespesa_quandoExiste_deveChamarDelete() {
    // Arrange
    when(despesaRepositoryMock.findById(idExistente)).thenReturn(Optional.of(despesaExistente));
    // Método delete é void, não precisamos mockar retorno, só verificar chamada
    doNothing().when(despesaRepositoryMock).delete(any(Despesa.class));

    // Act
    despesaService.deletarDespesa(idExistente);

    // Assert / Verify
    verify(despesaRepositoryMock).findById(idExistente);
    verify(despesaRepositoryMock).delete(despesaExistente); // Verifica se delete foi chamado com a entidade correta
  }

  @Test
  @DisplayName("Deve lançar exceção ao deletar ID inexistente")
  void deletarDespesa_quandoNaoExiste_deveLancarRecursoNaoEncontrado() {
    // Arrange
    when(despesaRepositoryMock.findById(idInexistente)).thenReturn(Optional.empty());

    // Act & Assert
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
      despesaService.deletarDespesa(idInexistente);
    });
    assertEquals("Despesa não encontrada com o ID: " + idInexistente, exception.getMessage());

    // Verify
    verify(despesaRepositoryMock).findById(idInexistente);
    verify(despesaRepositoryMock, never()).delete(any(Despesa.class)); // Garante que delete não foi chamado
  }
}