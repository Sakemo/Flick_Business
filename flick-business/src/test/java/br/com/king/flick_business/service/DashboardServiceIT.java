package br.com.king.flick_business.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.LocalTime; // Importar todas as entidades
import java.time.ZoneId;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse; // Importar todos os repositórios
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test; // Para usar application-test.properties
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.king.flick_business.dto.DashboardSummaryDTO;
import br.com.king.flick_business.dto.DataPointDTO;
import br.com.king.flick_business.entity.Category;
import br.com.king.flick_business.entity.Cliente;
import br.com.king.flick_business.entity.Expense;
import br.com.king.flick_business.entity.Provider;
import br.com.king.flick_business.entity.ItemVenda;
import br.com.king.flick_business.entity.Product;
import br.com.king.flick_business.entity.Venda;
import br.com.king.flick_business.enums.FormaPagamento;
import br.com.king.flick_business.enums.TipoExpense;
import br.com.king.flick_business.repository.CategoryRepository;
import br.com.king.flick_business.repository.ClienteRepository;
import br.com.king.flick_business.repository.ExpenseRepository;
import br.com.king.flick_business.repository.ProviderRepository;
import br.com.king.flick_business.repository.ItemVendaRepository;
import br.com.king.flick_business.repository.ProductRepository;
import br.com.king.flick_business.repository.VendaRepository;

@SpringBootTest // Carrega o contexto completo da aplicação Spring Boot
@ActiveProfiles("test") // Ativa o application-test.properties
class DashboardServiceIT {

        @Autowired
        private DashboardService dashboardService;

        // Injetar repositórios para popular dados diretamente
        @Autowired
        private VendaRepository vendaRepository;
        @Autowired
        private ItemVendaRepository itemVendaRepository;
        @Autowired
        private ProductRepository productRepository;
        @Autowired
        private ClienteRepository clienteRepository;
        @Autowired
        private CategoryRepository categoryRepository;
        @Autowired
        private ProviderRepository providerRepository;
        @Autowired
        private ExpenseRepository expenseRepository;

        // Dados de referência
        private Cliente cliente1;
        private Product productA, productB, productC;
        private ZonedDateTime hojeMeioDia, ontemMeioDia, inicioMesPassado, fimMesPassado;

