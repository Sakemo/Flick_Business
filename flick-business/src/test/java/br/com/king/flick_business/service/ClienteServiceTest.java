package br.com.king.flick_business.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull; // Mapper é estático, não precisa mockar
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach; // Não esqueça a importação estática completa do Mockito
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test; // Para nomes de teste mais legíveis
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any; // Para capturar argumentos passados aos mocks
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.king.flick_business.dto.ClienteRequestDTO;
import br.com.king.flick_business.dto.ClienteResponseDTO;
import br.com.king.flick_business.entity.Cliente;
import br.com.king.flick_business.exception.RecursoJaCadastrado;
import br.com.king.flick_business.exception.RecursoNaoDeletavel;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.mapper.ClienteMapper;
import br.com.king.flick_business.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class) // Habilita integração Mockito com JUnit 5
class ClienteServiceTest {

  @Mock // Cria um mock (objeto falso) para o repositório
  private ClienteRepository clienteRepositoryMock;

  // Não precisamos mockar ClienteMapper, pois seus métodos são estáticos.

  @InjectMocks // Cria uma instância real do ClienteService, injetando os mocks declarados
               // acima
  private ClienteService clienteService;

  // --- Variáveis de Teste Reutilizáveis ---
  private Cliente clienteExistente;
  private ClienteRequestDTO clienteRequestDTO;
  private ClienteRequestDTO clienteRequestAtualizacaoDTO;
  private final Long idExistente = 1L;
  private final Long idInexistente = 99L;
  private final String cpfExistente = "11122233344";
  private final String cpfNovo = "55566677788";

  @BeforeEach // Executado antes de cada método @Test
  @SuppressWarnings("unused")
  void setUp() {
    // Configura um cliente padrão que existe no "banco mockado"
    clienteExistente = Cliente.builder()
        .id(idExistente)
        .nome("Cliente Teste Existente")
        .cpf(cpfExistente)
        .telefone("11999998888")
        .endereco("Rua Teste, 123")
        .limiteFiado(new BigDecimal("100.00"))
        .saldoDevedor(BigDecimal.ZERO) // Importante para o teste de delete
        .ativo(true)
        .dataCadastro(LocalDateTime.now().minusDays(1))
        .dataAtualizacao(LocalDateTime.now())
        .build();

    // DTO para criar um novo cliente
    clienteRequestDTO = new ClienteRequestDTO(
        "Novo Cliente",
        cpfNovo,
        "22888887777",
        "Avenida Nova, 456",
        new BigDecimal("50.00"),
        true);

    // DTO para atualizar o cliente existente
    clienteRequestAtualizacaoDTO = new ClienteRequestDTO(
        "Cliente Teste Atualizado",
        cpfExistente, // Mantendo o mesmo CPF na atualização de exemplo
        "11777776666",
        "Rua Atualizada, 789",
        new BigDecimal("150.00"),
        true);
  }

  // --- Testes para o método salvar ---

  @Test
  @DisplayName("Deve salvar cliente com sucesso quando CPF não existe")
  void salvar_quandoCpfNaoExiste_deveRetornarDtoSalvo() {
    // Arrange
    // Simula que não encontrou cliente com o CPF novo
    when(clienteRepositoryMock.findByCpf(cpfNovo)).thenReturn(Optional.empty());
    // Simula o 'save' retornando o cliente com ID (usamos o próprio
    // 'clienteExistente' como base, mas com dados do DTO)
    @SuppressWarnings("unused")
    Cliente clienteParaSalvar = ClienteMapper.toEntity(clienteRequestDTO); // Cria entidade baseada no DTO
    Cliente clienteSalvo = Cliente.builder() // Simula o retorno do save com ID
        .id(2L) // Novo ID simulado
        .nome(clienteRequestDTO.nome())
        .cpf(clienteRequestDTO.cpf())
        .telefone(clienteRequestDTO.telefone())
        .endereco(clienteRequestDTO.endereco())
        .limiteFiado(clienteRequestDTO.limiteFiado())
        .saldoDevedor(BigDecimal.ZERO)
        .ativo(true)
        .build();
    when(clienteRepositoryMock.save(any(Cliente.class))).thenReturn(clienteSalvo); // any() porque o objeto exato pode
                                                                                   // diferir um pouco

    // Act
    ClienteResponseDTO resultado = clienteService.salvar(clienteRequestDTO);

    // Assert
    assertNotNull(resultado);
    assertEquals(clienteSalvo.getId(), resultado.id());
    assertEquals(clienteRequestDTO.nome(), resultado.nome());
    assertEquals(clienteRequestDTO.cpf(), resultado.cpf());

    // Verifica se os métodos corretos do repositório foram chamados
    verify(clienteRepositoryMock).findByCpf(cpfNovo);
    verify(clienteRepositoryMock).save(any(Cliente.class)); // Verifica se save foi chamado
  }

