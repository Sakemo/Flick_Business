package br.com.king.flick_business.service; // Mesmo pacote do service, mas em src/test/java

// Imports de DTOs, Entidades, Enums e Exceções do seu projeto
import java.math.BigDecimal; // Import DTO Categoria
import java.time.LocalDateTime; // Import DTO Fornecedor
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
import org.junit.jupiter.api.Test; // Para assertNotNull, assertEquals, assertThrows, assertFalse, etc.
import org.junit.jupiter.api.extension.ExtendWith; // Para any(), anyLong()
import org.mockito.ArgumentCaptor; // Para anyLong() especificamente
import static org.mockito.ArgumentMatchers.any; // Para rodar algo antes de cada teste
import static org.mockito.ArgumentMatchers.anyLong; // Opcional: Nome mais descritivo para o teste
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks; // Marca um método como um teste
import org.mockito.Mock; // Necessário para habilitar Mockito
import static org.mockito.Mockito.doNothing; // Para capturar argumentos passados aos mocks
import static org.mockito.Mockito.never; // Cria a instância real da classe que queremos testar (ProdutoService)
import static org.mockito.Mockito.verify; // Cria instâncias "falsas" (mocks) das dependências
import static org.mockito.Mockito.when; // Integra Mockito com JUnit 5
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.king.flick_business.dto.ProdutoRequestDTO; // Se seus DTOs/Entidades usarem
import br.com.king.flick_business.dto.ProdutoResponseDTO; // Para listas vazias
import br.com.king.flick_business.entity.Categoria; // Para testes de listagem
import br.com.king.flick_business.entity.Fornecedor; // Necessário para findById
import br.com.king.flick_business.entity.Produto;
import br.com.king.flick_business.enums.TipoPessoa;
import br.com.king.flick_business.enums.TipoUnidadeVenda;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.mapper.ProdutoMapper;
import br.com.king.flick_business.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class) // Liga o Mockito ao JUnit 5
class ProdutoServiceTest {

  // Mocks para as dependências do ProdutoService
  @Mock
  private ProdutoRepository produtoRepositoryMock;
  @Mock
  private ProdutoMapper produtoMapperMock;
  @Mock
  private CategoriaService categoriaServiceMock;
  @Mock
  private FornecedorService fornecedorServiceMock;

  // Instância real do ProdutoService com os mocks injetados
  @InjectMocks
  private ProdutoService produtoService;

  // Objetos de teste reutilizáveis
  private Categoria mockCategoria;
  private Fornecedor mockFornecedor;
  private Produto mockProduto;
  private ProdutoRequestDTO mockRequestDTO;
  private ProdutoResponseDTO mockResponseDTO;
  private final Long existingId = 1L;
  private final Long nonExistingId = 99L;
  private final Long categoriaId = 1L;
  private final Long fornecedorId = 1L;

  @BeforeEach // Configuração executada antes de cada teste
  @SuppressWarnings("unused")
  void setUp() {
    // Inicializa mocks de entidades
    mockCategoria = new Categoria(categoriaId, "Eletrônicos");
    mockFornecedor = new Fornecedor(fornecedorId, "Fornecedor Teste", TipoPessoa.JURIDICA, "11.222.333/0001-44",
        "99999-8888", "teste@fornecedor.com", "Notas");

    // Inicializa mock da entidade Produto
    mockProduto = Produto.builder()
        .id(existingId)
        .nome("Produto Teste")
        .descricao("Descrição Teste")
        .codigoBarras("123456")
        .precoVenda(BigDecimal.TEN)
        .precoCustoUnitario(BigDecimal.valueOf(5))
        .quantidadeEstoque(BigDecimal.valueOf(10))
        .tipoUnidadeVenda(TipoUnidadeVenda.UNIDADE)
        .categoria(mockCategoria)
        .fornecedor(mockFornecedor)
        .ativo(true)
        .criadoEm(LocalDateTime.now().minusDays(1)) // Simula datas
        .atualizadoEm(LocalDateTime.now())
        .build();

    // Inicializa mock do DTO de requisição
    mockRequestDTO = new ProdutoRequestDTO(
        "Produto Novo", "Desc Nova", "789012", BigDecimal.valueOf(20), BigDecimal.valueOf(15.50),
        BigDecimal.valueOf(7.25),
        TipoUnidadeVenda.UNIDADE, true, categoriaId, fornecedorId);

    mockResponseDTO = new ProdutoResponseDTO(
        mockProduto.getId(),
        mockProduto.getNome(),
        mockProduto.getDescricao(),
        mockProduto.getCodigoBarras(),
        mockProduto.getQuantidadeEstoque(),
        mockProduto.getPrecoVenda(),
        mockProduto.getPrecoCustoUnitario(),
        mockProduto.getTipoUnidadeVenda(),
        mockProduto.isAtivo(),
        mockProduto.getCategoria(), // Nome Categoria
        mockProduto.getFornecedor(), // Nome Fornecedor
        mockProduto.getCriadoEm(), // Data Criação
        mockProduto.getAtualizadoEm() // Data Atualização
    );
  }

