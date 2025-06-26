package br.com.king.flick_business.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import br.com.king.flick_business.dto.request.ClienteRequestDTO;
import br.com.king.flick_business.dto.response.ClienteResponseDTO;
import br.com.king.flick_business.entity.Cliente;
import br.com.king.flick_business.exception.RecursoJaCadastrado;
import br.com.king.flick_business.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

  @Mock
  private ClienteRepository clienteRepositoryMock;

  @InjectMocks
  private ClienteService clienteService;

  @Captor
  private ArgumentCaptor<Cliente> clienteCaptor;
  @Captor
  private ArgumentCaptor<Sort> sortCaptor;

  private Cliente clienteExistente;
  private ClienteRequestDTO clienteRequestDTO;
  private ClienteRequestDTO clienteRequestAtualizacaoDTO;
  private final Long idExistente = 1L;

  @BeforeEach
  void setUp() {
    clienteExistente = Cliente.builder()
        .id(idExistente).nome("Cliente Teste Existente").cpf("11122233344")
        .telefone("11999998888").endereco("Rua Teste, 123")
        .controleFiado(true).limiteFiado(new BigDecimal("100.00"))
        .saldoDevedor(BigDecimal.ZERO).ativo(true)
        .dataCadastro(LocalDateTime.now().minusDays(10))
        .dataUltimaCompraFiado(LocalDateTime.now().minusDays(5))
        .dataAtualizacao(LocalDateTime.now().minusDays(1))
        .build();

    clienteRequestDTO = new ClienteRequestDTO("Novo Cliente", "55566677788", "22888887777", "Avenida Nova, 456", false,
        new BigDecimal("50.00"), true);
    clienteRequestAtualizacaoDTO = new ClienteRequestDTO("Cliente Teste Atualizado", "11122233344", "11777776666",
        "Rua Atualizada, 789", true, new BigDecimal("150.00"), false);
  }

  @Test
  @DisplayName("salvar: Deve salvar cliente com sucesso quando CPF não existe")
  void salvar_quandoCpfNaoExiste_deveRetornarDtoSalvo() {
    when(clienteRepositoryMock.findByCpf(clienteRequestDTO.cpf())).thenReturn(Optional.empty());
    Cliente clienteSalvoMock = Cliente.builder().id(2L).nome(clienteRequestDTO.nome()).cpf(clienteRequestDTO.cpf())
        .ativo(true).saldoDevedor(BigDecimal.ZERO).controleFiado(false).build();
    when(clienteRepositoryMock.save(any(Cliente.class))).thenReturn(clienteSalvoMock);
    ClienteResponseDTO resultado = clienteService.salvar(clienteRequestDTO);
    assertNotNull(resultado);
    assertEquals(2L, resultado.id());
  }

  @Test
  @DisplayName("salvar: Deve lançar exceção ao salvar se CPF já existe")
  void salvar_quandoCpfJaExiste_deveLancarRecursoJaCadastrado() {
    when(clienteRepositoryMock.findByCpf(clienteRequestDTO.cpf())).thenReturn(Optional.of(clienteExistente));
    assertThrows(RecursoJaCadastrado.class, () -> clienteService.salvar(clienteRequestDTO));
  }

  @Test
  @DisplayName("atualizar: Deve atualizar cliente com sucesso")
  void atualizar_quandoClienteEncontradoECpfValido_deveRetornarDtoAtualizado() {
    when(clienteRepositoryMock.findById(idExistente)).thenReturn(Optional.of(clienteExistente));
    when(clienteRepositoryMock.findByCpf(clienteRequestAtualizacaoDTO.cpf())).thenReturn(Optional.of(clienteExistente));
    when(clienteRepositoryMock.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
    ClienteResponseDTO resultado = clienteService.atualizar(idExistente, clienteRequestAtualizacaoDTO);
    assertNotNull(resultado);
    assertEquals(clienteRequestAtualizacaoDTO.nome(), resultado.nome());
  }

  @Test
  @DisplayName("buscarPorId: Deve retornar cliente quando ID existe")
  void buscarPorId_quandoIdExiste_deveRetornarClienteResponseDTO() {
    when(clienteRepositoryMock.findById(idExistente)).thenReturn(Optional.of(clienteExistente));
    ClienteResponseDTO resultado = clienteService.buscarPorId(idExistente);
    assertNotNull(resultado);
    assertEquals(idExistente, resultado.id());
  }

  @Test
  @DisplayName("deletar: Deve inativar cliente quando sem saldo devedor")
  void deletar_quandoClienteEncontradoSemSaldo_deveSetarAtivoFalseESalvar() {
    when(clienteRepositoryMock.findById(idExistente)).thenReturn(Optional.of(clienteExistente));
    clienteService.deletar(idExistente);
    verify(clienteRepositoryMock).save(clienteCaptor.capture());
    assertFalse(clienteCaptor.getValue().getAtivo());
  }

  @Test
  @DisplayName("ativarInativar: Deve ativar um cliente inativo")
  void ativarInativar_quandoAtivarClienteInativo_deveSetarAtivoTrueESalvar() {
    Cliente clienteInativo = Cliente.builder().id(idExistente).ativo(false).saldoDevedor(BigDecimal.ZERO).build();
    when(clienteRepositoryMock.findById(idExistente)).thenReturn(Optional.of(clienteInativo));
    when(clienteRepositoryMock.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
    ClienteResponseDTO resultado = clienteService.ativarInativar(idExistente, true);
    assertTrue(resultado.ativo());
  }

  @Test
  @DisplayName("listarTodos: Deve chamar findClienteComFiltros com nomeContains nulo e Sort default quando outros filtros são nulos")
  void listarTodos_semFiltrosEspecificos_deveUsarNomeNullESortDefault() {
    Boolean apenasAtivos = true;
    when(clienteRepositoryMock.findClienteComFiltros(eq(""), eq(apenasAtivos), isNull(), any(Sort.class)))
        .thenReturn(Collections.emptyList());

    clienteService.listarTodos(apenasAtivos, null, null, null);

    verify(clienteRepositoryMock).findClienteComFiltros(eq(""), eq(apenasAtivos), isNull(), sortCaptor.capture());
    Sort capturedSort = sortCaptor.getValue();
    assertNotNull(capturedSort.getOrderFor("dataCadastro"), "Default sort deve ser por dataCadastro");
    assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("dataCadastro").getDirection());
  }

  @Test
  @DisplayName("listarTodos: Deve chamar findClienteComFiltros com Sort por nome ASC quando orderBy é 'nomeAsc'")
  void listarTodos_comOrderByNomeAsc_devePassarSortNomeAsc() {
    // Arrange
    String orderBy = "nomeAsc";
    Boolean apenasAtivos = true;
    when(clienteRepositoryMock.findClienteComFiltros(eq(""), eq(apenasAtivos), isNull(), any(Sort.class)))
        .thenReturn(Collections.emptyList());

    // Act
    clienteService.listarTodos(apenasAtivos, null, orderBy, null);

    // Assert / Verify
    verify(clienteRepositoryMock).findClienteComFiltros(
        eq(""), // nomeContains
        eq(apenasAtivos), // apenasAtivos
        isNull(), // devedores
        sortCaptor.capture());
    Sort capturedSort = sortCaptor.getValue();
    assertNotNull(capturedSort.getOrderFor("nome"), "Sort deve ser por nome");
    assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("nome").getDirection());
  }

  @Test
  @DisplayName("listarTodos: Deve chamar findClienteComFiltros com filtro de devedores=true e Sort default")
  void listarTodos_comFiltroDevedoresTrue_devePassarDevedoresTrueESortDefault() {
    // Arrange
    Boolean devedores = true;
    Boolean apenasAtivos = true;
    when(clienteRepositoryMock.findClienteComFiltros(eq(""), eq(apenasAtivos), eq(devedores), any(Sort.class)))
        .thenReturn(Collections.emptyList());

    // Act
    clienteService.listarTodos(apenasAtivos, devedores, null, null); // orderBy é null

    // Assert / Verify
    verify(clienteRepositoryMock).findClienteComFiltros(
        eq(""),
        eq(apenasAtivos), // apenasAtivos
        eq(true), // devedores
        sortCaptor.capture());
    Sort capturedSort = sortCaptor.getValue(); // Verifica o sort default
    assertNotNull(capturedSort.getOrderFor("dataCadastro"));
    assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("dataCadastro").getDirection());
  }

  @Test
  @DisplayName("listarTodos: Deve chamar findClienteComFiltros com filtro apenasAtivos=false e Sort especificado")
  void listarTodos_comApenasAtivosFalseEOrderBy_devePassarFiltrosCorretos() {
    // Arrange
    Boolean apenasAtivos = false;
    String orderBy = "nomeDesc";
    when(clienteRepositoryMock.findClienteComFiltros(eq(""), eq(apenasAtivos), isNull(), any(Sort.class)))
        .thenReturn(Collections.emptyList());

    // Act
    clienteService.listarTodos(apenasAtivos, null, orderBy, null);

    // Assert / Verify
    verify(clienteRepositoryMock).findClienteComFiltros(
        eq(""), // nomeContains
        eq(false), // apenasAtivos
        isNull(), // devedores
        sortCaptor.capture());
    Sort capturedSort = sortCaptor.getValue();
    assertNotNull(capturedSort.getOrderFor("nome"));
    assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("nome").getDirection());
  }

  @Test
  @DisplayName("listarTodos: Deve passar nomeContains corretamente para o repositório")
  void listarTodos_comNomeContains_devePassarNomeCorretamente() {
    // Arrange
    String nomeBusca = "Teste";
    Boolean apenasAtivos = true;
    when(clienteRepositoryMock.findClienteComFiltros(eq(nomeBusca), eq(apenasAtivos), isNull(), any(Sort.class)))
        .thenReturn(List.of(clienteExistente));

    // Act
    clienteService.listarTodos(apenasAtivos, null, null, nomeBusca);

    // Assert / Verify
    verify(clienteRepositoryMock).findClienteComFiltros(
        eq(nomeBusca), // nomeContains
        eq(apenasAtivos), // apenasAtivos
        isNull(), // devedores
        any(Sort.class) // Para o Sort default
    );
  }

  @Test
  @DisplayName("listarTodos: Deve passar nomeContains como null se string vazia ou só espaços")
  void listarTodos_comNomeContainsVazioOuEspacos_devePassarStringVazia() {
    // Arrange
    Boolean apenasAtivos = true;
    when(clienteRepositoryMock.findClienteComFiltros(eq(""), eq(apenasAtivos), isNull(), any(Sort.class)))
        .thenReturn(Collections.emptyList());

    // Act
    clienteService.listarTodos(apenasAtivos, null, null, "   "); // nomeContains com espaços

    // Assert / Verify
    verify(clienteRepositoryMock).findClienteComFiltros(
        eq(""), // nomeContains deve ser convertido para null
        eq(apenasAtivos),
        isNull(),
        any(Sort.class));
  }
}