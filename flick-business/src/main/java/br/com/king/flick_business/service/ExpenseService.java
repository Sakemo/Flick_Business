package br.com.king.flick_business.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.king.flick_business.dto.request.ExpenseRequestDTO;
import br.com.king.flick_business.dto.response.ExpenseResponseDTO;
import br.com.king.flick_business.entity.Expense;
import br.com.king.flick_business.enums.TipoExpense;
import br.com.king.flick_business.exception.BusinessException;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.repository.ExpenseRepository;
import br.com.king.flick_business.repository.spec.ExpenseSpecification;

@Service
public class ExpenseService {
  // Injeção do repositório de expenses
  private final ExpenseRepository expenseRepository;

  // Construtor para injeção de dependência
  public ExpenseService(ExpenseRepository expenseRepository) {
    this.expenseRepository = expenseRepository;
  }

  /**
   * Salva uma nova expense no banco de dados.
   * 
   * @param dto DTO com os dados da expense a ser salva
   * @return DTO de resposta com os dados da expense salva
   */
  @Transactional
  public ExpenseResponseDTO salvarExpense(ExpenseRequestDTO dto) {
    System.out.println("LOG: ExpenseService.salvarExpense - Iniciando salvamento de expense");
    Expense expense = mapDtoToEntity(dto);

    Expense expenseSalva = expenseRepository.save(expense);
    System.out.println("LOG: ExpenseService.salvarExpense - Expense salva com ID: " + expenseSalva.getId());
    return new ExpenseResponseDTO(expenseSalva);
  }

  /**
   * Atualiza uma expense existente.
   * 
   * @param id  ID da expense a ser atualizada
   * @param dto DTO com os novos dados
   * @return DTO de resposta com os dados atualizados
   */
  @Transactional
  public ExpenseResponseDTO atualizarExpense(Long id, ExpenseRequestDTO dto) {
    System.out.println("LOG: ExpenseService.atualizarExpense - Buscando expense para atualizar, ID: " + id);
    Expense expenseExistente = expenseRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Expense não encontrada com o ID: " + id));

    updateEntityFromDto(dto, expenseExistente);
    Expense expenseAtualizada = expenseRepository.save(expenseExistente);
    System.out.println("LOG: ExpenseService.atualizarExpense - Expense atualizada com sucesso, ID: " + id);

    return new ExpenseResponseDTO(expenseAtualizada);
  }

  /**
   * Lista expenses com filtros opcionais de data e tipo.
   * 
   * @param start             Data/hora inicial do filtro
   * @param end               Data/hora final do filtro
   * @param tipoExpenseString Tipo da expense (opcional)
   * @return Lista de DTOs de resposta das expenses filtradas
   */
  @Transactional(readOnly = true)
  public List<ExpenseResponseDTO> listExpenses(ZonedDateTime start, ZonedDateTime end, String tipoExpenseString,
      String name) {
    System.out.println("LOG: ExpenseService.listExpenses - Iniciando listagem de expenses");

    TipoExpense tipoFilter = null;
    if (tipoExpenseString != null && !tipoExpenseString.isEmpty()) {
      try {
        tipoFilter = TipoExpense.valueOf(tipoExpenseString.toUpperCase());
      } catch (IllegalArgumentException e) {
        System.err.println(
            "LOG: ExpenseService.listExpenses - Tipo de expense inválido recebido no filtro: " + tipoExpenseString);
        throw new BusinessException("Tipo de expense inválido: " + tipoExpenseString);
      }
    }

    Specification<Expense> spec = ExpenseSpecification.withFilter(name, start, end, tipoFilter);

    Sort sort = Sort.by(Sort.Direction.DESC, "dataExpense");

    List<Expense> expenses = expenseRepository.findAll(spec, sort);

    return expenses.stream().map(ExpenseResponseDTO::new).collect(Collectors.toList());
  }

  /**
   * Calcula a soma total das expenses dentro de um período
   * 
   * @param begin Data inicial
   * @param end   Data final
   * @return a soma total
   */
  @Transactional(readOnly = true)
  public BigDecimal calcTotalExpensesPerPeriod(LocalDateTime begin, LocalDateTime end) {
    System.out.println("LOG: ExpenseService.calcTotalExpensesPerPeriod - Calculating...");

    LocalDateTime currentMonthBegin = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
    LocalDateTime currentMonthEnd = currentMonthBegin.plusMonths(1).minusNanos(1);

    LocalDateTime beginQuery = (begin != null) ? begin : currentMonthBegin;
    LocalDateTime endQuery = (end != null) ? end : currentMonthEnd;

    java.time.ZoneId zoneId = java.time.ZoneId.systemDefault();
    BigDecimal value = expenseRepository.sumValorByDataExpenseBetween(
        beginQuery.atZone(zoneId),
        endQuery.atZone(zoneId));

    return value != null ? value : BigDecimal.ZERO;
  }

  /**
   * Busca uma expense pelo ID.
   * 
   * @param id ID da expense
   * @return DTO de resposta da expense encontrada
   */
  @Transactional(readOnly = true)
  public ExpenseResponseDTO buscarExpensePorId(Long id) {
    System.out.println("LOG: ExpenseService.buscarExpensePorId - Buscando expense por ID: " + id);
    Expense expense = expenseRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Expense não encontrada com o ID: " + id));

    return new ExpenseResponseDTO(expense);
  }

  /**
   * Deleta uma expense pelo ID.
   * 
   * @param id ID da expense a ser deletada
   */
  @Transactional
  public void deleteExpense(Long id) {
    System.out.println("LOG: ExpenseService.deleteExpense - Buscando expense para delete, ID: " + id);
    Expense expense = expenseRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Expense não encontrada com o ID: " + id));
    expenseRepository.delete(expense);
    System.out.println("LOG: ExpenseService.deleteExpense - Expense deletada com sucesso, ID: " + id);
  }

  // =======================
  // Métodos auxiliares (MAPPER)
  // =======================

  /**
   * Converte um DTO de requisição em uma entidade Expense.
   * 
   * @param dto DTO de requisição
   * @return Entidade Expense
   */
  private Expense mapDtoToEntity(ExpenseRequestDTO dto) {
    System.out.println("LOG: ExpenseService.mapDtoToEntity - Convertendo DTO para entidade");
    return Expense.builder()
        .name(dto.name())
        .valor(dto.valor())
        .dataExpense(dto.dataExpense())
        .tipoExpense(dto.tipoExpense())
        .build();
  }

  /**
   * Atualiza uma entidade Expense com dados de um DTO.
   * 
   * @param dto    DTO de requisição
   * @param entity Entidade a ser atualizada
   */
  private void updateEntityFromDto(ExpenseRequestDTO dto, Expense entity) {
    System.out.println("LOG: ExpenseService.updateEntityFromDto - Atualizando entidade com dados do DTO");
    entity.setName(dto.name());
    entity.setValor(dto.valor());
    entity.setDataExpense(dto.dataExpense());
    entity.setTipoExpense(dto.tipoExpense());
  }
}