  // --- Testes para buscarPorId ---

  @Test
  @DisplayName("Deve retornar ProdutoResponseDTO quando ID existe")
  void buscarPorId_quandoIdExiste_deveRetornarProdutoResponseDTO() {
    // Arrange
    when(produtoRepositoryMock.findById(existingId)).thenReturn(Optional.of(mockProduto));
    when(produtoMapperMock.toResponseDTO(mockProduto)).thenReturn(mockResponseDTO);

    // Act
    ProdutoResponseDTO resultado = produtoService.buscarPorId(existingId);

    // Assert
    assertNotNull(resultado);
    assertEquals(mockResponseDTO, resultado, "O DTO retornado não é o esperado."); // Compara DTOs (requer
                                                                                   // equals/hashcode no DTO ou compare
                                                                                   // campo a campo)

    // Verify
    verify(produtoRepositoryMock).findById(existingId);
    verify(produtoMapperMock).toResponseDTO(mockProduto);
  }

  @Test
  @DisplayName("Deve lançar RecursoNaoEncontrado quando ID não existe")
  void buscarPorId_quandoIdNaoExiste_deveLancarRecursoNaoEncontrado() {
    // Arrange
    when(produtoRepositoryMock.findById(nonExistingId)).thenReturn(Optional.empty());

    // Act & Assert
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
      produtoService.buscarPorId(nonExistingId);
    }, "Deveria lançar RecursoNaoEncontrado");

    assertEquals("Produto não encontrado com ID: " + nonExistingId, exception.getMessage());

    // Verify
    verify(produtoRepositoryMock).findById(nonExistingId);
    verify(produtoMapperMock, never()).toResponseDTO(any());
  }

  // --- Testes para salvar ---

  @Test
  @DisplayName("Deve salvar e retornar ProdutoResponseDTO quando dados válidos")
  void salvar_quandoDadosValidos_deveRetornarProdutoResponseDTOSalvo() {
    // Arrange
    // 1. Simular busca de Categoria e Fornecedor OK
    when(categoriaServiceMock.buscarEntidadePorId(categoriaId)).thenReturn(mockCategoria);
    when(fornecedorServiceMock.buscarEntidadePorId(fornecedorId)).thenReturn(mockFornecedor);

    // 2. Simular mapper DTO -> Entidade (sem ID)
    Produto produtoParaSalvar = Produto.builder() /* Copia campos do DTO */
        .nome(mockRequestDTO.nome()).descricao(mockRequestDTO.descricao()).codigoBarras(mockRequestDTO.codigoBarras())
        .quantidadeEstoque(mockRequestDTO.quantidadeEstoque()).precoVenda(mockRequestDTO.precoVenda())
        .precoCustoUnitario(mockRequestDTO.precoCustoUnitario()).tipoUnidadeVenda(mockRequestDTO.tipoUnidadeVenda())
        .ativo(mockRequestDTO.ativo()).categoria(mockCategoria).fornecedor(mockFornecedor).build();
    when(produtoMapperMock.toEntity(mockRequestDTO, mockCategoria, mockFornecedor)).thenReturn(produtoParaSalvar);

    // 3. Simular repositório salvando e retornando entidade COM ID e timestamps
    Produto produtoSalvoComId = Produto.builder()
        .id(2L)
        .nome(produtoParaSalvar.getNome())
        .descricao(produtoParaSalvar.getDescricao())
        .codigoBarras(produtoParaSalvar.getCodigoBarras())
        .quantidadeEstoque(produtoParaSalvar.getQuantidadeEstoque())
        .precoVenda(produtoParaSalvar.getPrecoVenda())
        .precoCustoUnitario(produtoParaSalvar.getPrecoCustoUnitario())
        .tipoUnidadeVenda(produtoParaSalvar.getTipoUnidadeVenda()).ativo(produtoParaSalvar.isAtivo())
        .categoria(mockCategoria)
        .fornecedor(mockFornecedor)
        .criadoEm(LocalDateTime.now())
        .atualizadoEm(LocalDateTime.now()).build();
    when(produtoRepositoryMock.save(produtoParaSalvar)).thenReturn(produtoSalvoComId);

    // 4. Simular mapper Entidade Salva -> DTO Resposta
    ProdutoResponseDTO dtoEsperado = new ProdutoResponseDTO(produtoSalvoComId); // Usa o construtor do DTO real
    when(produtoMapperMock.toResponseDTO(produtoSalvoComId)).thenReturn(dtoEsperado);

    // Act
    ProdutoResponseDTO resultado = produtoService.salvar(mockRequestDTO);

    // Assert
    assertNotNull(resultado);
    assertEquals(dtoEsperado.id(), resultado.id());
    assertEquals(dtoEsperado.nome(), resultado.nome());
    assertNotNull(resultado.categoria(), "Categoria no resultado não deveria ser nula");
    assertEquals(dtoEsperado.categoria().getNome(), resultado.categoria().getNome());
    assertEquals(dtoEsperado, resultado);

    // Verify
    verify(categoriaServiceMock).buscarEntidadePorId(categoriaId);
    verify(fornecedorServiceMock).buscarEntidadePorId(fornecedorId);
    verify(produtoMapperMock).toEntity(mockRequestDTO, mockCategoria, mockFornecedor);
    verify(produtoRepositoryMock).save(produtoParaSalvar);
    verify(produtoMapperMock).toResponseDTO(produtoSalvoComId);
  }

  @Test
  @DisplayName("Deve lançar RecursoNaoEncontrado ao salvar se Categoria não existe")
  void salvar_quandoCategoriaNaoExiste_deveLancarRecursoNaoEncontrado() {
    // Arrange: Simular que a busca da categoria falha
    when(categoriaServiceMock.buscarEntidadePorId(categoriaId))
        .thenThrow(new RecursoNaoEncontrado("Categoria não encontrada com ID: " + categoriaId));

    // Act & Assert
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
      produtoService.salvar(mockRequestDTO);
    });
    assertEquals("Categoria não encontrada com ID: " + categoriaId, exception.getMessage());

    // Verify
    verify(categoriaServiceMock).buscarEntidadePorId(categoriaId);
    verify(fornecedorServiceMock, never()).buscarEntidadePorId(anyLong()); // Não deve nem tentar buscar fornecedor
    verify(produtoMapperMock, never()).toEntity(any(), any(), any());
    verify(produtoRepositoryMock, never()).save(any());
    verify(produtoMapperMock, never()).toResponseDTO(any());
  }

  @Test
  @DisplayName("Deve lançar RecursoNaoEncontrado ao salvar se Fornecedor não existe")
  void salvar_quandoFornecedorNaoExiste_deveLancarRecursoNaoEncontrado() {
    // Arrange
    // 1. Categoria existe
    when(categoriaServiceMock.buscarEntidadePorId(categoriaId)).thenReturn(mockCategoria);
    // 2. Fornecedor NÃO existe
    when(fornecedorServiceMock.buscarEntidadePorId(fornecedorId))
        .thenThrow(new RecursoNaoEncontrado("Fornecedor não encontrado com ID: " + fornecedorId));

    // Act & Assert
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
      produtoService.salvar(mockRequestDTO);
    });
    assertEquals("Fornecedor não encontrado com ID: " + fornecedorId, exception.getMessage());

    // Verify
    verify(categoriaServiceMock).buscarEntidadePorId(categoriaId); // Verificou categoria
    verify(fornecedorServiceMock).buscarEntidadePorId(fornecedorId); // Tentou verificar fornecedor
    verify(produtoMapperMock, never()).toEntity(any(), any(), any()); // Não chegou a mapear
    verify(produtoRepositoryMock, never()).save(any());
    verify(produtoMapperMock, never()).toResponseDTO(any());
  }

  @Test
  @DisplayName("Deve atualizar e retornar DTO quando dados válidos e ID existe")
  void atualizar_quandoDadosValidos_deveRetornarProdutoResponseDTOAtualizado() {
    // Arrange
    // 1. Simular que o produto a ser atualizado existe
    when(produtoRepositoryMock.findById(existingId)).thenReturn(Optional.of(mockProduto));

    // 2. Simular busca das novas (ou mesmas) Categoria e Fornecedor
    when(categoriaServiceMock.buscarEntidadePorId(categoriaId)).thenReturn(mockCategoria); // Reutiliza a mesma
                                                                                           // categoria
    Fornecedor novoFornecedorMock = new Fornecedor(2L, "Novo Fornecedor", TipoPessoa.FISICA, "123.456.789-00",
        "111-333", "novo@email.com", "Notas novas");
    // Assume que o mockRequestDTO tem fornecedorId = 1L, mas vamos simular a busca
    // por ID 2L
    ProdutoRequestDTO requestUpdateComNovoFornecedor = new ProdutoRequestDTO(
        "Produto Atualizado", "Desc Att", "987654", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO,
        TipoUnidadeVenda.UNIDADE, true, categoriaId, 2L // ID do novo fornecedor
    );
    when(fornecedorServiceMock.buscarEntidadePorId(2L)).thenReturn(novoFornecedorMock); // Mock busca do novo fornecedor

    // 3. Mockar o updateEntityFromDTO (não retorna nada, só verifica a chamada)
    // Usamos doNothing() pois o método é void
    doNothing().when(produtoMapperMock).updateEntityFromDTO(requestUpdateComNovoFornecedor, mockProduto, mockCategoria,
        novoFornecedorMock);

    // 4. Mockar o save do repositório (que faz o UPDATE)
    // O save no atualizar retorna a própria entidade atualizada
    when(produtoRepositoryMock.save(mockProduto)).thenReturn(mockProduto); // Retorna o mockProduto atualizado

    // 5. Mockar o mapper para retornar o DTO de resposta final
    // Cria um DTO esperado que reflete as mudanças (nome e fornecedor)
    ProdutoResponseDTO dtoEsperado = new ProdutoResponseDTO(
        mockProduto.getId(), requestUpdateComNovoFornecedor.nome(), requestUpdateComNovoFornecedor.descricao(),
        requestUpdateComNovoFornecedor.codigoBarras(),
        requestUpdateComNovoFornecedor.quantidadeEstoque(), requestUpdateComNovoFornecedor.precoVenda(),
        requestUpdateComNovoFornecedor.precoCustoUnitario(),
        requestUpdateComNovoFornecedor.tipoUnidadeVenda(), requestUpdateComNovoFornecedor.ativo(),
        mockCategoria, novoFornecedorMock,
        mockProduto.getCriadoEm(), LocalDateTime.now() // Atualizado agora
    );
    when(produtoMapperMock.toResponseDTO(mockProduto)).thenReturn(dtoEsperado);

    // Act
    ProdutoResponseDTO resultado = produtoService.atualizar(existingId, requestUpdateComNovoFornecedor);

    // Assert
    assertNotNull(resultado);
    assertEquals(dtoEsperado.id(), resultado.id());
    assertEquals("Produto Atualizado", resultado.nome());
    assertNotNull(resultado.fornecedor(), "O fornecedeor não deveria ser nulo.");
    assertEquals("Novo Fornecedor", resultado.fornecedor().getNome());
    assertEquals(dtoEsperado, resultado);

    // Verify
    verify(produtoRepositoryMock).findById(existingId);
    verify(categoriaServiceMock).buscarEntidadePorId(categoriaId);
    verify(fornecedorServiceMock).buscarEntidadePorId(2L); // Verifica busca do NOVO fornecedor ID
    verify(produtoMapperMock).updateEntityFromDTO(requestUpdateComNovoFornecedor, mockProduto, mockCategoria,
        novoFornecedorMock);
    verify(produtoRepositoryMock).save(mockProduto);
    verify(produtoMapperMock).toResponseDTO(mockProduto);
  }

  @Test
  @DisplayName("Deve lançar RecursoNaoEncontrado ao atualizar se Produto não existe")
  void atualizar_quandoProdutoNaoExiste_deveLancarRecursoNaoEncontrado() {
    // Arrange
    when(produtoRepositoryMock.findById(nonExistingId)).thenReturn(Optional.empty());

    // Act & Assert
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
      produtoService.atualizar(nonExistingId, mockRequestDTO);
    });
    assertEquals("Produto não encontrado com ID: " + nonExistingId, exception.getMessage());

    // Verify
    verify(produtoRepositoryMock).findById(nonExistingId);
    // Garantir que nada mais foi chamado
    verify(categoriaServiceMock, never()).buscarEntidadePorId(anyLong());
    verify(fornecedorServiceMock, never()).buscarEntidadePorId(anyLong());
    verify(produtoMapperMock, never()).updateEntityFromDTO(any(), any(), any(), any());
    verify(produtoRepositoryMock, never()).save(any());
    verify(produtoMapperMock, never()).toResponseDTO(any());
  }

  @Test
  @DisplayName("Deve lançar RecursoNaoEncontrado ao atualizar se Nova Categoria não existe")
  void atualizar_quandoNovaCategoriaNaoExiste_deveLancarRecursoNaoEncontrado() {
    // Arrange
    // 1. Produto existe
    when(produtoRepositoryMock.findById(existingId)).thenReturn(Optional.of(mockProduto));
    // 2. Nova Categoria NÃO existe
    Long idCategoriaInexistente = 55L;
    ProdutoRequestDTO requestComCategoriaInexistente = new ProdutoRequestDTO(
        "Nome", "Desc", "123", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO,
        TipoUnidadeVenda.UNIDADE, true, idCategoriaInexistente, fornecedorId); // ID de categoria inválido
    when(categoriaServiceMock.buscarEntidadePorId(idCategoriaInexistente))
        .thenThrow(new RecursoNaoEncontrado("Categoria não encontrada com ID: " + idCategoriaInexistente));

    // Act & Assert
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
      produtoService.atualizar(existingId, requestComCategoriaInexistente);
    });
    assertEquals("Categoria não encontrada com ID: " + idCategoriaInexistente, exception.getMessage());

    // Verify
    verify(produtoRepositoryMock).findById(existingId); // Buscou o produto
    verify(categoriaServiceMock).buscarEntidadePorId(idCategoriaInexistente); // Tentou buscar categoria
    verify(fornecedorServiceMock, never()).buscarEntidadePorId(anyLong()); // Não chegou a buscar fornecedor
    verify(produtoMapperMock, never()).updateEntityFromDTO(any(), any(), any(), any());
    verify(produtoRepositoryMock, never()).save(any());
    verify(produtoMapperMock, never()).toResponseDTO(any());
  }

  @Test
  @DisplayName("Deve lançar RecursoNaoEncontrado ao atualizar se Novo Fornecedor não existe")
  void atualizar_quandoNovoFornecedorNaoExiste_deveLancarRecursoNaoEncontrado() {
    // Arrange
    // 1. Produto existe
    when(produtoRepositoryMock.findById(existingId)).thenReturn(Optional.of(mockProduto));
    // 2. Categoria existe (a nova, ou a mesma)
    when(categoriaServiceMock.buscarEntidadePorId(categoriaId)).thenReturn(mockCategoria);
    // 3. Novo Fornecedor NÃO existe
    Long idFornecedorInexistente = 66L;
    ProdutoRequestDTO requestComFornecedorInexistente = new ProdutoRequestDTO(
        "Nome", "Desc", "123", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO,
        TipoUnidadeVenda.UNIDADE, true, categoriaId, idFornecedorInexistente); // ID de fornecedor inválido
    when(fornecedorServiceMock.buscarEntidadePorId(idFornecedorInexistente))
        .thenThrow(new RecursoNaoEncontrado("Fornecedor não encontrado com ID: " + idFornecedorInexistente));

    // Act & Assert
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
      produtoService.atualizar(existingId, requestComFornecedorInexistente);
    });
    assertEquals("Fornecedor não encontrado com ID: " + idFornecedorInexistente, exception.getMessage());

    // Verify
    verify(produtoRepositoryMock).findById(existingId); // Buscou produto
    verify(categoriaServiceMock).buscarEntidadePorId(categoriaId); // Buscou categoria
    verify(fornecedorServiceMock).buscarEntidadePorId(idFornecedorInexistente); // Tentou buscar fornecedor
    verify(produtoMapperMock, never()).updateEntityFromDTO(any(), any(), any(), any()); // Não chegou a mapear
    verify(produtoRepositoryMock, never()).save(any());
    verify(produtoMapperMock, never()).toResponseDTO(any());
  }

  @Test
  @DisplayName("Deve chamar save com ativo=false ao deletar logicamente ID existente")
  void deletarLogicamente_quandoIdExiste_deveChamarSaveComAtivoFalse() {
    // Arrange
    // Garantir que o produto mockado começa ativo
    assertTrue(mockProduto.isAtivo(), "Pré-condição: mockProduto deve estar ativo");
    when(produtoRepositoryMock.findById(existingId)).thenReturn(Optional.of(mockProduto));
    // Não precisamos mockar o save para retornar algo, apenas capturar o argumento

    // Act
    produtoService.deletarLogicamente(existingId);

    // Assert / Verify usando ArgumentCaptor
    // 1. Criar o captor para a classe Produto
    ArgumentCaptor<Produto> produtoCaptor = ArgumentCaptor.forClass(Produto.class);

    // 2. Verificar se o save foi chamado e capturar o objeto Produto passado para
    // ele
    verify(produtoRepositoryMock).save(produtoCaptor.capture());

    // 3. Pegar o valor capturado
    Produto produtoSalvo = produtoCaptor.getValue();

    // 4. Verificar se o estado 'ativo' do objeto capturado é false
    assertFalse(produtoSalvo.isAtivo(), "O produto salvo deveria estar inativo");
    assertEquals(existingId, produtoSalvo.getId(), "O ID do produto salvo está incorreto"); // Opcional: sanity check

    // Verificar também que findById foi chamado
    verify(produtoRepositoryMock).findById(existingId);
  }

  @Test
  @DisplayName("Deve lançar RecursoNaoEncontrado ao deletar logicamente ID inexistente")
  void deletarLogicamente_quandoIdNaoExiste_deveLancarRecursoNaoEncontrado() {
    // Arrange
    when(produtoRepositoryMock.findById(nonExistingId)).thenReturn(Optional.empty());

    // Act & Assert
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
      produtoService.deletarLogicamente(nonExistingId);
    });
    assertEquals("Produto não encontrado com ID: " + nonExistingId, exception.getMessage());

    // Verify
    verify(produtoRepositoryMock).findById(nonExistingId);
    verify(produtoRepositoryMock, never()).save(any()); // Garantir que save não foi chamado
  }

  // --- Teste para listarTodos (Exemplo) ---
  @Test
  @DisplayName("Deve retornar lista de DTOs ao listar todos")
  void listarTodos_deveRetornarListaDeDTOs() {
    // Arrange
    Produto outroProduto = Produto.builder().id(2L).nome("Outro Produto").ativo(true).categoria(mockCategoria)
        .fornecedor(mockFornecedor).build();
    List<Produto> listaProdutos = List.of(mockProduto, outroProduto);
    when(produtoRepositoryMock.findAllWithCategoriaAndFornecedor()).thenReturn(listaProdutos);

    // Mock do mapper para a lista inteira (assumindo que seu mapper tem
    // toResponseDTOList ou você mocka individualmente)
    ProdutoResponseDTO dto1 = new ProdutoResponseDTO(mockProduto); // Usa construtor real do DTO
    ProdutoResponseDTO dto2 = new ProdutoResponseDTO(outroProduto);
    when(produtoMapperMock.toResponseDTOList(eq(listaProdutos))).thenReturn(List.of(dto1, dto2)); // Mock do método de
                                                                                                  // lista

    // Act
    List<ProdutoResponseDTO> resultado = produtoService.listarTodos(null);

    // Assert
    assertNotNull(resultado);
    assertEquals(2, resultado.size(), "Tamanho da lista incorreto");
    assertEquals(dto1.id(), resultado.get(0).id());
    assertEquals(dto2.id(), resultado.get(1).id());

    // Verify
    verify(produtoRepositoryMock).findAllWithCategoriaAndFornecedor();
    verify(produtoMapperMock).toResponseDTOList(eq(listaProdutos));
  }

  @Test
  @DisplayName("Deve retornar lista vazia ao listar todos quando não há produtos")
  void listarTodos_quandoNaoHaProdutos_deveRetornarListaVazia() {
    // Arrange
    when(produtoRepositoryMock.findAllWithCategoriaAndFornecedor()).thenReturn(Collections.emptyList());
    when(produtoMapperMock.toResponseDTOList(Collections.emptyList())).thenReturn(Collections.emptyList());

    // Act
    List<ProdutoResponseDTO> resultado = produtoService.listarTodos(null);

    // Assert
    assertNotNull(resultado);
    assertTrue(resultado.isEmpty(), "A lista deveria estar vazia");

    // Verify
    verify(produtoRepositoryMock).findAllWithCategoriaAndFornecedor();
    verify(produtoMapperMock).toResponseDTOList(Collections.emptyList());
    verify(produtoRepositoryMock, never()).findByCategoriaId(anyLong());
    verify(produtoRepositoryMock, never()).findByCategoriaIdWithFornecedor(anyLong());
  }

  @Test
  @DisplayName("Deve retornar lista de DTOs filtrada por categoria ao listar por categoriaId")
  void listarTodos_quandoCategoriaIdFornecido_deveRetornarListaFiltrada() {
    // Arrange
    // mockProduto já tem categoriaId = 1L
    List<Produto> listaFiltrada = List.of(mockProduto);
    when(produtoRepositoryMock.findByCategoriaIdWithFornecedor(categoriaId)).thenReturn(listaFiltrada);

    ProdutoResponseDTO dtoEsperado = new ProdutoResponseDTO(mockProduto);
    when(produtoMapperMock.toResponseDTOList(eq(listaFiltrada))).thenReturn(List.of(dtoEsperado));

    // Act
    List<ProdutoResponseDTO> resultado = produtoService.listarTodos(categoriaId);

    // Assert
    assertNotNull(resultado);
    assertEquals(1, resultado.size());
    assertEquals(dtoEsperado.id(), resultado.get(0).id());
    assertNotNull(resultado.get(0).categoria(), "Categoria no DTO não deve ser nula");
    assertEquals(mockCategoria.getNome(), resultado.get(0).categoria().getNome()); // Verifica o nome da categoria

    // Verify
    verify(produtoRepositoryMock).findByCategoriaIdWithFornecedor(categoriaId);
    verify(produtoMapperMock).toResponseDTOList(eq(listaFiltrada));
    verify(produtoRepositoryMock, never()).findAllWithCategoriaAndFornecedor(); // Não deve chamar findAll
  }
}