        @BeforeEach
        void setUpDatabaseTestData() {
                // Limpar dados antes de cada teste (ddl-auto=create-drop já faz isso, mas pode
                // ser explícito)
                itemVendaRepository.deleteAll();
                vendaRepository.deleteAll();
                productRepository.deleteAll();
                clienteRepository.deleteAll();
                categoryRepository.deleteAll();
                providerRepository.deleteAll();
                expenseRepository.deleteAll();

                // Datas de referência
                LocalDate hoje = LocalDate.now();
                hojeMeioDia = hoje.atTime(LocalTime.NOON).atZone(java.time.ZoneId.systemDefault());
                ontemMeioDia = hoje.minusDays(1).atTime(LocalTime.NOON).atZone(java.time.ZoneId.systemDefault());
                inicioMesPassado = hoje.minusMonths(1).withDayOfMonth(1).atStartOfDay()
                                .atZone(java.time.ZoneId.systemDefault());
                fimMesPassado = hoje.minusMonths(1).withDayOfMonth(hoje.minusMonths(1).lengthOfMonth())
                                .atTime(LocalTime.MAX).atZone(java.time.ZoneId.systemDefault());

                // Criar entidades base
                Category categoryEletronicos = categoryRepository
                                .save(Category.builder().name("Eletrônicos").build());
                Provider providerX = providerRepository.save(Provider.builder().name("Provider X").build());

                cliente1 = clienteRepository.save(Cliente.builder().name("Cliente Teste 1").cpf("111").active(true)
                                .controleFiado(true).limiteFiado(new BigDecimal("200")).saldoDevedor(BigDecimal.ZERO)
                                .build());
                Cliente cliente2 = clienteRepository
                                .save(Cliente.builder().name("Cliente Teste 2").cpf("222").active(true)
                                                .controleFiado(false).saldoDevedor(BigDecimal.ZERO).build());

                productA = productRepository
                                .save(Product.builder().name("Product A").category(categoryEletronicos)
                                                .provider(providerX)
                                                .salePrice(new BigDecimal("100.00"))
                                                .stockQuantity(new BigDecimal("10")).active(true)
                                                .build());
                productB = productRepository
                                .save(Product.builder().name("Product B").category(categoryEletronicos)
                                                .provider(providerX)
                                                .salePrice(new BigDecimal("50.00"))
                                                .stockQuantity(new BigDecimal("5")).active(true)
                                                .build());
                productC = productRepository
                                .save(Product.builder().name("Product C").category(categoryEletronicos)
                                                .provider(providerX)
                                                .salePrice(new BigDecimal("20.00"))
                                                .stockQuantity(new BigDecimal("20")).active(true)
                                                .build());

                // --- VENDAS ---
                // Venda 1: Hoje, PIX, Product A (2 unidades), Product B (1 unidade)
                Venda venda1 = Venda.builder().cliente(null).formaPagamento(FormaPagamento.PIX).dataVenda(hojeMeioDia)
                                .observacoes("Venda PIX hoje").build();
                ItemVenda item1_1 = ItemVenda.builder().product(productA).quantidade(new BigDecimal("2.000"))
                                .precoUnitarioVenda(productA.getSalePrice()).build(); // 200.00
                ItemVenda item1_2 = ItemVenda.builder().product(productB).quantidade(new BigDecimal("1.000"))
                                .precoUnitarioVenda(productB.getSalePrice()).build(); // 50.00
                venda1.adicionarItem(item1_1);
                venda1.adicionarItem(item1_2);
                venda1.setValorTotal(item1_1.getValorTotalItem().add(item1_2.getValorTotalItem())); // 250.00
                vendaRepository.save(venda1);

                // Venda 2: Ontem, FIADO (Cliente 1), Product B (3 unidades)
                Venda venda2 = Venda.builder().cliente(cliente1).formaPagamento(FormaPagamento.FIADO)
                                .dataVenda(ontemMeioDia)
                                .observacoes("Venda Fiado ontem").build();
                ItemVenda item2_1 = ItemVenda.builder().product(productB).quantidade(new BigDecimal("3.000"))
                                .precoUnitarioVenda(productB.getSalePrice()).build(); // 150.00
                venda2.adicionarItem(item2_1);
                venda2.setValorTotal(item2_1.getValorTotalItem()); // 150.00
                vendaRepository.save(venda2);
                cliente1.setSaldoDevedor(cliente1.getSaldoDevedor().add(venda2.getValorTotal()));
                cliente1.setDataUltimaCompraFiado(venda2.getDataVenda());
                clienteRepository.save(cliente1);

                // Venda 3: Mês passado, DINHEIRO, Product C (5 unidades)
                Venda venda3 = Venda.builder().cliente(null).formaPagamento(FormaPagamento.DINHEIRO)
                                .dataVenda(inicioMesPassado.plusDays(5)).observacoes("Venda Dinheiro mês passado")
                                .build();
                ItemVenda item3_1 = ItemVenda.builder().product(productC).quantidade(new BigDecimal("5.000"))
                                .precoUnitarioVenda(productC.getSalePrice()).build(); // 100.00
                venda3.adicionarItem(item3_1);
                venda3.setValorTotal(item3_1.getValorTotalItem()); // 100.00
                vendaRepository.save(venda3);

                // --- DESPESAS ---
                expenseRepository.save(Expense.builder().name("Aluguel Loja").valor(new BigDecimal("50.00"))
                                .tipoExpense(TipoExpense.EMPRESARIAL).dataExpense(hojeMeioDia.minusDays(2)).build());
                expenseRepository.save(Expense.builder().name("Material Escritório").valor(new BigDecimal("20.00"))
                                .tipoExpense(TipoExpense.EMPRESARIAL).dataExpense(ontemMeioDia).build());
                expenseRepository.save(Expense.builder().name("Marketing").valor(new BigDecimal("30.00"))
                                .tipoExpense(TipoExpense.INVESTIMENTO).dataExpense(inicioMesPassado.plusDays(10))
                                .build());
        }

        @Test
        @DisplayName("getDashboardSummary: Deve calcular corretamente para período HOJE")
        void getDashboardSummary_periodoHoje_deveRetornarValoresCorretos() {
                // Período: Apenas HOJE
                ZonedDateTime inicioHoje = LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault());
                ZonedDateTime fimHoje = LocalDate.now().atTime(LocalTime.MAX).atZone(java.time.ZoneId.systemDefault());