  @Test
  @DisplayName("Deve lançar exceção ao salvar se CPF já existe")
  void salvar_quandoCpfJaExiste_deveLancarRecursoJaCadastrado() {
    // Arrange
    // Simula que ENCONTROU um cliente com o CPF do DTO
    when(clienteRepositoryMock.findByCpf(cpfNovo)).thenReturn(Optional.of(clienteExistente));

    // Act & Assert
    RecursoJaCadastrado exception = assertThrows(RecursoJaCadastrado.class, () -> {
      clienteService.salvar(clienteRequestDTO);
    });

    // Verifica a mensagem da exceção
    assertTrue(exception.getMessage().contains("Já existe um cliente cadastrado com o CPF: " + cpfNovo));

    // Verifica que findByCpf foi chamado, mas save NUNCA foi chamado
    verify(clienteRepositoryMock).findByCpf(cpfNovo);
    verify(clienteRepositoryMock, never()).save(any(Cliente.class));
  }

  // --- Testes para o método atualizar ---

  @Test
  @DisplayName("Deve atualizar cliente com sucesso quando encontrado e CPF não pertence a outro")
  void atualizar_quandoClienteEncontradoECpfValido_deveRetornarDtoAtualizado() {
    // Arrange
    // 1. Simula encontrar o cliente a ser atualizado
    when(clienteRepositoryMock.findById(idExistente)).thenReturn(Optional.of(clienteExistente));
    // 2. Simula a validação de CPF (ou não encontra outro, ou encontra o próprio
    // cliente)
    when(clienteRepositoryMock.findByCpf(clienteRequestAtualizacaoDTO.cpf())).thenReturn(Optional.of(clienteExistente)); // CPF
                                                                                                                         // pertence
                                                                                                                         // ao
                                                                                                                         // próprio
                                                                                                                         // cliente
    // 3. Simula o save retornando o cliente atualizado (pode ser o mesmo objeto
    // modificado)
    when(clienteRepositoryMock.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Retorna
                                                                                                              // o mesmo
                                                                                                              // objeto
                                                                                                              // que
                                                                                                              // recebeu

    // Act
    ClienteResponseDTO resultado = clienteService.atualizar(idExistente, clienteRequestAtualizacaoDTO);

    // Assert
    assertNotNull(resultado);
    assertEquals(idExistente, resultado.id());
    assertEquals(clienteRequestAtualizacaoDTO.nome(), resultado.nome()); // Verifica se o nome foi atualizado
    assertEquals(clienteRequestAtualizacaoDTO.telefone(), resultado.telefone()); // Verifica telefone

    // Verifica as chamadas aos mocks
    verify(clienteRepositoryMock).findById(idExistente);
    verify(clienteRepositoryMock).findByCpf(clienteRequestAtualizacaoDTO.cpf());
    verify(clienteRepositoryMock).save(any(Cliente.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao atualizar se cliente não for encontrado")
  void atualizar_quandoClienteNaoEncontrado_deveLancarRecursoNaoEncontrado() {
    // Arrange
    when(clienteRepositoryMock.findById(idInexistente)).thenReturn(Optional.empty());

    // Act & Assert
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
      clienteService.atualizar(idInexistente, clienteRequestAtualizacaoDTO);
    });
    assertEquals("Cliente não encontrado com o ID: " + idInexistente, exception.getMessage());

    // Verifica
    verify(clienteRepositoryMock).findById(idInexistente);
    verify(clienteRepositoryMock, never()).findByCpf(anyString());
    verify(clienteRepositoryMock, never()).save(any(Cliente.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao atualizar se CPF pertence a outro cliente")
  void atualizar_quandoCpfPertenceOutroCliente_deveLancarRecursoJaCadastrado() {
    // Arrange
    Cliente outroClienteComCpf = Cliente.builder().id(5L).cpf(cpfNovo).build(); // Outro cliente tem o CPF desejado
    ClienteRequestDTO dtoComCpfDeOutro = new ClienteRequestDTO("Nome", cpfNovo, "tel", "end", BigDecimal.ONE, true);

    when(clienteRepositoryMock.findById(idExistente)).thenReturn(Optional.of(clienteExistente));
    // Simula encontrar OUTRO cliente com o CPF que queremos usar na atualização
    when(clienteRepositoryMock.findByCpf(cpfNovo)).thenReturn(Optional.of(outroClienteComCpf));

    // Act & Assert
    RecursoJaCadastrado exception = assertThrows(RecursoJaCadastrado.class, () -> {
      clienteService.atualizar(idExistente, dtoComCpfDeOutro);
    });
    assertTrue(exception.getMessage().contains("Já existe um cliente cadastrado com o CPF: " + cpfNovo));

    // Verifica
    verify(clienteRepositoryMock).findById(idExistente);
    verify(clienteRepositoryMock).findByCpf(cpfNovo);
    verify(clienteRepositoryMock, never()).save(any(Cliente.class));
  }

  // --- Testes para o método buscarPorId ---

  @Test
  @DisplayName("Deve retornar cliente quando ID existe")
  void buscarPorId_quandoIdExiste_deveRetornarClienteResponseDTO() {
    // Arrange
    when(clienteRepositoryMock.findById(idExistente)).thenReturn(Optional.of(clienteExistente));

    // Act
    ClienteResponseDTO resultado = clienteService.buscarPorId(idExistente);

    // Assert
    assertNotNull(resultado);
    assertEquals(idExistente, resultado.id());
    assertEquals(clienteExistente.getNome(), resultado.nome());

    verify(clienteRepositoryMock).findById(idExistente);
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar por ID inexistente")
  void buscarPorId_quandoIdNaoExiste_deveLancarRecursoNaoEncontrado() {
    // Arrange
    when(clienteRepositoryMock.findById(idInexistente)).thenReturn(Optional.empty());

    // Act & Assert
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class,
        () -> clienteService.buscarPorId(idInexistente));
    assertEquals("Cliente não encontrado com o ID: " + idInexistente, exception.getMessage());
    verify(clienteRepositoryMock).findById(idInexistente);
  }

  // --- Testes para o método listarTodos ---

  @Test
  @DisplayName("Deve retornar lista de clientes ativos quando apenasAtivos for true")
  void listarTodos_quandoApenasAtivosTrue_deveChamarFindByAtivoTrue() {
    // Arrange
    List<Cliente> listaAtivos = List.of(clienteExistente); // Exemplo com um cliente ativo
    when(clienteRepositoryMock.findByAtivoTrue()).thenReturn(listaAtivos);

    // Act
    List<ClienteResponseDTO> resultado = clienteService.listarTodos(true);

    // Assert
    assertNotNull(resultado);
    assertFalse(resultado.isEmpty());
    assertEquals(1, resultado.size());
    assertEquals(clienteExistente.getNome(), resultado.get(0).nome());

    verify(clienteRepositoryMock).findByAtivoTrue();
    verify(clienteRepositoryMock, never()).findAll(); // Garante que findAll não foi chamado
  }

  @Test
  @DisplayName("Deve retornar lista de todos os clientes quando apenasAtivos for false")
  void listarTodos_quandoApenasAtivosFalse_deveChamarFindAll() {
    // Arrange
    Cliente clienteInativo = Cliente.builder().id(3L).nome("Inativo").ativo(false).build();
    List<Cliente> listaTodos = List.of(clienteExistente, clienteInativo);
    when(clienteRepositoryMock.findAll()).thenReturn(listaTodos);

    // Act
    List<ClienteResponseDTO> resultado = clienteService.listarTodos(false);

    // Assert
    assertNotNull(resultado);
    assertEquals(2, resultado.size());

    verify(clienteRepositoryMock, never()).findByAtivoTrue();
    verify(clienteRepositoryMock).findAll(); // Garante que findAll foi chamado
  }

  // --- Testes para o método deletar ---

  @Test
  @DisplayName("Deve inativar cliente (deletar logicamente) quando encontrado e sem saldo devedor")
  void deletar_quandoClienteEncontradoSemSaldo_deveSetarAtivoFalseESalvar() {
    // Arrange
    // O clienteExistente já tem saldo ZERO no setUp()
    when(clienteRepositoryMock.findById(idExistente)).thenReturn(Optional.of(clienteExistente));
    // Precisamos capturar o objeto Cliente que é passado para o save
    ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);

    // Act
    clienteService.deletar(idExistente);

    // Assert
    verify(clienteRepositoryMock).findById(idExistente);
    // Verifica se o save foi chamado e captura o argumento
    verify(clienteRepositoryMock).save(clienteCaptor.capture());
    // Pega o cliente que foi capturado
    Cliente clienteSalvo = clienteCaptor.getValue();
    // Verifica se o campo ativo foi setado para false ANTES de salvar
    assertFalse(clienteSalvo.getAtivo());
  }

  @Test
  @DisplayName("Deve lançar exceção ao deletar cliente não encontrado")
  void deletar_quandoClienteNaoEncontrado_deveLancarRecursoNaoEncontrado() {
    // Arrange
    when(clienteRepositoryMock.findById(idInexistente)).thenReturn(Optional.empty());

    // Act & Assert
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class,
        () -> clienteService.deletar(idInexistente));
    assertEquals("Cliente não encontrado com o ID: " + idInexistente, exception.getMessage());
    verify(clienteRepositoryMock).findById(idInexistente);
    verify(clienteRepositoryMock, never()).save(any(Cliente.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao deletar cliente com saldo devedor")
  void deletar_quandoClienteComSaldoDevedor_deveLancarRecursoNaoDeletavel() {
    // Arrange
    Cliente clienteComSaldo = Cliente.builder()
        .id(idExistente)
        .saldoDevedor(new BigDecimal("10.50")) // Saldo > 0
        .ativo(true)
        .build();
    when(clienteRepositoryMock.findById(idExistente)).thenReturn(Optional.of(clienteComSaldo));

    // Act & Assert
    RecursoNaoDeletavel exception = assertThrows(RecursoNaoDeletavel.class, () -> {
      clienteService.deletar(idExistente);
    });
    assertEquals("Cliente com saldo devedor não pode ser deletado", exception.getMessage());

    // Verifica
    verify(clienteRepositoryMock).findById(idExistente);
    verify(clienteRepositoryMock, never()).save(any(Cliente.class));
  }

  // --- Testes para o método ativarInativar ---

  @Test
  @DisplayName("Deve ativar um cliente inativo")
  void ativarInativar_quandoAtivarClienteInativo_deveSetarAtivoTrueESalvar() {
    // Arrange
    Cliente clienteInativo = Cliente.builder().id(idExistente).ativo(false).build();
    when(clienteRepositoryMock.findById(idExistente)).thenReturn(Optional.of(clienteInativo));
    when(clienteRepositoryMock.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
    ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);

    // Act
    ClienteResponseDTO resultado = clienteService.ativarInativar(idExistente, true);

    // Assert
    assertNotNull(resultado);
    assertTrue(resultado.ativo()); // Verifica no DTO retornado
    verify(clienteRepositoryMock).save(clienteCaptor.capture());
    assertTrue(clienteCaptor.getValue().getAtivo()); // Verifica o estado do objeto salvo
    verify(clienteRepositoryMock).findById(idExistente);
  }

  @Test
  @DisplayName("Deve inativar um cliente ativo")
  void ativarInativar_quandoInativarClienteAtivo_deveSetarAtivoFalseESalvar() {
    // Arrange
    // O clienteExistente está ativo no setUp()
    when(clienteRepositoryMock.findById(idExistente)).thenReturn(Optional.of(clienteExistente));
    when(clienteRepositoryMock.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
    ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);

    // Act
    ClienteResponseDTO resultado = clienteService.ativarInativar(idExistente, false);

    // Assert
    assertNotNull(resultado);
    assertFalse(resultado.ativo());
    verify(clienteRepositoryMock).save(clienteCaptor.capture());
    assertFalse(clienteCaptor.getValue().getAtivo());
    verify(clienteRepositoryMock).findById(idExistente);
  }

  @Test
  @DisplayName("Deve lançar exceção ao ativar/inativar cliente não encontrado")
  void ativarInativar_quandoClienteNaoEncontrado_deveLancarRecursoNaoEncontrado() {
    // Arrange
    when(clienteRepositoryMock.findById(idInexistente)).thenReturn(Optional.empty());

    // Act & Assert
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class,
        () -> clienteService.ativarInativar(idInexistente, true));
    assertEquals("Cliente não encontrado com o ID: " + idInexistente, exception.getMessage());
    verify(clienteRepositoryMock).findById(idInexistente);
    verify(clienteRepositoryMock, never()).save(any(Cliente.class));
  }

}