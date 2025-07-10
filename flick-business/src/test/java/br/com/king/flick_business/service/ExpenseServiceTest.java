package br.com.king.flick_business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import br.com.king.flick_business.dto.request.ExpenseRequestDTO;
import br.com.king.flick_business.dto.response.ExpenseResponseDTO;
import br.com.king.flick_business.entity.Expense;
import br.com.king.flick_business.enums.TipoExpense;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.repository.ExpenseRepository;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

  @Mock
  private ExpenseRepository expenseRepository;

  @InjectMocks
  private ExpenseService expenseService;

  @Captor
  private ArgumentCaptor<Expense> expenseCaptor;

  @Captor
  private ArgumentCaptor<Specification<Expense>> specCaptor;

  private Expense expenseExistente;
  private ExpenseRequestDTO expenseRequestDTO;
  private final Long idExistente = 1L;
  private final Long idInexistente = 99L;
  private ZonedDateTime dataRef;

  @BeforeEach
  void setUp() {
    dataRef = ZonedDateTime.now().with(LocalTime.NOON);

    expenseExistente = Expense.builder()
        .id(idExistente)
        .name("Almoço Reunião")
        .valor(new BigDecimal("75.50"))
        .dataExpense(dataRef.minusDays(1))
        .tipoExpense(TipoExpense.EMPRESARIAL)
        .build();

    // CORREÇÃO: O DTO de requisição agora usa ZonedDateTime diretamente.
    expenseRequestDTO = new ExpenseRequestDTO(
        "Café da Manhã",
        new BigDecimal("15.80"),
        dataRef, // Passando o objeto ZonedDateTime
        TipoExpense.PESSOAL,
        "Observação café");
  }

  @Test
  @DisplayName("Deve salvar expense com dados válidos e retornar DTO")
  void salvarExpense_comDadosValidos_deveRetornarDtoSalvo() {
    when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
      Expense d = invocation.getArgument(0);
      d.setId(2L);
      return d;
    });

    ExpenseResponseDTO resultado = expenseService.salvarExpense(expenseRequestDTO);

    assertNotNull(resultado);
    assertEquals(2L, resultado.id());
    assertEquals("Café da Manhã", resultado.name());

    // A comparação agora é direta, ZonedDateTime com ZonedDateTime.
    assertEquals(expenseRequestDTO.dataExpense(), resultado.dataExpense());

    verify(expenseRepository).save(any(Expense.class));
  }

  @Test
  @DisplayName("Deve chamar findAll com Specification e Sort quando listar expenses")
  void listarExpenses_comFiltros_deveChamarFindAllComSpecESort() {
    String nomeFiltro = "almoço";
    ZonedDateTime inicioFiltro = dataRef.minusDays(5);
    ZonedDateTime fimFiltro = dataRef.plusDays(5);
    String tipoFiltroString = "EMPRESARIAL";

    when(expenseRepository.findAll(any(Specification.class), any(Sort.class)))
        .thenReturn(List.of(expenseExistente));

    // A chamada ao serviço agora está correta, pois a assinatura foi corrigida.
    List<ExpenseResponseDTO> resultado = expenseService.listExpenses(inicioFiltro, fimFiltro, tipoFiltroString,
        nomeFiltro);

    assertEquals(1, resultado.size());
    assertEquals("Almoço Reunião", resultado.get(0).name());

    verify(expenseRepository).findAll(specCaptor.capture(), any(Sort.class));
    Specification<Expense> capturedSpec = specCaptor.getValue();
    assertNotNull(capturedSpec);
  }

  @Test
  @DisplayName("Deve delete expense quando ID existe")
  void deleteExpense_quandoIdExiste_deveChamarDeleteDoRepositorio() {
    when(expenseRepository.findById(idExistente)).thenReturn(Optional.of(expenseExistente));
    doNothing().when(expenseRepository).delete(expenseExistente);

    expenseService.deleteExpense(idExistente);

    verify(expenseRepository).findById(idExistente);
    verify(expenseRepository).delete(expenseExistente);
  }

  @Test
  @DisplayName("Deve lançar RecursoNaoEncontrado ao tentar delete ID inexistente")
  void deleteExpense_quandoIdNaoExiste_deveLancarExcecao() {
    when(expenseRepository.findById(idInexistente)).thenReturn(Optional.empty());

    assertThrows(RecursoNaoEncontrado.class, () -> {
      expenseService.deleteExpense(idInexistente);
    });

    // A correção da ambiguidade permanece.
    verify(expenseRepository, never()).delete(any(Expense.class));
  }
}