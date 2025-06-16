package br.com.king.flick_business.service;

import java.math.BigDecimal; // Importar DTOs
import java.time.LocalDateTime; // Importar Entidades
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
import org.springframework.data.domain.Sort;

import br.com.king.flick_business.dto.ItemVendaRequestDTO;
import br.com.king.flick_business.dto.VendaRequestDTO;
import br.com.king.flick_business.dto.VendaResponseDTO;
import br.com.king.flick_business.entity.Cliente;
import br.com.king.flick_business.entity.ItemVenda;
import br.com.king.flick_business.entity.Produto;
import br.com.king.flick_business.entity.Venda;
import br.com.king.flick_business.enums.FormaPagamento;
import br.com.king.flick_business.exception.BusinessException;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.repository.ClienteRepository;
import br.com.king.flick_business.repository.ProdutoRepository;
import br.com.king.flick_business.repository.VendaRepository;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepositoryMock;
    @Mock
    private ClienteRepository clienteRepositoryMock;
    @Mock
    private ProdutoRepository produtoRepositoryMock; // Mock do repositório agora
    @Mock
    private ConfiguracaoGeralService configuracaoServiceMock;

    @InjectMocks
    private VendaService vendaService;

    // Captors para verificar objetos salvos
    @Captor
    private ArgumentCaptor<Venda> vendaCaptor;
    @Captor
    private ArgumentCaptor<List<Produto>> produtosListCaptor;
    @Captor
    private ArgumentCaptor<Cliente> clienteCaptor;

    // Dados de teste
    private Cliente clienteAtivoFiadoPermitido;
    private Cliente clienteAtivoFiadoNaoPermitido;
    private Cliente clienteInativo;
    private Produto produtoComEstoque;
    private Produto produtoSemEstoque;
    private Produto produtoInativo;
    private Produto produtoSemPreco;
    private VendaRequestDTO vendaFiadoRequestDTO;
    private VendaRequestDTO vendaDinheiroRequestDTO;
    private VendaRequestDTO vendaComItemSemEstoqueDTO;
    private VendaRequestDTO vendaComItemInativoDTO;
    private VendaRequestDTO vendaComItemSemPrecoDTO;
    private VendaRequestDTO vendaFiadoLimiteExcedidoDTO;
    private VendaRequestDTO vendaFiadoClienteNaoPermitidoDTO;
    private VendaRequestDTO vendaFiadoClienteInativoDTO;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        // Clientes
        clienteAtivoFiadoPermitido = Cliente.builder().id(1L).nome("Cliente Fiado OK").ativo(true).controleFiado(true)
                .limiteFiado(new BigDecimal("100.00")).saldoDevedor(new BigDecimal("10.00")).build();
        clienteAtivoFiadoNaoPermitido = Cliente.builder().id(2L).nome("Cliente Fiado Nao").ativo(true)
                .controleFiado(false).saldoDevedor(BigDecimal.ZERO).build();
        clienteInativo = Cliente.builder().id(3L).nome("Cliente Inativo").ativo(false).build();

        // Produtos
        produtoComEstoque = Produto.builder().id(10L).nome("Produto A").ativo(true).precoVenda(new BigDecimal("25.50"))
                .quantidadeEstoque(new BigDecimal("10.000")).build();
        produtoSemEstoque = Produto.builder().id(20L).nome("Produto B").ativo(true).precoVenda(new BigDecimal("10.00"))
                .quantidadeEstoque(BigDecimal.ZERO).build(); // Estoque ZERO
        produtoInativo = Produto.builder().id(30L).nome("Produto C").ativo(false).precoVenda(new BigDecimal("5.00"))
                .quantidadeEstoque(new BigDecimal("5.000")).build();
        produtoSemPreco = Produto.builder().id(40L).nome("Produto D").ativo(true).precoVenda(null)
                .quantidadeEstoque(new BigDecimal("2.000")).build(); // Sem preço

        // DTOs de Itens
        ItemVendaRequestDTO item1DTO = new ItemVendaRequestDTO(produtoComEstoque.getId(), new BigDecimal("2.000")); // Vende
                                                                                                                    // 2
                                                                                                                    // do
                                                                                                                    // Produto
                                                                                                                    // A
        ItemVendaRequestDTO item2DTO = new ItemVendaRequestDTO(produtoSemEstoque.getId(), new BigDecimal("1.000")); // Vende
                                                                                                                    // 1
                                                                                                                    // do
                                                                                                                    // Produto
                                                                                                                    // B

        // DTOs de Venda
        vendaFiadoRequestDTO = new VendaRequestDTO(clienteAtivoFiadoPermitido.getId(), List.of(item1DTO),
                FormaPagamento.FIADO, "Obs Fiado");
        vendaDinheiroRequestDTO = new VendaRequestDTO(null, List.of(item1DTO, item2DTO), FormaPagamento.DINHEIRO,
                "Obs Dinheiro"); // Sem cliente
        vendaComItemSemEstoqueDTO = new VendaRequestDTO(null,
                List.of(new ItemVendaRequestDTO(produtoComEstoque.getId(), new BigDecimal("15.000"))),
                FormaPagamento.PIX, ""); // Qtd > Estoque
        vendaComItemInativoDTO = new VendaRequestDTO(null,
                List.of(new ItemVendaRequestDTO(produtoInativo.getId(), new BigDecimal("1.000"))),
                FormaPagamento.DEBITO, "");
        vendaComItemSemPrecoDTO = new VendaRequestDTO(null,
                List.of(new ItemVendaRequestDTO(produtoSemPreco.getId(), new BigDecimal("1.000"))),
                FormaPagamento.CREDITO, "");
        vendaFiadoLimiteExcedidoDTO = new VendaRequestDTO(clienteAtivoFiadoPermitido.getId(),
                List.of(new ItemVendaRequestDTO(produtoComEstoque.getId(), new BigDecimal("4.000"))),
                FormaPagamento.FIADO, ""); // 4 * 25.50 = 102. Saldo 10 + 102 = 112 > Limite 100
        vendaFiadoClienteNaoPermitidoDTO = new VendaRequestDTO(clienteAtivoFiadoNaoPermitido.getId(), List.of(item1DTO),
                FormaPagamento.FIADO, "");
        vendaFiadoClienteInativoDTO = new VendaRequestDTO(clienteInativo.getId(), List.of(item1DTO),
                FormaPagamento.FIADO, "");

    }

    // --- Testes registrarVenda ---

    @Test
    @DisplayName("Deve registrar venda DINHEIRO com sucesso e atualizar estoque do item controlável")
    void registrarVenda_DinheiroOk_DeveSalvarVendaEAtualizarEstoque() {
        // Arrange
        // Mock produtos (são buscados no loop)
        when(produtoRepositoryMock.findById(produtoComEstoque.getId())).thenReturn(Optional.of(produtoComEstoque));
        when(produtoRepositoryMock.findById(produtoSemEstoque.getId())).thenReturn(Optional.of(produtoSemEstoque));
        // Mock save da venda (precisa retornar a venda com ID)
        when(vendaRepositoryMock.save(any(Venda.class))).thenAnswer(invocation -> {
            Venda v = invocation.getArgument(0);
            v.setId(100L); // Simula ID gerado
            v.setDataVenda(LocalDateTime.now()); // Simula @CreationTimestamp
            return v;
        });

        // Act
        VendaResponseDTO response = vendaService.registrarVenda(vendaDinheiroRequestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(FormaPagamento.DINHEIRO, response.formaPagamento());
        assertNull(response.cliente()); // Cliente é nulo pois não é FIADO
        assertEquals(2, response.itens().size()); // 2 itens na venda
        // Valor Total = (2 * 25.50) + (1 * 10.00) = 51.00 + 10.00 = 61.00
        assertEquals(0, new BigDecimal("61.00").compareTo(response.valorTotal()));
        assertEquals(produtoComEstoque.getId(), response.itens().get(0).produto().id());
        assertEquals(0, new BigDecimal("2.000").compareTo(response.itens().get(0).quantidade()));
        assertEquals(0, produtoComEstoque.getPrecoVenda().compareTo(response.itens().get(0).precoUnitarioVenda()));
        assertEquals(produtoSemEstoque.getId(), response.itens().get(1).produto().id());

        // Verifica save da Venda
        verify(vendaRepositoryMock).save(vendaCaptor.capture());
        Venda vendaSalva = vendaCaptor.getValue();
        assertEquals(FormaPagamento.DINHEIRO, vendaSalva.getFormaPagamento());
        assertEquals(0, new BigDecimal("61.00").compareTo(vendaSalva.getValorTotal()));
        assertNull(vendaSalva.getCliente());

        // Verifica atualização de estoque
        verify(produtoRepositoryMock).saveAll(produtosListCaptor.capture());
        List<Produto> produtosAtualizados = produtosListCaptor.getValue();
        assertEquals(1, produtosAtualizados.size()); // Apenas o produto COM estoque foi atualizado
        assertEquals(produtoComEstoque.getId(), produtosAtualizados.get(0).getId());
        // Estoque inicial 10.000 - vendido 2.000 = 8.000
        assertEquals(0, new BigDecimal("8.000").compareTo(produtosAtualizados.get(0).getQuantidadeEstoque()));

        // Verifica que cliente não foi buscado nem salvo
        verify(clienteRepositoryMock, never()).findById(any());
        verify(clienteRepositoryMock, never()).save(any());
    }

    @Test
    @DisplayName("Deve registrar venda FIADO com sucesso, atualizar estoque e saldo do cliente")
    void registrarVenda_FiadoOk_DeveSalvarTudoEAtualizarSaldo() {
        // Arrange
        when(clienteRepositoryMock.findById(clienteAtivoFiadoPermitido.getId()))
                .thenReturn(Optional.of(clienteAtivoFiadoPermitido));
        when(produtoRepositoryMock.findById(produtoComEstoque.getId())).thenReturn(Optional.of(produtoComEstoque));
        when(vendaRepositoryMock.save(any(Venda.class))).thenAnswer(invocation -> {
            Venda v = invocation.getArgument(0);
            v.setId(101L);
            v.setDataVenda(LocalDateTime.now());
            // Simular a associação Cliente->Venda feita no save (para o DTO de resposta)
            v.setCliente(clienteAtivoFiadoPermitido);
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
        assertEquals(clienteAtivoFiadoPermitido.getId(), response.cliente().id());
        assertEquals(1, response.itens().size());
        // Valor Total = 2 * 25.50 = 51.00
        assertEquals(0, new BigDecimal("51.00").compareTo(response.valorTotal()));

        // Verifica save da Venda
        verify(vendaRepositoryMock).save(vendaCaptor.capture());
        assertEquals(clienteAtivoFiadoPermitido, vendaCaptor.getValue().getCliente());
        assertEquals(0, new BigDecimal("51.00").compareTo(vendaCaptor.getValue().getValorTotal()));

        // Verifica atualização de estoque
        verify(produtoRepositoryMock).saveAll(produtosListCaptor.capture());
        assertEquals(1, produtosListCaptor.getValue().size());
        assertEquals(produtoComEstoque.getId(), produtosListCaptor.getValue().get(0).getId());
        assertEquals(0, new BigDecimal("8.000").compareTo(produtosListCaptor.getValue().get(0).getQuantidadeEstoque()));

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
        when(clienteRepositoryMock.findById(clienteAtivoFiadoPermitido.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
            vendaService.registrarVenda(vendaFiadoRequestDTO);
        });
        assertNotNull(exception); // Optionally, assert that the exception is not null
        verify(clienteRepositoryMock).findById(clienteAtivoFiadoPermitido.getId());
        verifyNoInteractions(vendaRepositoryMock, produtoRepositoryMock); // Nenhum outro repo deve ser chamado
        verify(clienteRepositoryMock, never()).save(any()); // Save do cliente nunca ocorre
    }

    @Test
    @DisplayName("Deve falhar ao registrar venda FIADO se cliente estiver inativo")
    void registrarVenda_FiadoClienteInativo_DeveLancarExcecao() {
        // Arrange
        when(clienteRepositoryMock.findById(clienteInativo.getId())).thenReturn(Optional.of(clienteInativo));

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> { // Ou a exceção específica que
                                                                                          // você usar
            vendaService.registrarVenda(vendaFiadoClienteInativoDTO);
        });
        assertNotNull(exception); // Optionally, assert that the exception is not null
        verify(clienteRepositoryMock).findById(clienteInativo.getId());
        verifyNoInteractions(vendaRepositoryMock, produtoRepositoryMock);
        verify(clienteRepositoryMock, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao registrar venda FIADO se cliente não pode comprar fiado")
    void registrarVenda_FiadoClienteNaoPermitido_DeveLancarExcecao() {
        // Arrange
        when(clienteRepositoryMock.findById(clienteAtivoFiadoNaoPermitido.getId()))
                .thenReturn(Optional.of(clienteAtivoFiadoNaoPermitido));

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
            vendaService.registrarVenda(vendaFiadoClienteNaoPermitidoDTO);
        });
        assertNotNull(exception); // Optionally, assert that the exception is not null
        verify(clienteRepositoryMock).findById(clienteAtivoFiadoNaoPermitido.getId());
        verifyNoInteractions(vendaRepositoryMock, produtoRepositoryMock);
        verify(clienteRepositoryMock, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao registrar venda se produto não for encontrado")
    void registrarVenda_ProdutoNaoEncontrado_DeveLancarExcecao() {
        // Arrange
        when(produtoRepositoryMock.findById(produtoComEstoque.getId())).thenReturn(Optional.empty());

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
            vendaService.registrarVenda(vendaDinheiroRequestDTO); // Usando venda dinheiro (não precisa cliente)
        });
        assertNotNull(exception); // Optionally, assert that the exception is not null
        verify(produtoRepositoryMock).findById(produtoComEstoque.getId());
        verifyNoInteractions(vendaRepositoryMock, clienteRepositoryMock); // Venda e cliente não devem ser salvos
        verify(produtoRepositoryMock, never()).saveAll(anyList()); // Estoque não atualizado
    }

    @Test
    @DisplayName("Deve falhar ao registrar venda se produto estiver inativo")
    void registrarVenda_ProdutoInativo_DeveLancarExcecao() {
        // Arrange
        when(produtoRepositoryMock.findById(produtoInativo.getId())).thenReturn(Optional.of(produtoInativo));

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
            vendaService.registrarVenda(vendaComItemInativoDTO);
        });
        assertNotNull(exception); // Optionally, assert that the exception is not null
        verify(produtoRepositoryMock).findById(produtoInativo.getId());
        verifyNoInteractions(vendaRepositoryMock, clienteRepositoryMock);
        verify(produtoRepositoryMock, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve falhar ao registrar venda se produto estiver sem preço")
    void registrarVenda_ProdutoSemPreco_DeveLancarExcecao() {
        // Arrange
        when(produtoRepositoryMock.findById(produtoSemPreco.getId())).thenReturn(Optional.of(produtoSemPreco));

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
            vendaService.registrarVenda(vendaComItemSemPrecoDTO);
        });
        assertNotNull(exception); // Optionally, assert that the exception is not null
        verify(produtoRepositoryMock).findById(produtoSemPreco.getId());
        verifyNoInteractions(vendaRepositoryMock, clienteRepositoryMock);
        verify(produtoRepositoryMock, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve falhar ao registrar venda se estoque for insuficiente (e controlado)")
    void registrarVenda_EstoqueInsuficiente_DeveLancarExcecao() {
        // Arrange
        // O produtoComEstoque tem 10.000, o DTO pede 15.000
        when(produtoRepositoryMock.findById(produtoComEstoque.getId())).thenReturn(Optional.of(produtoComEstoque));

        // Act & Assert
        RecursoNaoEncontrado exception = assertThrows(RecursoNaoEncontrado.class, () -> {
            vendaService.registrarVenda(vendaComItemSemEstoqueDTO);
        });
        assertNotNull(exception);
        verify(produtoRepositoryMock).findById(produtoComEstoque.getId());
        verifyNoInteractions(vendaRepositoryMock, clienteRepositoryMock);
        verify(produtoRepositoryMock, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve falhar ao registrar venda FIADO se limite for excedido")
    void registrarVenda_FiadoLimiteExcedido_DeveLancarExcecaoEReverter() {
        // Arrange
        when(clienteRepositoryMock.findById(clienteAtivoFiadoPermitido.getId()))
                .thenReturn(Optional.of(clienteAtivoFiadoPermitido));
        when(produtoRepositoryMock.findById(produtoComEstoque.getId())).thenReturn(Optional.of(produtoComEstoque));
        // Simula save da venda (acontece ANTES da validação do limite)
        when(vendaRepositoryMock.save(any(Venda.class))).thenAnswer(invocation -> {
            Venda v = invocation.getArgument(0);
            v.setId(102L);
            v.setDataVenda(LocalDateTime.now());
            // Associa cliente para a validação posterior
            v.setCliente(clienteAtivoFiadoPermitido);
            // Calcula o valor total que será usado na validação
            // 4 * 25.50 = 102.00
            v.setValorTotal(new BigDecimal("102.00"));
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
        verify(produtoRepositoryMock).saveAll(anyList());
        // Verifica que o Cliente NÃO foi salvo (a exceção ocorreu antes disso)
        verify(clienteRepositoryMock, never()).save(any(Cliente.class));
        // IMPORTANTE: Em um teste de integração real com @Transactional, o rollback
        // desfaria o save da Venda e do Estoque. Aqui no teste unitário, apenas
        // verificamos que a exceção ocorreu onde esperado e o save do cliente não
        // aconteceu.
        verify(configuracaoServiceMock).buscarEntidadeConfiguracao();
    }

    // --- Testes listarVendas e buscarVendaPorId ---
    // (São mais simples, focam em verificar se o repo é chamado e o DTO é
    // retornado)

    @Test
    @DisplayName("Deve listar vendas chamando")
    void listarVendas_semFiltros_DeveRetornarListaDto() {
        // Arrange
        Venda v1 = Venda.builder().id(1L).cliente(clienteAtivoFiadoPermitido).valorTotal(BigDecimal.TEN)
                .dataVenda(LocalDateTime.now().minusDays(1)).formaPagamento(FormaPagamento.FIADO).build();

        Venda v2 = Venda.builder().id(2L).cliente(null).valorTotal(BigDecimal.ONE).dataVenda(LocalDateTime.now())
                .formaPagamento(FormaPagamento.DINHEIRO).build();

        List<Venda> listaDeVendasMock = List.of(v1, v2);

        when(vendaRepositoryMock.findVendasComFiltros(any(), any(), any(), any(), any(), any(Sort.class)))
                .thenReturn(listaDeVendasMock);

        List<VendaResponseDTO> resultado = vendaService.listarVendas(null, null, null, null, null);

        assertNotNull(resultado);
        assertEquals(2, resultado.size(), "Deveria encontrar 2 vendas");
        assertEquals(v1.getId(), resultado.get(0).id());
        assertEquals(v2.getId(), resultado.get(1).id());
        verify(vendaRepositoryMock).findVendasComFiltros(any(), any(), isNull(), isNull(), isNull(), any(Sort.class));
    }

    @Test
    @DisplayName("Deve buscar venda por ID com sucesso")
    void buscarVendaPorId_QuandoEncontrado_DeveRetornarDto() {
        // Arrange
        Venda vendaMock = Venda.builder()
                .id(1L)
                .cliente(clienteAtivoFiadoPermitido)
                .valorTotal(BigDecimal.TEN)
                .formaPagamento(FormaPagamento.FIADO)
                .dataVenda(LocalDateTime.now())
                .build();
        // Adicionar itens se o DTO precisar deles para o construtor
        ItemVenda itemMock = ItemVenda.builder().id(1L).produto(produtoComEstoque).quantidade(BigDecimal.ONE)
                .precoUnitarioVenda(produtoComEstoque.getPrecoVenda()).build();
        vendaMock.adicionarItem(itemMock); // Adiciona item ao mock da venda

        when(vendaRepositoryMock.findByIdComItensECliente(1L)).thenReturn(Optional.of(vendaMock));

        // Act
        VendaResponseDTO resultado = vendaService.buscarVendaPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(vendaMock.getId(), resultado.id());
        assertEquals(vendaMock.getValorTotal(), resultado.valorTotal());
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