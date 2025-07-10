package br.com.king.flick_business.service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
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
        .id(idExistente).name("Cliente Teste Existente").cpf("11122233344")
        .telefone("11999998888").endereco("Rua Teste, 123")
        .controleFiado(true).limiteFiado(new BigDecimal("100.00"))
        .saldoDevedor(BigDecimal.ZERO).active(true)
        .dataCadastro(ZonedDateTime.now().minusDays(10))
        .dataUltimaCompraFiado(ZonedDateTime.now().minusDays(5))
        .dataAtualizacao(ZonedDateTime.now().minusDays(1))
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
    Cliente clienteSalvoMock = Cliente.builder().id(2L).name(clienteRequestDTO.name()).cpf(clienteRequestDTO.cpf())
        .active(true).saldoDevedor(BigDecimal.ZERO).controleFiado(false).build();
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
    assertEquals(clienteRequestAtualizacaoDTO.name(), resultado.name());
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
  @DisplayName("delete: Deve inativar cliente quando sem saldo devedor")
  void delete_quandoClienteEncontradoSemSaldo_deveSetarActiveFalseESalvar() {
    when(clienteRepositoryMock.findById(idExistente)).thenReturn(Optional.of(clienteExistente));
    clienteService.delete(idExistente);
    verify(clienteRepositoryMock).save(clienteCaptor.capture());
    assertFalse(clienteCaptor.getValue().getActive());
  }

  @Test
  @DisplayName("ativarInativar: Deve ativar um cliente inactive")
  void ativarInativar_quandoAtivarClienteInactive_deveSetarActiveTrueESalvar() {
    Cliente clienteInactive = Cliente.builder().id(idExistente).active(false).saldoDevedor(BigDecimal.ZERO).build();
    when(clienteRepositoryMock.findById(idExistente)).thenReturn(Optional.of(clienteInactive));
    when(clienteRepositoryMock.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
    ClienteResponseDTO resultado = clienteService.ativarInativar(idExistente, true);
    assertTrue(resultado.active());
  }

  @Test
  @DisplayName("listTodos: Deve chamar findClienteComFilters com nameContains nulo e Sort default quando outros filtros são nulos")
  void listTodos_semFiltersEspecificos_deveUsarNameNullESortDefault() {
    Boolean apenasActives = true;
    when(clienteRepositoryMock.findClienteComFilters(eq(""), eq(apenasActives), isNull(), any(Sort.class)))
        .thenReturn(Collections.emptyList());

    clienteService.listTodos(apenasActives, null, null, null);

    verify(clienteRepositoryMock).findClienteComFilters(eq(""), eq(apenasActives), isNull(), sortCaptor.capture());
    Sort capturedSort = sortCaptor.getValue();
    assertNotNull(capturedSort.getOrderFor("dataCadastro"), "Default sort deve ser por dataCadastro");
    assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("dataCadastro").getDirection());
  }

  @Test
  @DisplayName("listTodos: Deve chamar findClienteComFilters com Sort por name ASC quando orderBy é 'nameAsc'")
  void listTodos_comOrderByNameAsc_devePassarSortNameAsc() {
    // Arrange
    String orderBy = "nameAsc";
    Boolean apenasActives = true;
    when(clienteRepositoryMock.findClienteComFilters(eq(""), eq(apenasActives), isNull(), any(Sort.class)))
        .thenReturn(Collections.emptyList());

    // Act
    clienteService.listTodos(apenasActives, null, orderBy, null);

    // Assert / Verify
    verify(clienteRepositoryMock).findClienteComFilters(
        eq(""), // nameContains
        eq(apenasActives), // apenasActives
        isNull(), // devedores
        sortCaptor.capture());
    Sort capturedSort = sortCaptor.getValue();
    assertNotNull(capturedSort.getOrderFor("name"), "Sort deve ser por name");
    assertEquals(Sort.Direction.ASC, capturedSort.getOrderFor("name").getDirection());
  }

  @Test
  @DisplayName("listTodos: Deve chamar findClienteComFilters com filtro de devedores=true e Sort default")
  void listTodos_comFilterDevedoresTrue_devePassarDevedoresTrueESortDefault() {
    // Arrange
    Boolean devedores = true;
    Boolean apenasActives = true;
    when(clienteRepositoryMock.findClienteComFilters(eq(""), eq(apenasActives), eq(devedores), any(Sort.class)))
        .thenReturn(Collections.emptyList());

    // Act
    clienteService.listTodos(apenasActives, devedores, null, null); // orderBy é null

    // Assert / Verify
    verify(clienteRepositoryMock).findClienteComFilters(
        eq(""),
        eq(apenasActives), // apenasActives
        eq(true), // devedores
        sortCaptor.capture());
    Sort capturedSort = sortCaptor.getValue(); // Verifica o sort default
    assertNotNull(capturedSort.getOrderFor("dataCadastro"));
    assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("dataCadastro").getDirection());
  }

  @Test
  @DisplayName("listTodos: Deve chamar findClienteComFilters com filtro apenasActives=false e Sort especificado")
  void listTodos_comApenasActivesFalseEOrderBy_devePassarFiltersCorretos() {
    // Arrange
    Boolean apenasActives = false;
    String orderBy = "nameDesc";
    when(clienteRepositoryMock.findClienteComFilters(eq(""), eq(apenasActives), isNull(), any(Sort.class)))
        .thenReturn(Collections.emptyList());

    // Act
    clienteService.listTodos(apenasActives, null, orderBy, null);

    // Assert / Verify
    verify(clienteRepositoryMock).findClienteComFilters(
        eq(""), // nameContains
        eq(false), // apenasActives
        isNull(), // devedores
        sortCaptor.capture());
    Sort capturedSort = sortCaptor.getValue();
    assertNotNull(capturedSort.getOrderFor("name"));
    assertEquals(Sort.Direction.DESC, capturedSort.getOrderFor("name").getDirection());
  }

  @Test
  @DisplayName("listTodos: Deve passar nameContains corretamente para o repositório")
  void listTodos_comNameContains_devePassarNameCorretamente() {
    // Arrange
    String nameBusca = "Teste";
    Boolean apenasActives = true;
    when(clienteRepositoryMock.findClienteComFilters(eq(nameBusca), eq(apenasActives), isNull(), any(Sort.class)))
        .thenReturn(List.of(clienteExistente));

    // Act
    clienteService.listTodos(apenasActives, null, null, nameBusca);

    // Assert / Verify
    verify(clienteRepositoryMock).findClienteComFilters(
        eq(nameBusca), // nameContains
        eq(apenasActives), // apenasActives
        isNull(), // devedores
        any(Sort.class) // Para o Sort default
    );
  }

  @Test
  @DisplayName("listTodos: Deve passar nameContains como null se string vazia ou só espaços")
  void listTodos_comNameContainsVazioOuEspacos_devePassarStringVazia() {
    // Arrange
    Boolean apenasActives = true;
    when(clienteRepositoryMock.findClienteComFilters(eq(""), eq(apenasActives), isNull(), any(Sort.class)))
        .thenReturn(Collections.emptyList());

    // Act
    clienteService.listTodos(apenasActives, null, null, "   "); // nameContains com espaços

    // Assert / Verify
    verify(clienteRepositoryMock).findClienteComFilters(
        eq(""), // nameContains deve ser convertido para null
        eq(apenasActives),
        isNull(),
        any(Sort.class));
  }
}