package br.com.king.flick_business.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor; // Usar @Captor
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor; // Importar Sort para testes de listagem com filtro
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.king.flick_business.dto.ProdutoRequestDTO;
import br.com.king.flick_business.dto.ProdutoResponseDTO;
import br.com.king.flick_business.entity.Categoria;
import br.com.king.flick_business.entity.Fornecedor;
import br.com.king.flick_business.entity.Produto;
import br.com.king.flick_business.enums.TipoUnidadeVenda;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.mapper.ProdutoMapper;
import br.com.king.flick_business.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

  @Mock
  private ProdutoRepository produtoRepositoryMock;
  @Mock
  private ProdutoMapper produtoMapperMock;
  @Mock
  private CategoriaService categoriaServiceMock;
  @Mock
  private FornecedorService fornecedorServiceMock;

  @InjectMocks
  private ProdutoService produtoService;

  @Captor
  private ArgumentCaptor<Produto> produtoCaptor;
  // Não precisamos de sortCaptor aqui, pois ProdutoService.listarTodos(Long) não
  // usa Sort diretamente

  private Categoria mockCategoria;
  private Fornecedor mockFornecedor;
  private Produto mockProduto;
  private ProdutoRequestDTO mockRequestDTO;
  private ProdutoResponseDTO mockResponseDTO; // Para buscarPorId e mock do mapper
  private final Long existingId = 1L;
  private final Long nonExistingId = 99L;
  private final Long categoriaId = 1L;
  private final Long fornecedorId = 1L;

  @BeforeEach
  void setUp() {
    mockCategoria = Categoria.builder().id(categoriaId).nome("Eletrônicos").build();
    // Ajustar Fornecedor.builder() se TipoPessoa não for mais usado diretamente ou
    // for opcional
    mockFornecedor = Fornecedor.builder()
        .id(fornecedorId)
        .nome("Fornecedor Teste")
        // .tipoPessoa(TipoPessoa.JURIDICA) // Remover ou ajustar se Fornecedor mudou
        // .cnpjCpf("11.222.333/0001-44")
        .build();

    mockProduto = Produto.builder()
        .id(existingId).nome("Produto Teste").descricao("Descrição Teste").codigoBarras("123456")
        .precoVenda(BigDecimal.TEN).precoCustoUnitario(BigDecimal.valueOf(5))
        .quantidadeEstoque(BigDecimal.valueOf(10)).tipoUnidadeVenda(TipoUnidadeVenda.UNIDADE)
        .categoria(mockCategoria).fornecedor(mockFornecedor).ativo(true)
        .criadoEm(LocalDateTime.now().minusDays(1)).atualizadoEm(LocalDateTime.now())
        .build();

    // Ordem dos campos DEVE bater com ProdutoRequestDTO.java do backend
    mockRequestDTO = new ProdutoRequestDTO(
        "Produto Novo", // nome
        "Desc Nova", // descricao
        "789012", // codigoBarras
        BigDecimal.valueOf(20), // quantidadeEstoque
        BigDecimal.valueOf(15.50), // precoVenda
        BigDecimal.valueOf(7.25), // precoCustoUnitario
        TipoUnidadeVenda.UNIDADE, // tipoUnidadeVenda
        true, // ativo
        categoriaId, // categoriaId
        fornecedorId // fornecedorId
    );

    // mockResponseDTO construído a partir de mockProduto
    mockResponseDTO = new ProdutoResponseDTO(mockProduto);
  }

  @Test
  @DisplayName("buscarPorId: Deve retornar ProdutoResponseDTO quando ID existe")
  void buscarPorId_quandoIdExiste_deveRetornarProdutoResponseDTO() {
    when(produtoRepositoryMock.findById(existingId)).thenReturn(Optional.of(mockProduto));
    when(produtoMapperMock.toResponseDTO(mockProduto)).thenReturn(mockResponseDTO);

    ProdutoResponseDTO resultado = produtoService.buscarPorId(existingId);

    assertNotNull(resultado);
    assertEquals(mockResponseDTO.id(), resultado.id());
    assertEquals(mockResponseDTO.nome(), resultado.nome());
    assertNotNull(resultado.categoria());
    assertEquals(mockCategoria.getNome(), resultado.categoria().getNome());
    // Se quiser comparar o DTO inteiro, certifique-se que ProdutoResponseDTO
    // (record)
    // tem equals/hashCode bem definidos para os objetos Categoria e Fornecedor,
    // ou compare os objetos aninhados campo a campo.
    // assertEquals(mockResponseDTO, resultado); // Pode ser problemático se os
    // objetos aninhados não tiverem equals/hashCode adequados

    verify(produtoRepositoryMock).findById(existingId);
    verify(produtoMapperMock).toResponseDTO(mockProduto);
  }

  @Test
  @DisplayName("buscarPorId: Deve lançar RecursoNaoEncontrado quando ID não existe")
  void buscarPorId_quandoIdNaoExiste_deveLancarRecursoNaoEncontrado() {
    when(produtoRepositoryMock.findById(nonExistingId)).thenReturn(Optional.empty());
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class,
        () -> produtoService.buscarPorId(nonExistingId));
    assertEquals("Produto não encontrado com ID: " + nonExistingId, exception.getMessage());
    verify(produtoRepositoryMock).findById(nonExistingId);
    verify(produtoMapperMock, never()).toResponseDTO(any());
  }

  @Test
  @DisplayName("salvar: Deve salvar e retornar ProdutoResponseDTO quando dados válidos")
  void salvar_quandoDadosValidos_deveRetornarProdutoResponseDTOSalvo() {
    when(categoriaServiceMock.buscarEntidadePorId(categoriaId)).thenReturn(mockCategoria);
    when(fornecedorServiceMock.buscarEntidadePorId(fornecedorId)).thenReturn(mockFornecedor);

    Produto produtoParaSalvar = // ... (criação do produtoParaSalvar como antes)
        Produto.builder().nome(mockRequestDTO.nome()).descricao(mockRequestDTO.descricao())
            .codigoBarras(mockRequestDTO.codigoBarras()).quantidadeEstoque(mockRequestDTO.quantidadeEstoque())
            .precoVenda(mockRequestDTO.precoVenda()).precoCustoUnitario(mockRequestDTO.precoCustoUnitario())
            .tipoUnidadeVenda(mockRequestDTO.tipoUnidadeVenda()).ativo(mockRequestDTO.ativo())
            .categoria(mockCategoria).fornecedor(mockFornecedor).build();
    when(produtoMapperMock.toEntity(eq(mockRequestDTO), eq(mockCategoria), eq(mockFornecedor)))
        .thenReturn(produtoParaSalvar);

    Produto produtoSalvoComId = Produto.builder().id(2L) // Atribui ID
        // Copia outros campos de produtoParaSalvar
        .nome(produtoParaSalvar.getNome()).descricao(produtoParaSalvar.getDescricao())
        .codigoBarras(produtoParaSalvar.getCodigoBarras()).quantidadeEstoque(produtoParaSalvar.getQuantidadeEstoque())
        .precoVenda(produtoParaSalvar.getPrecoVenda()).precoCustoUnitario(produtoParaSalvar.getPrecoCustoUnitario())
        .tipoUnidadeVenda(produtoParaSalvar.getTipoUnidadeVenda()).ativo(produtoParaSalvar.isAtivo())
        .categoria(produtoParaSalvar.getCategoria()).fornecedor(produtoParaSalvar.getFornecedor())
        .criadoEm(LocalDateTime.now()).atualizadoEm(LocalDateTime.now()).build();
    when(produtoRepositoryMock.save(eq(produtoParaSalvar))).thenReturn(produtoSalvoComId);

    ProdutoResponseDTO dtoEsperado = new ProdutoResponseDTO(produtoSalvoComId);
    when(produtoMapperMock.toResponseDTO(eq(produtoSalvoComId))).thenReturn(dtoEsperado);

    ProdutoResponseDTO resultado = produtoService.salvar(mockRequestDTO);

    assertNotNull(resultado);
    assertEquals(dtoEsperado.id(), resultado.id());
    assertEquals(dtoEsperado.nome(), resultado.nome());
    assertNotNull(resultado.categoria());
    assertEquals(dtoEsperado.categoria().getNome(), resultado.categoria().getNome());

    verify(categoriaServiceMock).buscarEntidadePorId(categoriaId);
    verify(fornecedorServiceMock).buscarEntidadePorId(fornecedorId);
    verify(produtoMapperMock).toEntity(mockRequestDTO, mockCategoria, mockFornecedor);
    verify(produtoRepositoryMock).save(produtoParaSalvar);
    verify(produtoMapperMock).toResponseDTO(produtoSalvoComId);
  }

  // ... (Testes para salvar com categoria/fornecedor não encontrado permanecem os
  // mesmos) ...
  @Test
  @DisplayName("salvar: Deve lançar RecursoNaoEncontrado se Categoria não existe")
  void salvar_quandoCategoriaNaoExiste_deveLancarRecursoNaoEncontrado() {
    when(categoriaServiceMock.buscarEntidadePorId(categoriaId))
        .thenThrow(new RecursoNaoEncontrado("Categoria não encontrada"));
    assertThrows(RecursoNaoEncontrado.class, () -> produtoService.salvar(mockRequestDTO));
    verify(fornecedorServiceMock, never()).buscarEntidadePorId(anyLong());
  }

  @Test
  @DisplayName("atualizar: Deve atualizar e retornar DTO quando dados válidos e ID existe")
  void atualizar_quandoDadosValidos_deveRetornarProdutoResponseDTOAtualizado() {
    when(produtoRepositoryMock.findById(existingId)).thenReturn(Optional.of(mockProduto)); // Produto a ser atualizado
    when(categoriaServiceMock.buscarEntidadePorId(mockRequestDTO.categoriaId())).thenReturn(mockCategoria); // Nova/mesma
                                                                                                            // categoria
    when(fornecedorServiceMock.buscarEntidadePorId(mockRequestDTO.fornecedorId())).thenReturn(mockFornecedor); // Novo/mesmo
                                                                                                               // fornecedor

    // O mockProduto será modificado por updateEntityFromDTO
    doNothing().when(produtoMapperMock).updateEntityFromDTO(eq(mockRequestDTO), eq(mockProduto), eq(mockCategoria),
        eq(mockFornecedor));
    when(produtoRepositoryMock.save(eq(mockProduto))).thenReturn(mockProduto); // Retorna a entidade atualizada

    // O DTO esperado deve refletir o estado de mockProduto APÓS a simulação da
    // atualização
    // (se os valores do mockRequestDTO fossem diferentes do mockProduto inicial)
    // Para este teste, vamos assumir que mockRequestDTO tem os valores atualizados
    Produto produtoAposUpdateSimulado = Produto.builder()
        .id(existingId).nome(mockRequestDTO.nome()).descricao(mockRequestDTO.descricao()) // Usar valores do DTO
        // ... outros campos do DTO ...
        .categoria(mockCategoria).fornecedor(mockFornecedor)
        .criadoEm(mockProduto.getCriadoEm()) // Mantém criadoEm original
        .atualizadoEm(LocalDateTime.now()) // Simula atualização
        .ativo(mockRequestDTO.ativo())
        .precoVenda(mockRequestDTO.precoVenda())
        .quantidadeEstoque(mockRequestDTO.quantidadeEstoque())
        .tipoUnidadeVenda(mockRequestDTO.tipoUnidadeVenda())
        .codigoBarras(mockRequestDTO.codigoBarras())
        .precoCustoUnitario(mockRequestDTO.precoCustoUnitario())
        .build();
    ProdutoResponseDTO dtoEsperado = new ProdutoResponseDTO(produtoAposUpdateSimulado);
    when(produtoMapperMock.toResponseDTO(eq(mockProduto))).thenReturn(dtoEsperado);

    ProdutoResponseDTO resultado = produtoService.atualizar(existingId, mockRequestDTO);

    assertNotNull(resultado);
    assertEquals(dtoEsperado.id(), resultado.id());
    assertEquals(mockRequestDTO.nome(), resultado.nome());
    assertNotNull(resultado.fornecedor());
    assertEquals(mockFornecedor.getNome(), resultado.fornecedor().getNome());

    verify(produtoRepositoryMock).findById(existingId);
    verify(categoriaServiceMock).buscarEntidadePorId(mockRequestDTO.categoriaId());
    verify(fornecedorServiceMock).buscarEntidadePorId(mockRequestDTO.fornecedorId());
    verify(produtoMapperMock).updateEntityFromDTO(mockRequestDTO, mockProduto, mockCategoria, mockFornecedor);
    verify(produtoRepositoryMock).save(mockProduto);
    verify(produtoMapperMock).toResponseDTO(mockProduto);
  }

  // ... (Testes para atualizar com falhas permanecem os mesmos) ...

  @Test
  @DisplayName("deletarLogicamente: Deve chamar save com ativo=false/true (toggle) ao deletar/ativar ID existente")
  void deletarLogicamente_quandoIdExiste_deveChamarSaveComAtivoTrocado() {
    // Cenário 1: Produto está ativo, deve inativar
    mockProduto.setAtivo(true); // Garante estado inicial
    when(produtoRepositoryMock.findById(existingId)).thenReturn(Optional.of(mockProduto));
    ArgumentCaptor<Produto> produtoCaptor = ArgumentCaptor.forClass(Produto.class);

    produtoService.deletarLogicamente(existingId);

    verify(produtoRepositoryMock).save(produtoCaptor.capture());
    assertFalse(produtoCaptor.getValue().isAtivo(), "Produto deveria ser inativado");

    // Cenário 2: Produto está inativo, deve ativar
    mockProduto.setAtivo(false); // Garante estado inicial
    when(produtoRepositoryMock.findById(existingId)).thenReturn(Optional.of(mockProduto)); // Re-stubbing

    produtoService.deletarLogicamente(existingId); // Chama de novo

    // O captor já tem o valor da última chamada, precisamos verificar o novo save
    // Para isso, podemos usar times(2) na verificação do save ou resetar o mock
    // (menos comum)
    // Ou usar um novo captor, mas vamos simplificar verificando o estado final do
    // mockProduto após a segunda chamada
    verify(produtoRepositoryMock, times(2)).save(produtoCaptor.capture());
    assertTrue(produtoCaptor.getValue().isAtivo(), "Produto deveria ser ativado");

    verify(produtoRepositoryMock, times(2)).findById(existingId);
  }

  // ... (Teste para deletarLogicamente com ID inexistente permanece o mesmo) ...

  // Teste para deleção física
  @Test
  @DisplayName("deletarFisicamente: Deve chamar deleteById quando produto existe")
  void deletarFisicamente_quandoProdutoExiste_deveChamarDeleteById() {
    when(produtoRepositoryMock.existsById(existingId)).thenReturn(true);
    doNothing().when(produtoRepositoryMock).deleteById(existingId);

    assertDoesNotThrow(() -> produtoService.deletarFisicamente(existingId));

    verify(produtoRepositoryMock).existsById(existingId);
    verify(produtoRepositoryMock).deleteById(existingId);
  }

  @Test
  @DisplayName("deletarFisicamente: Deve lançar RecursoNaoEncontrado quando produto não existe")
  void deletarFisicamente_quandoProdutoNaoExiste_deveLancarRecursoNaoEncontrado() {
    when(produtoRepositoryMock.existsById(nonExistingId)).thenReturn(false);

    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
      produtoService.deletarFisicamente(nonExistingId);
    });
    assertTrue(exception.getMessage().contains("para deleção física"));

    verify(produtoRepositoryMock).existsById(nonExistingId);
    verify(produtoRepositoryMock, never()).deleteById(anyLong());
  }
}