                DashboardSummaryDTO summary = dashboardService.getDashboardSummary(inicioHoje, fimHoje);

                assertNotNull(summary);
                // Vendas Bruto: Venda 1 (250.00)
                assertEquals(0, new BigDecimal("250.00").compareTo(summary.totalVendasBruto()));
                // Quantidade de Vendas: 1 (Venda 1)
                assertEquals(1L, summary.quantidadeVendas());
                // Ticket Médio: 250.00 / 1 = 250.00
                assertEquals(0, new BigDecimal("250.00").compareTo(summary.ticketMedio()));
                // Expenses: Nenhuma hoje
                assertEquals(0, BigDecimal.ZERO.compareTo(summary.totalExpenses()));
                // Lucro: 250.00 - 0 = 250.00
                assertEquals(0, new BigDecimal("250.00").compareTo(summary.lucroBrutoEstimado()));
                // Vendas por Forma de Pagamento
                Map<FormaPagamento, BigDecimal> vendasPorForma = summary.totalVendasPorFormaPagamento();
                assertEquals(0, new BigDecimal("250.00").compareTo(vendasPorForma.get(FormaPagamento.PIX)));
                assertEquals(0, BigDecimal.ZERO.compareTo(vendasPorForma.get(FormaPagamento.FIADO)));
                assertEquals(0, BigDecimal.ZERO.compareTo(vendasPorForma.get(FormaPagamento.DINHEIRO)));
                // Product Mais Vendido: Product A (2 unidades)
                assertNotNull(summary.productMaisVendido());
                assertEquals(productA.getId(), summary.productMaisVendido().productId());
                assertEquals(0, new BigDecimal("2.000")
                                .compareTo(summary.productMaisVendido().quantidadeTotalVendida()));
                // Gráfico Vendas Diárias: 1 ponto para hoje com valor 250.00
                assertFalse(summary.graficoVendasDiarias().isEmpty());
                assertEquals(1, summary.graficoVendasDiarias().size());
                DataPointDTO pontoHoje = summary.graficoVendasDiarias().get(0);
                assertEquals(LocalDate.now(), pontoHoje.data());
                assertEquals(0, new BigDecimal("250.00").compareTo(pontoHoje.valor()));
        }

        @Test
        @DisplayName("getDashboardSummary: Deve calcular corretamente para período ONTEM")
        void getDashboardSummary_periodoOntem_deveRetornarValoresCorretos() {
                ZonedDateTime inicioOntem = LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime fimOntem = LocalDate.now().minusDays(1).atTime(LocalTime.MAX)
                                .atZone(ZoneId.systemDefault());

                DashboardSummaryDTO summary = dashboardService.getDashboardSummary(inicioOntem, fimOntem);

                assertNotNull(summary);
                // Vendas Bruto: Venda 2 (150.00)
                assertEquals(0, new BigDecimal("150.00").compareTo(summary.totalVendasBruto()));
                // Quantidade de Vendas: 1 (Venda 2)
                assertEquals(1L, summary.quantidadeVendas());
                // Ticket Médio: 150.00
                assertEquals(0, new BigDecimal("150.00").compareTo(summary.ticketMedio()));
                // Expenses: Material Escritório (20.00)
                assertEquals(0, new BigDecimal("20.00").compareTo(summary.totalExpenses()));
                // Lucro: 150.00 - 20.00 = 130.00
                assertEquals(0, new BigDecimal("130.00").compareTo(summary.lucroBrutoEstimado()));
                // Vendas por Forma de Pagamento
                Map<FormaPagamento, BigDecimal> vendasPorForma = summary.totalVendasPorFormaPagamento();
                assertEquals(0, BigDecimal.ZERO.compareTo(vendasPorForma.get(FormaPagamento.PIX)));
                assertEquals(0, new BigDecimal("150.00").compareTo(vendasPorForma.get(FormaPagamento.FIADO)));
                // Product Mais Vendido: Product B (3 unidades)
                assertNotNull(summary.productMaisVendido());
                assertEquals(productB.getId(), summary.productMaisVendido().productId());
                assertEquals(0, new BigDecimal("3.000")
                                .compareTo(summary.productMaisVendido().quantidadeTotalVendida()));
                // Gráfico Vendas Diárias
                assertEquals(1, summary.graficoVendasDiarias().size());
                assertEquals(LocalDate.now().minusDays(1), summary.graficoVendasDiarias().get(0).data());
        }

        @Test
        @DisplayName("getDashboardSummary: Deve calcular corretamente para TODO O PERÍODO (Mês Passado até Hoje)")
        void getDashboardSummary_todoPeriodo_deveRetornarValoresGlobais() {
                // Abrange todas as vendas e expenses criadas
                ZonedDateTime inicioGlobal = inicioMesPassado.minusDays(1); // Um pouco antes do início do mês passado
                ZonedDateTime fimGlobal = hojeMeioDia.plusDays(1); // Um pouco depois de hoje

                DashboardSummaryDTO summary = dashboardService.getDashboardSummary(inicioGlobal, fimGlobal);

                assertNotNull(summary);
                // Total Vendas Bruto: 250 (V1) + 150 (V2) + 100 (V3) = 500.00
                assertEquals(0, new BigDecimal("500.00").compareTo(summary.totalVendasBruto()));
                // Quantidade de Vendas: 3
                assertEquals(3L, summary.quantidadeVendas());
                // Ticket Médio: 500.00 / 3 = 166.67 (arredondado)
                assertEquals(0, new BigDecimal("166.67").compareTo(summary.ticketMedio()));
                // Total Expenses: 50 (Aluguel) + 20 (Material) + 30 (Marketing) = 100.00
                assertEquals(0, new BigDecimal("100.00").compareTo(summary.totalExpenses()));
                // Lucro Bruto: 500.00 - 100.00 = 400.00
                assertEquals(0, new BigDecimal("400.00").compareTo(summary.lucroBrutoEstimado()));

                // Vendas por Forma de Pagamento
                Map<FormaPagamento, BigDecimal> vendasPorForma = summary.totalVendasPorFormaPagamento();
                assertEquals(0, new BigDecimal("250.00").compareTo(vendasPorForma.get(FormaPagamento.PIX)));
                assertEquals(0, new BigDecimal("150.00").compareTo(vendasPorForma.get(FormaPagamento.FIADO)));
                assertEquals(0, new BigDecimal("100.00").compareTo(vendasPorForma.get(FormaPagamento.DINHEIRO)));

                // Product Mais Vendido: Product B (1 de V1 + 3 de V2 = 4 unidades)
                // Product C (5 de V3), Product A (2 de V1) -> Product C é o mais vendido
                assertNotNull(summary.productMaisVendido());
                assertEquals(productC.getId(), summary.productMaisVendido().productId());
                assertEquals(0, new BigDecimal("5.000")
                                .compareTo(summary.productMaisVendido().quantidadeTotalVendida()));

                // Gráfico Vendas Diárias: 3 pontos distintos
                assertEquals(3, summary.graficoVendasDiarias().size());
        }

        @Test
        @DisplayName("getDashboardSummary: Deve retornar zeros e nulos quando não há dados no período")
        void getDashboardSummary_periodoVazio_deveRetornarZerosENulos() {
                ZonedDateTime inicioMuitoFuturo = LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime fimMuitoFuturo = LocalDate.now().plusYears(1).plusDays(1).atTime(LocalTime.MAX)
                                .atZone(ZoneId.systemDefault());

                DashboardSummaryDTO summary = dashboardService.getDashboardSummary(inicioMuitoFuturo, fimMuitoFuturo);

                assertNotNull(summary);
                assertEquals(0, BigDecimal.ZERO.compareTo(summary.totalVendasBruto()));
                assertEquals(0L, summary.quantidadeVendas());
                assertEquals(0, BigDecimal.ZERO.compareTo(summary.ticketMedio()));
                assertEquals(0, BigDecimal.ZERO.compareTo(summary.totalExpenses()));
                assertEquals(0, BigDecimal.ZERO.compareTo(summary.lucroBrutoEstimado()));
                assertNull(summary.productMaisVendido()); // Espera-se nulo
                assertTrue(summary.graficoVendasDiarias().isEmpty());
                // Verifica se todas as formas de pagamento estão zeradas
                summary.totalVendasPorFormaPagamento().forEach((forma, valor) -> {
                        assertEquals(0, BigDecimal.ZERO.compareTo(valor), "Valor para " + forma + " deveria ser zero.");
                });
        }
}