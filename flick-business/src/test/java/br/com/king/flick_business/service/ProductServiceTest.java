package br.com.king.flick_business.service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
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

import br.com.king.flick_business.dto.ProductRequestDTO;
import br.com.king.flick_business.dto.ProductResponseDTO;
import br.com.king.flick_business.entity.Category;
import br.com.king.flick_business.entity.Provider;
import br.com.king.flick_business.entity.Product;
import br.com.king.flick_business.enums.UnitOfSale;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.mapper.ProductMapper;
import br.com.king.flick_business.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock
  private ProductRepository productRepositoryMock;
  @Mock
  private ProductMapper productMapperMock;
  @Mock
  private CategoryService categoryServiceMock;
  @Mock
  private ProviderService providerServiceMock;

  @InjectMocks
  private ProductService productService;

  @Captor
  private ArgumentCaptor<Product> productCaptor;
  // Não precisamos de sortCaptor aqui, pois ProductService.listTodos(Long) não
  // usa Sort diretamente

  private Category mockCategory;
  private Provider mockProvider;
  private Product mockProduct;
  private ProductRequestDTO mockRequestDTO;
  private ProductResponseDTO mockResponseDTO; // Para searchById e mock do mapper
  private final Long existingId = 1L;
  private final Long nonExistingId = 99L;
  private final Long categoryId = 1L;
  private final Long providerId = 1L;

  @BeforeEach
  void setUp() {
    mockCategory = Category.builder().id(categoryId).name("Eletrônicos").build();
    // Ajustar Provider.builder() se TipoPessoa não for mais usado diretamente ou
    // for opcional
    mockProvider = Provider.builder()
        .id(providerId)
        .name("Provider Teste")
        // .tipoPessoa(TipoPessoa.JURIDICA) // Remover ou ajustar se Provider mudou
        // .cnpjCpf("11.222.333/0001-44")
        .build();

    mockProduct = Product.builder()
        .id(existingId).name("Product Teste").description("Descrição Teste").barcode("123456")
        .salePrice(BigDecimal.TEN).costPrice(BigDecimal.valueOf(5))
        .stockQuantity(BigDecimal.valueOf(10)).unitOfSale(UnitOfSale.UNIDADE)
        .category(mockCategory).provider(mockProvider).active(true)
        .createdAt(ZonedDateTime.now().minusDays(1)).updatedAt(ZonedDateTime.now())
        .build();

    // Ordem dos campos DEVE bater com ProductRequestDTO.java do backend
    mockRequestDTO = new ProductRequestDTO(
        "Product Novo", // name
        "Desc Nova", // description
        "789012", // codigoBarras
        BigDecimal.valueOf(20), // quantidadeEstoque
        BigDecimal.valueOf(15.50), // salePrice
        BigDecimal.valueOf(7.25), // precoCustoUnitario
        UnitOfSale.UNIDADE, // UnitOfSale
        true, // active
        categoryId, // categoryId
        providerId // providerId
    );

    // mockResponseDTO construído a partir de mockProduct
    mockResponseDTO = new ProductResponseDTO(mockProduct);
  }

  @Test
  @DisplayName("searchById: Deve retornar ProductResponseDTO quando ID existe")
  void searchById_quandoIdExiste_deveRetornarProductResponseDTO() {
    when(productRepositoryMock.findById(existingId)).thenReturn(Optional.of(mockProduct));
    when(productMapperMock.toResponseDTO(mockProduct)).thenReturn(mockResponseDTO);

    ProductResponseDTO resultado = productService.searchById(existingId);

    assertNotNull(resultado);
    assertEquals(mockResponseDTO.id(), resultado.id());
    assertEquals(mockResponseDTO.name(), resultado.name());
    assertNotNull(resultado.category());
    assertEquals(mockCategory.getName(), resultado.category().getName());
    // Se quiser comparar o DTO inteiro, certifique-se que ProductResponseDTO
    // (record)
    // tem equals/hashCode bem definidos para os objetos Category e Provider,
    // ou compare os objetos aninhados campo a campo.
    // assertEquals(mockResponseDTO, resultado); // Pode ser problemático se os
    // objetos aninhados não tiverem equals/hashCode adequados

    verify(productRepositoryMock).findById(existingId);
    verify(productMapperMock).toResponseDTO(mockProduct);
  }

  @Test
  @DisplayName("searchById: Deve lançar RecursoNaoEncontrado quando ID não existe")
  void searchById_quandoIdNaoExiste_deveLancarRecursoNaoEncontrado() {
    when(productRepositoryMock.findById(nonExistingId)).thenReturn(Optional.empty());
    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class,
        () -> productService.searchById(nonExistingId));
    assertEquals("Product não encontrado com ID: " + nonExistingId, exception.getMessage());
    verify(productRepositoryMock).findById(nonExistingId);
    verify(productMapperMock, never()).toResponseDTO(any());
  }

  @Test
  @DisplayName("save: Deve save e retornar ProductResponseDTO quando dados válidos")
  void save_quandoDadosValidos_deveRetornarProductResponseDTOSalvo() {
    when(categoryServiceMock.searchEntityById(categoryId)).thenReturn(mockCategory);
    when(providerServiceMock.searchEntityById(providerId)).thenReturn(mockProvider);

    Product productParaSalvar = // ... (criação do productParaSalvar como antes)
        Product.builder().name(mockRequestDTO.name()).description(mockRequestDTO.description())
            .barcode(mockRequestDTO.barcode()).stockQuantity(mockRequestDTO.stockQuantity())
            .salePrice(mockRequestDTO.salePrice()).costPrice(mockRequestDTO.precoCustoUnitario())
            .unitOfSale(mockRequestDTO.UnitOfSale()).active(mockRequestDTO.active())
            .category(mockCategory).provider(mockProvider).build();
    when(productMapperMock.toEntity(eq(mockRequestDTO), eq(mockCategory), eq(mockProvider)))
        .thenReturn(productParaSalvar);

    Product productSalvoComId = Product.builder().id(2L) // Atribui ID
        // Copia outros campos de productParaSalvar
        .name(productParaSalvar.getName()).description(productParaSalvar.getDescription())
        .barcode(productParaSalvar.getBarcode()).stockQuantity(productParaSalvar.getStockQuantity())
        .salePrice(productParaSalvar.getSalePrice()).costPrice(productParaSalvar.getCostPrice())
        .unitOfSale(productParaSalvar.getUnitOfSale()).active(productParaSalvar.isActive())
        .category(productParaSalvar.getCategory()).provider(productParaSalvar.getProvider())
        .createdAt(ZonedDateTime.now()).updatedAt(ZonedDateTime.now()).build();
    when(productRepositoryMock.save(eq(productParaSalvar))).thenReturn(productSalvoComId);

    ProductResponseDTO dtoEsperado = new ProductResponseDTO(productSalvoComId);
    when(productMapperMock.toResponseDTO(eq(productSalvoComId))).thenReturn(dtoEsperado);

    ProductResponseDTO resultado = productService.save(mockRequestDTO);

    assertNotNull(resultado);
    assertEquals(dtoEsperado.id(), resultado.id());
    assertEquals(dtoEsperado.name(), resultado.name());
    assertNotNull(resultado.category());
    assertEquals(dtoEsperado.category().getName(), resultado.category().getName());

    verify(categoryServiceMock).searchEntityById(categoryId);
    verify(providerServiceMock).searchEntityById(providerId);
    verify(productMapperMock).toEntity(mockRequestDTO, mockCategory, mockProvider);
    verify(productRepositoryMock).save(productParaSalvar);
    verify(productMapperMock).toResponseDTO(productSalvoComId);
  }

  // ... (Testes para save com category/provider não encontrado permanecem os
  // mesmos) ...
  @Test
  @DisplayName("save: Deve lançar RecursoNaoEncontrado se Category não existe")
  void save_quandoCategoryNaoExiste_deveLancarRecursoNaoEncontrado() {
    when(categoryServiceMock.searchEntityById(categoryId))
        .thenThrow(new RecursoNaoEncontrado("Category não encontrada"));
    assertThrows(RecursoNaoEncontrado.class, () -> productService.save(mockRequestDTO));
    verify(providerServiceMock, never()).searchEntityById(anyLong());
  }

  @Test
  @DisplayName("atualizar: Deve atualizar e retornar DTO quando dados válidos e ID existe")
  void atualizar_quandoDadosValidos_deveRetornarProductResponseDTOAtualizado() {
    when(productRepositoryMock.findById(existingId)).thenReturn(Optional.of(mockProduct)); // Product a ser atualizado
    when(categoryServiceMock.searchEntityById(mockRequestDTO.categoryId())).thenReturn(mockCategory); // Nova/mesma
                                                                                                      // category
    when(providerServiceMock.searchEntityById(mockRequestDTO.providerId())).thenReturn(mockProvider); // Novo/mesmo
                                                                                                      // provider

    // O mockProduct será modificado por updateEntityFromDTO
    doNothing().when(productMapperMock).updateEntityFromDTO(eq(mockRequestDTO), eq(mockProduct), eq(mockCategory),
        eq(mockProvider));
    when(productRepositoryMock.save(eq(mockProduct))).thenReturn(mockProduct); // Retorna a entidade atualizada

    // O DTO esperado deve refletir o estado de mockProduct APÓS a simulação da
    // atualização
    // (se os valuees do mockRequestDTO fossem diferentes do mockProduct inicial)
    // Para este teste, vamos assumir que mockRequestDTO tem os valuees atualizados
    Product productAposUpdateSimulado = Product.builder()
        .id(existingId).name(mockRequestDTO.name()).description(mockRequestDTO.description()) // Usar valuees do DTO
        // ... outros campos do DTO ...
        .category(mockCategory).provider(mockProvider)
        .createdAt(mockProduct.getCreatedAt()) // Mantém criadoEm original
        .updatedAt(ZonedDateTime.now()) // Simula atualização
        .active(mockRequestDTO.active())
        .salePrice(mockRequestDTO.salePrice())
        .stockQuantity(mockRequestDTO.stockQuantity())
        .unitOfSale(mockRequestDTO.UnitOfSale())
        .barcode(mockRequestDTO.barcode())
        .costPrice(mockRequestDTO.precoCustoUnitario())
        .build();
    ProductResponseDTO dtoEsperado = new ProductResponseDTO(productAposUpdateSimulado);
    when(productMapperMock.toResponseDTO(eq(mockProduct))).thenReturn(dtoEsperado);

    ProductResponseDTO resultado = productService.atualizar(existingId, mockRequestDTO);

    assertNotNull(resultado);
    assertEquals(dtoEsperado.id(), resultado.id());
    assertEquals(mockRequestDTO.name(), resultado.name());
    assertNotNull(resultado.provider());
    assertEquals(mockProvider.getName(), resultado.provider().getName());

    verify(productRepositoryMock).findById(existingId);
    verify(categoryServiceMock).searchEntityById(mockRequestDTO.categoryId());
    verify(providerServiceMock).searchEntityById(mockRequestDTO.providerId());
    verify(productMapperMock).updateEntityFromDTO(mockRequestDTO, mockProduct, mockCategory, mockProvider);
    verify(productRepositoryMock).save(mockProduct);
    verify(productMapperMock).toResponseDTO(mockProduct);
  }

  // ... (Testes para atualizar com falhas permanecem os mesmos) ...

  @Test
  @DisplayName("deleteLogicamente: Deve chamar save com active=false/true (toggle) ao delete/ativar ID existente")
  void deleteLogicamente_quandoIdExiste_deveChamarSaveComActiveTrocado() {
    // Cenário 1: Product está active, deve inativar
    mockProduct.setActive(true); // Garante estado inicial
    when(productRepositoryMock.findById(existingId)).thenReturn(Optional.of(mockProduct));
    ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

    productService.deleteLogicamente(existingId);

    verify(productRepositoryMock).save(productCaptor.capture());
    assertFalse(productCaptor.getValue().isActive(), "Product deveria ser inativado");

    // Cenário 2: Product está inactive, deve ativar
    mockProduct.setActive(false); // Garante estado inicial
    when(productRepositoryMock.findById(existingId)).thenReturn(Optional.of(mockProduct)); // Re-stubbing

    productService.deleteLogicamente(existingId); // Chama de novo

    // O captor já tem o value da última chamada, precisamos verificar o novo save
    // Para isso, podemos usar times(2) na verificação do save ou resetar o mock
    // (menos comum)
    // Ou usar um novo captor, mas vamos simplificar verificando o estado final do
    // mockProduct após a segunda chamada
    verify(productRepositoryMock, times(2)).save(productCaptor.capture());
    assertTrue(productCaptor.getValue().isActive(), "Product deveria ser ativado");

    verify(productRepositoryMock, times(2)).findById(existingId);
  }

  // ... (Teste para deleteLogicamente com ID inexistente permanece o mesmo) ...

  // Teste para deleção física
  @Test
  @DisplayName("deleteFisicamente: Deve chamar deleteById quando product existe")
  void deleteFisicamente_quandoProductExiste_deveChamarDeleteById() {
    when(productRepositoryMock.existsById(existingId)).thenReturn(true);
    doNothing().when(productRepositoryMock).deleteById(existingId);

    assertDoesNotThrow(() -> productService.deleteFisicamente(existingId));

    verify(productRepositoryMock).existsById(existingId);
    verify(productRepositoryMock).deleteById(existingId);
  }

  @Test
  @DisplayName("deleteFisicamente: Deve lançar RecursoNaoEncontrado quando product não existe")
  void deleteFisicamente_quandoProductNaoExiste_deveLancarRecursoNaoEncontrado() {
    when(productRepositoryMock.existsById(nonExistingId)).thenReturn(false);

    RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
      productService.deleteFisicamente(nonExistingId);
    });
    assertTrue(exception.getMessage().contains("para deleção física"));

    verify(productRepositoryMock).existsById(nonExistingId);
    verify(productRepositoryMock, never()).deleteById(anyLong());
  }
}