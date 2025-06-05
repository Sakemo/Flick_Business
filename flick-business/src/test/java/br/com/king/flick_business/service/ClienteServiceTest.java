package br.com.king.flick_business.service;

import br.com.king.flick_business.dto.ClienteRequestDTO;
import br.com.king.flick_business.dto.ClienteResponseDTO;
import br.com.king.flick_business.entity.Cliente;
import br.com.king.flick_business.exception.RecursoJaCadastrado;
import br.com.king.flick_business.exception.RecursoNaoDeletavel;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.mapper.ClienteMapper; // Mapper é estático
import br.com.king.flick_business.repository.ClienteRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor; // Usar @Captor
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort; // Importar Sort

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections; // Para lista vazia
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

  @Mock
  private ClienteRepository clienteRepositoryMock;

  @InjectMocks
  private ClienteService clienteService;

  @Captor // Captor para verificar a entidade Cliente salva/atualizada
  private ArgumentCaptor<Cliente> clienteCaptor;
  @Captor // Captor para o objeto Sort
  private ArgumentCaptor<Sort> sortCaptor;

  private Cliente clienteExistente;
  private ClienteRequestDTO clienteRequestDTO;
  private ClienteRequestDTO clienteRequestAtualizacaoDTO;
  private final Long idExistente = 1L;
  private final Long idInexistente = 99L;
  private final String cpfExistente = "11122233344";
  private final String cpfNovo = "55566677788";

  @BeforeEach
  void setUp() {
    clienteExistente = Cliente.builder()
        .id(idExistente)
        .nome("Cliente Teste Existente")
        .cpf(cpfExistente)
        .telefone("11999998888")
        .endereco("Rua Teste, 123")
        .controleFiado(true)
        .limiteFiado(new BigDecimal("100.00"))
        .saldoDevedor(BigDecimal.ZERO)
        .ativo(true)
        .dataCadastro(LocalDateTime.now().minusDays(10))
        .dataUltimaCompraFiado(LocalDateTime.now().minusDays(5))
        .dataAtualizacao(LocalDateTime.now().minusDays(1))
        .build();

    clienteRequestDTO = new ClienteRequestDTO(
        "Novo Cliente",
        cpfNovo,
        "22888887777",
        "Avenida Nova, 456",
        false, // controleFiado
        new BigDecimal("50.00"), // limiteFiado
        true); // ativo

    clienteRequestAtualizacaoDTO = new ClienteRequestDTO(
        "Cliente Teste Atualizado",
        cpfExistente,
        "11777776666",
        "Rua Atualizada, 789",
        true,
        new BigDecimal("150.00"),
        false // ativo
    );
  }

  // --- Testes salvar, atualizar, buscarPorId, deletar, ativarInativar ---
  // (Estes testes permanecem muito similares ao que você já tinha, pois a lógica
  // principal desses métodos no service não mudou drasticamente em relação aos
  // filtros)

  @Test
  @DisplayName("salvar: Deve salvar cliente com sucesso quando CPF não existe")
  void salvar_quandoCpfNaoExiste_deveRetornarDtoSalvo() {
    // Arrange
    when(clienteRepositoryMock.findByCpf(cpfNovo)).thenReturn(Optional.empty());
    Cliente clienteParaSalvar = ClienteMapper.toEntity(clienteRequestDTO);
    // Simula o retorno do save com ID e timestamps (Lombok @Builder.Default já lida
    // com ativo e saldoDevedor)
    Cliente clienteSalvoMock = Cliente.builder()
        .id(2L)
        .nome(clienteRequestDTO.nome())
        .cpf(clienteRequestDTO.cpf())
        .telefone(clienteRequestDTO.telefone())
        .endereco(clienteRequestDTO.endereco())
        .controleFiado(clienteRequestDTO.controleFiado())
        .limiteFiado(clienteRequestDTO.limiteFiado())
        .ativo(clienteRequestDTO.ativo() != null ? clienteRequestDTO.ativo() : true) // Garante o default
        .saldoDevedor(BigDecimal.ZERO) // Default
        .dataCadastro(LocalDateTime.now())
        .dataAtualizacao(LocalDateTime.now())
        .build();
    when(clienteRepositoryMock.save(any(Cliente.class))).thenReturn(clienteSalvoMock);

    // Act
    ClienteResponseDTO resultado = clienteService.salvar(clienteRequestDTO);

    // Assert
    assertNotNull(resultado);
    assertEquals(clienteSalvoMock.getId(), resultado.id());
    assertEquals(clienteRequestDTO.nome(), resultado.nome());
    verify(clienteRepositoryMock).findByCpf(cpfNovo);
    verify(clienteRepositoryMock).save(any(Cliente.class));
  }

  @Test
  @DisplayName("salvar: Deve lançar exceção ao salvar se CPF já existe")
  void salvar_quandoCpfJaExiste_deveLancarRecursoJaCadastrado() {
    when(clienteRepositoryMock.findByCpf(cpfNovo)).thenReturn(Optional.of(clienteExistente));
    assertThrows(RecursoJaCadastrado.class, () -> clienteService.salvar(clienteRequestDTO));
    verify(clienteRepositoryMock).findByCpf(cpfNovo);
    verify(clienteRepositoryMock, never()).save(any(Cliente.class));
  }

  @Test
  @DisplayName("atualizar: Deve atualizar cliente com sucesso")
  void atualizar_quandoClienteEncontradoECpfValido_deveRetornarDtoAtualizado() {
    when(clienteRepositoryMock.findById(idExistente)).thenReturn(Optional.of(clienteExistente));
    when(clienteRepositoryMock.findByCpf(clienteRequestAtualizacaoDTO.cpf())).thenReturn(Optional.of(clienteExistente)); // Mesmo
                                                                                                                         // CPF
    when(clienteRepositoryMock.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

    ClienteResponseDTO resultado = clienteService.atualizar(idExistente, clienteRequestAtualizacaoDTO);

    assertNotNull(resultado);
    assertEquals(idExistente, resultado.id());
    assertEquals(clienteRequestAtualizacaoDTO.nome(), resultado.nome());
    verify(clienteRepositoryMock).findById(idExistente);
    verify(clienteRepositoryMock).findByCpf(clienteRequestAtualizacaoDTO.cpf());
    verify(clienteRepositoryMock).save(clienteCaptor.capture());
    assertEquals(clienteRequestAtualizacaoDTO.nome(), clienteCaptor.getValue().getNome());
    assertEquals(clienteRequestAtualizacaoDTO.ativo(), clienteCaptor.getValue().getAtivo());
  }

  // ... (Outros testes de salvar/atualizar com falha, buscarPorId, deletar,
  // ativarInativar
  // permanecem estruturalmente os mesmos que você já tinha) ...

  // --- NOVOS Testes para o método listarTodos com Filtros e Ordenação ---

  @Test
  @DisplayName("listarTodos: Deve chamar findClienteComFiltros com parâmetros corretos e Sort default (dataCadastro DESC)")
  void listarTodos_semOrderByEspecifico_deveUsarSortDefault() {
    // Arrange
    Boolean apenasAtivos = true;
    Boolean devedores = null; // Sem filtro de devedores
    String nomeContains = "Teste";
    List<Cliente> listaMock = List.of(clienteExistente);

    // Mock para a query findClienteComFiltros
    when(clienteRepositoryMock.findClienteComFiltros(
        eq(nomeContains),
        eq(apenasAtivos),
        eq(devedores),
        any(Sort.class) // Capturaremos o Sort para verificar
    )).thenReturn(listaMock);

    // Act
    clienteService.listarTodos(apenasAtivos, devedores, null, nomeContains); // orderBy = null

    // Assert / Verify
    verify(clienteRepositoryMock).findClienteComFiltros(
        eq(nomeContains),
        eq(apenasAtivos),
        eq(devedores),
        sortCaptor.capture() // Captura o objeto Sort
    );
    Sort capturedSort = sortCaptor.getValue();
    assertNotNull(capturedSort.getOrderFor("dataCadastro"));
    assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("dataCadastro").getDirection());
  }

  @Test
  @DisplayName("listarTodos: Deve chamar findClienteComFiltros com Sort por nome ASC")
  void listarTodos_comOrderByNomeAsc_devePassarSortNomeAsc() {
    // Arrange
    String orderBy = "nomeAsc";
    when(clienteRepositoryMock.findClienteComFiltros(any(), any(), any(), any(Sort.class)))
        .thenReturn(Collections.emptyList());

    // Act
    clienteService.listarTodos(true, null, orderBy, null);

    // Assert / Verify
    verify(clienteRepositoryMock).findClienteComFiltros(
        isNull(), // nomeContains
        eq(true), // apenasAtivos
        isNull(), // devedores
        sortCaptor.capture());
    Sort capturedSort = sortCaptor.getValue();
    assertNotNull(capturedSort.getOrderFor("nome"));
    assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("nome").getDirection());
  }

  @Test
  @DisplayName("listarTodos: Deve chamar findClienteComFiltros com filtro de devedores=true")
  void listarTodos_comFiltroDevedoresTrue_devePassarDevedoresTrue() {
    // Arrange
    Boolean devedores = true;
    when(clienteRepositoryMock.findClienteComFiltros(any(), any(), eq(devedores), any(Sort.class)))
        .thenReturn(Collections.emptyList());

    // Act
    clienteService.listarTodos(true, devedores, null, null);

    // Assert / Verify
    verify(clienteRepositoryMock).findClienteComFiltros(
        isNull(),
        eq(true),
        eq(true), // Verifica se 'devedores' foi passado como true
        any(Sort.class));
  }

  @Test
  @DisplayName("listarTodos: Deve chamar findClienteComFiltros com filtro apenasAtivos=false")
  void listarTodos_comApenasAtivosFalse_devePassarApenasAtivosFalse() {
    // Arrange
    Boolean apenasAtivos = false;
    when(clienteRepositoryMock.findClienteComFiltros(any(), eq(apenasAtivos), any(), any(Sort.class)))
        .thenReturn(Collections.emptyList());

    // Act
    clienteService.listarTodos(apenasAtivos, null, "nomeAsc", null);

    // Assert / Verify
    verify(clienteRepositoryMock).findClienteComFiltros(
        isNull(),
        eq(false), // Verifica se 'apenasAtivos' foi passado como false
        isNull(),
        any(Sort.class));
  }

  @Test
  @DisplayName("listarTodos: Deve passar todos os filtros e ordenação corretamente")
  void listarTodos_comTodosOsFiltrosEOrder_deveChamarComTodosOsArgumentos() {
    // Arrange
    String nome = "Cliente";
    Boolean apenasAtivos = true;
    Boolean devedores = false;
    String orderBy = "saldoAsc";
    List<Cliente> listaMock = List.of(clienteExistente);

    when(clienteRepositoryMock.findClienteComFiltros(eq(nome), eq(apenasAtivos), eq(devedores), any(Sort.class)))
        .thenReturn(listaMock);

    // Act
    List<ClienteResponseDTO> resultado = clienteService.listarTodos(apenasAtivos, devedores, orderBy, nome);

    // Assert
    assertNotNull(resultado);
    // Adicionar mais asserções sobre o resultado se necessário

    // Verify
    verify(clienteRepositoryMock).findClienteComFiltros(
        eq(nome),
        eq(apenasAtivos),
        eq(devedores),
        sortCaptor.capture());
    Sort capturedSort = sortCaptor.getValue();
    assertNotNull(capturedSort.getOrderFor("saldoDevedor"));
    assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("saldoDevedor").getDirection());
  }
}