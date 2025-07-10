package br.com.king.flick_business.service;

import java.math.BigDecimal; // Importar DTOs
import java.time.ZonedDateTime; // Importar Entidades
import java.util.List; // Importar Enum
import java.util.Optional; // Importar Exceções

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList; // Usar @Captor para simplificar
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import br.com.king.flick_business.dto.VendaRequestDTO;
import br.com.king.flick_business.dto.VendaResponseDTO;
import br.com.king.flick_business.dto.request.ItemVendaRequestDTO;
import br.com.king.flick_business.dto.response.PageResponse;
import br.com.king.flick_business.entity.Cliente;
import br.com.king.flick_business.entity.ItemVenda;
import br.com.king.flick_business.entity.Product;
import br.com.king.flick_business.entity.Venda;
import br.com.king.flick_business.enums.FormaPagamento;
import br.com.king.flick_business.exception.BusinessException;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.repository.ClienteRepository;
import br.com.king.flick_business.repository.ProductRepository;
import br.com.king.flick_business.repository.VendaRepository;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepositoryMock;
    @Mock
    private ClienteRepository clienteRepositoryMock;
    @Mock
    private ProductRepository productRepositoryMock; // Mock do repositório agora
    @Mock
    private ConfiguracaoGeralService configuracaoServiceMock;

    @InjectMocks
    private VendaService vendaService;

    // Captors para verificar objetos salvos
    @Captor
    private ArgumentCaptor<Venda> vendaCaptor;
    @Captor
    private ArgumentCaptor<List<Product>> productsListCaptor;
    @Captor
    private ArgumentCaptor<Cliente> clienteCaptor;

    // Dados de teste
    private Cliente clienteActiveFiadoPermitido;
    private Cliente clienteActiveFiadoNaoPermitido;
    private Cliente clienteInactive;
    private Product productComEstoque;
    private Product productSemEstoque;
    private Product productInactive;
    private Product productSemPreco;
    private VendaRequestDTO vendaFiadoRequestDTO;
    private VendaRequestDTO vendaDinheiroRequestDTO;
    private VendaRequestDTO vendaComItemSemEstoqueDTO;
    private VendaRequestDTO vendaComItemInactiveDTO;
    private VendaRequestDTO vendaComItemSemPrecoDTO;
    private VendaRequestDTO vendaFiadoLimiteExcedidoDTO;
    private VendaRequestDTO vendaFiadoClienteNaoPermitidoDTO;
    private VendaRequestDTO vendaFiadoClienteInactiveDTO;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        // Clientes
        clienteActiveFiadoPermitido = Cliente.builder().id(1L).name("Cliente Fiado OK").active(true).controleFiado(true)
                .limiteFiado(new BigDecimal("100.00")).saldoDevedor(new BigDecimal("10.00")).build();
        clienteActiveFiadoNaoPermitido = Cliente.builder().id(2L).name("Cliente Fiado Nao").active(true)
                .controleFiado(false).saldoDevedor(BigDecimal.ZERO).build();
        clienteInactive = Cliente.builder().id(3L).name("Cliente Inactive").active(false).build();

        // Products
        productComEstoque = Product.builder().id(10L).name("Product A").active(true).salePrice(new BigDecimal("25.50"))
                .stockQuantity(new BigDecimal("10.000")).build();
        productSemEstoque = Product.builder().id(20L).name("Product B").active(true).salePrice(new BigDecimal("10.00"))
                .stockQuantity(BigDecimal.ZERO).build(); // Estoque ZERO
        productInactive = Product.builder().id(30L).name("Product C").active(false).salePrice(new BigDecimal("5.00"))
                .stockQuantity(new BigDecimal("5.000")).build();
        productSemPreco = Product.builder().id(40L).name("Product D").active(true).salePrice(null)
                .stockQuantity(new BigDecimal("2.000")).build(); // Sem preço

        // DTOs de Itens
        ItemVendaRequestDTO item1DTO = new ItemVendaRequestDTO(productComEstoque.getId(), new BigDecimal("2.000")); // Vende
                                                                                                                    // 2
                                                                                                                    // do
                                                                                                                    // Product
                                                                                                                    // A
        ItemVendaRequestDTO item2DTO = new ItemVendaRequestDTO(productSemEstoque.getId(), new BigDecimal("1.000")); // Vende
                                                                                                                    // 1
                                                                                                                    // do
                                                                                                                    // Product
                                                                                                                    // B

        // DTOs de Venda
        vendaFiadoRequestDTO = new VendaRequestDTO(clienteActiveFiadoPermitido.getId(), List.of(item1DTO),
                FormaPagamento.FIADO, "Obs Fiado");
        vendaDinheiroRequestDTO = new VendaRequestDTO(null, List.of(item1DTO, item2DTO), FormaPagamento.DINHEIRO,
                "Obs Dinheiro"); // Sem cliente
        vendaComItemSemEstoqueDTO = new VendaRequestDTO(null,
                List.of(new ItemVendaRequestDTO(productComEstoque.getId(), new BigDecimal("15.000"))),
                FormaPagamento.PIX, ""); // Qtd > Estoque
        vendaComItemInactiveDTO = new VendaRequestDTO(null,
                List.of(new ItemVendaRequestDTO(productInactive.getId(), new BigDecimal("1.000"))),
                FormaPagamento.DEBITO, "");
        vendaComItemSemPrecoDTO = new VendaRequestDTO(null,
                List.of(new ItemVendaRequestDTO(productSemPreco.getId(), new BigDecimal("1.000"))),
                FormaPagamento.CREDITO, "");
        vendaFiadoLimiteExcedidoDTO = new VendaRequestDTO(clienteActiveFiadoPermitido.getId(),
                List.of(new ItemVendaRequestDTO(productComEstoque.getId(), new BigDecimal("4.000"))),
                FormaPagamento.FIADO, ""); // 4 * 25.50 = 102. Saldo 10 + 102 = 112 > Limite 100
        vendaFiadoClienteNaoPermitidoDTO = new VendaRequestDTO(clienteActiveFiadoNaoPermitido.getId(),
                List.of(item1DTO),
                FormaPagamento.FIADO, "");
        vendaFiadoClienteInactiveDTO = new VendaRequestDTO(clienteInactive.getId(), List.of(item1DTO),
                FormaPagamento.FIADO, "");

    }

    // --- Testes registrarVenda ---

    @Test
    @DisplayName("Deve registrar venda DINHEIRO com sucesso e atualizar estoque do item controlável")
    void registrarVenda_DinheiroOk_DeveSalvarVendaEAtualizarEstoque() {
        // Arrange
        // Mock products (são buscados no loop)
        when(productRepositoryMock.findById(productComEstoque.getId())).thenReturn(Optional.of(productComEstoque));
        when(productRepositoryMock.findById(productSemEstoque.getId())).thenReturn(Optional.of(productSemEstoque));
        // Mock save da venda (precisa retornar a venda com ID)
        when(vendaRepositoryMock.save(any(Venda.class))).thenAnswer(invocation -> {
            Venda v = invocation.getArgument(0);
            v.setId(100L); // Simula ID gerado
            v.setDataVenda(ZonedDateTime.now()); // Simula @CreationTimestamp
            return v;
        });

        // Act
        VendaResponseDTO response = vendaService.registrarVenda(vendaDinheiroRequestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(FormaPagamento.DINHEIRO, response.formaPagamento());
        assertNull(response.cliente()); // Cliente é nulo pois não é FIADO
        assertEquals(2, response.itens().size()); // 2 itens na venda
        // Value Total = (2 * 25.50) + (1 * 10.00) = 51.00 + 10.00 = 61.00
        assertEquals(0, new BigDecimal("61.00").compareTo(response.valueTotal()));
        assertEquals(productComEstoque.getId(), response.itens().get(0).product().id());
        assertEquals(0, new BigDecimal("2.000").compareTo(response.itens().get(0).quantidade()));
        assertEquals(0, productComEstoque.getSalePrice().compareTo(response.itens().get(0).precoUnitarioVenda()));
        assertEquals(productSemEstoque.getId(), response.itens().get(1).product().id());

        // Verifica save da Venda
        verify(vendaRepositoryMock).save(vendaCaptor.capture());
        Venda vendaSalva = vendaCaptor.getValue();
        assertEquals(FormaPagamento.DINHEIRO, vendaSalva.getFormaPagamento());
        assertEquals(0, new BigDecimal("61.00").compareTo(vendaSalva.getValueTotal()));
        assertNull(vendaSalva.getCliente());

        // Verifica atualização de estoque
        verify(productRepositoryMock).saveAll(productsListCaptor.capture());
        List<Product> productsAtualizados = productsListCaptor.getValue();
        assertEquals(1, productsAtualizados.size()); // Apenas o product COM estoque foi atualizado
        assertEquals(productComEstoque.getId(), productsAtualizados.get(0).getId());
        // Estoque inicial 10.000 - vendido 2.000 = 8.000
        assertEquals(0, new BigDecimal("8.000").compareTo(productsAtualizados.get(0).getStockQuantity()));

        // Verifica que cliente não foi buscado nem salvo
        verify(clienteRepositoryMock, never()).findById(any());
        verify(clienteRepositoryMock, never()).save(any());
    }

    @Test
    @DisplayName("Deve registrar venda FIADO com sucesso, atualizar estoque e saldo do cliente")
    void registrarVenda_FiadoOk_DeveSalvarTudoEAtualizarSaldo() {
        // Arrange
        when(clienteRepositoryMock.findById(clienteActiveFiadoPermitido.getId()))
                .thenReturn(Optional.of(clienteActiveFiadoPermitido));
        when(productRepositoryMock.findById(productComEstoque.getId())).thenReturn(Optional.of(productComEstoque));
        when(vendaRepositoryMock.save(any(Venda.class))).thenAnswer(invocation -> {
            Venda v = invocation.getArgument(0);
            v.setId(101L);
            v.setDataVenda(ZonedDateTime.now());
            // Simular a associação Cliente->Venda feita no save (para o DTO de resposta)
            v.setCliente(clienteActiveFiadoPermitido);
            return v;
        });
        // Mock save do cliente (retorna o próprio cliente modificado)
        when(clienteRepositoryMock.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(configuracaoServiceMock.buscarEntidadeConfiguracao()).thenReturn(Optional.empty());

        // Act
        VendaResponseDTO response = vendaService.registrarVenda(vendaFiadoRequestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(FormaPagamento.FIADO, response.formaPagamento());
        assertNotNull(response.cliente());
        assertEquals(clienteActiveFiadoPermitido.getId(), response.cliente().id());
        assertEquals(1, response.itens().size());
        // Value Total = 2 * 25.50 = 51.00
        assertEquals(0, new BigDecimal("51.00").compareTo(response.valueTotal()));

        // Verifica save da Venda
        verify(vendaRepositoryMock).save(vendaCaptor.capture());
        assertEquals(clienteActiveFiadoPermitido, vendaCaptor.getValue().getCliente());
        assertEquals(0, new BigDecimal("51.00").compareTo(vendaCaptor.getValue().getValueTotal()));

        // Verifica atualização de estoque
        verify(productRepositoryMock).saveAll(productsListCaptor.capture());
        assertEquals(1, productsListCaptor.getValue().size());
        assertEquals(productComEstoque.getId(), productsListCaptor.getValue().get(0).getId());
        assertEquals(0, new BigDecimal("8.000").compareTo(productsListCaptor.getValue().get(0).getStockQuantity()));

        // Verifica atualização do Cliente
        verify(clienteRepositoryMock).save(clienteCaptor.capture());
        Cliente clienteSalvo = clienteCaptor.getValue();
        // Saldo inicial 10.00 + Venda 51.00 = 61.00
        assertEquals(0, new BigDecimal("61.00").compareTo(clienteSalvo.getSaldoDevedor()));

        verify(configuracaoServiceMock).buscarEntidadeConfiguracao();
    }

    // --- Testes de Falha registrarVenda ---

    @Test
    @DisplayName("Deve falhar ao registrar venda FIADO se cliente não for encontrado")
    void registrarVenda_FiadoClienteNaoEncontrado_DeveLancarExcecao() {
        // Arrange
        when(clienteRepositoryMock.findById(clienteActiveFiadoPermitido.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
            vendaService.registrarVenda(vendaFiadoRequestDTO);
        });
        assertNotNull(exception); // Optionally, assert that the exception is not null
        verify(clienteRepositoryMock).findById(clienteActiveFiadoPermitido.getId());
        verifyNoInteractions(vendaRepositoryMock, productRepositoryMock); // Nenhum outro repo deve ser chamado
        verify(clienteRepositoryMock, never()).save(any()); // Save do cliente nunca ocorre
    }

    @Test
    @DisplayName("Deve falhar ao registrar venda FIADO se cliente estiver inactive")
    void registrarVenda_FiadoClienteInactive_DeveLancarExcecao() {
        // Arrange
        when(clienteRepositoryMock.findById(clienteInactive.getId())).thenReturn(Optional.of(clienteInactive));

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> { // Ou a exceção específica que
                                                                                          // você usar
            vendaService.registrarVenda(vendaFiadoClienteInactiveDTO);
        });
        assertNotNull(exception); // Optionally, assert that the exception is not null
        verify(clienteRepositoryMock).findById(clienteInactive.getId());
        verifyNoInteractions(vendaRepositoryMock, productRepositoryMock);
        verify(clienteRepositoryMock, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao registrar venda FIADO se cliente não pode comprar fiado")
    void registrarVenda_FiadoClienteNaoPermitido_DeveLancarExcecao() {
        // Arrange
        when(clienteRepositoryMock.findById(clienteActiveFiadoNaoPermitido.getId()))
                .thenReturn(Optional.of(clienteActiveFiadoNaoPermitido));

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
            vendaService.registrarVenda(vendaFiadoClienteNaoPermitidoDTO);
        });
        assertNotNull(exception); // Optionally, assert that the exception is not null
        verify(clienteRepositoryMock).findById(clienteActiveFiadoNaoPermitido.getId());
        verifyNoInteractions(vendaRepositoryMock, productRepositoryMock);
        verify(clienteRepositoryMock, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao registrar venda se product não for encontrado")
    void registrarVenda_ProductNaoEncontrado_DeveLancarExcecao() {
        // Arrange
        when(productRepositoryMock.findById(productComEstoque.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
            vendaService.registrarVenda(vendaDinheiroRequestDTO); // Usando venda dinheiro (não precisa cliente)
        });
        assertNotNull(exception); // Optionally, assert that the exception is not null
        verify(productRepositoryMock).findById(productComEstoque.getId());
        verifyNoInteractions(vendaRepositoryMock, clienteRepositoryMock); // Venda e cliente não devem ser salvos
        verify(productRepositoryMock, never()).saveAll(anyList()); // Estoque não atualizado
    }

    @Test
    @DisplayName("Deve falhar ao registrar venda se product estiver inactive")
    void registrarVenda_ProductInactive_DeveLancarExcecao() {
        // Arrange
        when(productRepositoryMock.findById(productInactive.getId())).thenReturn(Optional.of(productInactive));

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
            vendaService.registrarVenda(vendaComItemInactiveDTO);
        });
        assertNotNull(exception); // Optionally, assert that the exception is not null
        verify(productRepositoryMock).findById(productInactive.getId());
        verifyNoInteractions(vendaRepositoryMock, clienteRepositoryMock);
        verify(productRepositoryMock, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve falhar ao registrar venda se product estiver sem preço")
    void registrarVenda_ProductSemPreco_DeveLancarExcecao() {
        // Arrange
        when(productRepositoryMock.findById(productSemPreco.getId())).thenReturn(Optional.of(productSemPreco));

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
            vendaService.registrarVenda(vendaComItemSemPrecoDTO);
        });
        assertNotNull(exception); // Optionally, assert that the exception is not null
        verify(productRepositoryMock).findById(productSemPreco.getId());
        verifyNoInteractions(vendaRepositoryMock, clienteRepositoryMock);
        verify(productRepositoryMock, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve falhar ao registrar venda se estoque for insuficiente (e controlado)")
    void registrarVenda_EstoqueInsuficiente_DeveLancarExcecao() {
        // Arrange
        // O productComEstoque tem 10.000, o DTO pede 15.000
        when(productRepositoryMock.findById(productComEstoque.getId())).thenReturn(Optional.of(productComEstoque));

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
            vendaService.registrarVenda(vendaComItemSemEstoqueDTO);
        });
        assertNotNull(exception);
        verify(productRepositoryMock).findById(productComEstoque.getId());
        verifyNoInteractions(vendaRepositoryMock, clienteRepositoryMock);
        verify(productRepositoryMock, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve falhar ao registrar venda FIADO se limite for excedido")
    void registrarVenda_FiadoLimiteExcedido_DeveLancarExcecaoEReverter() {
        // Arrange
        when(clienteRepositoryMock.findById(clienteActiveFiadoPermitido.getId()))
                .thenReturn(Optional.of(clienteActiveFiadoPermitido));
        when(productRepositoryMock.findById(productComEstoque.getId())).thenReturn(Optional.of(productComEstoque));
        // Simula save da venda (acontece ANTES da validação do limite)
        when(vendaRepositoryMock.save(any(Venda.class))).thenAnswer(invocation -> {
            Venda v = invocation.getArgument(0);
            v.setId(102L);
            v.setDataVenda(ZonedDateTime.now());
            // Associa cliente para a validação posterior
            v.setCliente(clienteActiveFiadoPermitido);
            // Calcula o value total que será usado na validação
            // 4 * 25.50 = 102.00
            v.setValueTotal(new BigDecimal("102.00"));
            return v;
        });

        when(configuracaoServiceMock.buscarEntidadeConfiguracao()).thenReturn(Optional.empty());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            vendaService.registrarVenda(vendaFiadoLimiteExcedidoDTO);
        });
        assertTrue(exception.getMessage().contains("Limite fiado excedido"));

        // Verifica que a Venda chegou a ser salva (antes da exceção)
        verify(vendaRepositoryMock).save(any(Venda.class));
        // Verifica que o Estoque chegou a ser atualizado (antes da exceção)
        verify(productRepositoryMock).saveAll(anyList());
        // Verifica que o Cliente NÃO foi salvo (a exceção ocorreu antes disso)
        verify(clienteRepositoryMock, never()).save(any(Cliente.class));
        // IMPORTANTE: Em um teste de integração real com @Transactional, o rollback
        // desfaria o save da Venda e do Estoque. Aqui no teste unitário, apenas
        // verificamos que a exceção ocorreu onde esperado e o save do cliente não
        // aconteceu.
        verify(configuracaoServiceMock).buscarEntidadeConfiguracao();
    }

    // --- Testes listVendas e buscarVendaPorId ---
    // (São mais simples, focam em verificar se o repo é chamado e o DTO é
    // retornado)

    @Test
    @DisplayName("Deve list vendas chamando")
    void listVendas_semFilters_DeveRetornarPageResponse() {
        // Arrange
        Venda v1 = Venda.builder()
                .id(1L)
                .cliente(clienteActiveFiadoPermitido)
                .valueTotal(BigDecimal.TEN)
                .dataVenda(ZonedDateTime.now().minusDays(1))
                .formaPagamento(FormaPagamento.FIADO)
                .build();

        Venda v2 = Venda.builder()
                .id(2L)
                .cliente(null)
                .valueTotal(BigDecimal.ONE)
                .dataVenda(ZonedDateTime.now())
                .formaPagamento(FormaPagamento.DINHEIRO)
                .build();

        List<Venda> vendas = List.of(v1, v2);
        Pageable pageable = PageRequest.of(0, 8);
        Page<Venda> vendasPageMock = new PageImpl<>(vendas, pageable, vendas.size());

        when(vendaRepositoryMock.findVendasComFilters(any(ZonedDateTime.class), any(ZonedDateTime.class), any(), any(),
                any(), any(Pageable.class)))
                .thenReturn(vendasPageMock);

        PageResponse<VendaResponseDTO> resultado = vendaService.listVendas(null, null, null, null, null, null, 0, 8);

        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size(), "Deveria encontrar 2 vendas");
        assertEquals(v1.getId(), resultado.getContent().get(0).id());
        assertEquals(v2.getId(), resultado.getContent().get(1).id());
        verify(vendaRepositoryMock).findVendasComFilters(any(), any(), isNull(), isNull(), isNull(),
                any(Pageable.class));
    }

    @Test
    @DisplayName("Deve buscar venda por ID com sucesso")
    void buscarVendaPorId_QuandoEncontrado_DeveRetornarDto() {
        // Arrange
        Venda vendaMock = Venda.builder()
                .id(1L)
                .cliente(clienteActiveFiadoPermitido)
                .valueTotal(BigDecimal.TEN)
                .formaPagamento(FormaPagamento.FIADO)
                .dataVenda(ZonedDateTime.now())
                .build();
        // Adicionar itens se o DTO precisar deles para o construtor
        ItemVenda itemMock = ItemVenda.builder().id(1L).product(productComEstoque).quantidade(BigDecimal.ONE)
                .precoUnitarioVenda(productComEstoque.getSalePrice()).build();
        vendaMock.adicionarItem(itemMock); // Adiciona item ao mock da venda

        when(vendaRepositoryMock.findByIdComItensECliente(1L)).thenReturn(Optional.of(vendaMock));

        // Act
        VendaResponseDTO resultado = vendaService.buscarVendaPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(vendaMock.getId(), resultado.id());
        assertEquals(vendaMock.getValueTotal(), resultado.valueTotal());
        assertNotNull(resultado.cliente());
        assertFalse(resultado.itens().isEmpty()); // Verifica se os itens foram mapeados

        verify(vendaRepositoryMock).findByIdComItensECliente(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar venda por ID inexistente")
    void buscarVendaPorId_QuandoNaoEncontrado_DeveLancarExcecao() {
        // Arrange
        Long idInexistente = 999L; // Define a non-existent ID for testing
        when(vendaRepositoryMock.findByIdComItensECliente(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class,
                () -> vendaService.buscarVendaPorId(idInexistente));
        assertNotNull(exception);
        verify(vendaRepositoryMock).findByIdComItensECliente(idInexistente);
    }

}