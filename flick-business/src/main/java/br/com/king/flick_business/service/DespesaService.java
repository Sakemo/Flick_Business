package br.com.king.flick_business.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.king.flick_business.dto.DespesaRequestDTO;
import br.com.king.flick_business.dto.DespesaResponseDTO;
import br.com.king.flick_business.entity.Despesa;
import br.com.king.flick_business.enums.TipoDespesa;
import br.com.king.flick_business.exception.BusinessException;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.repository.DespesaRepository;

@Service
public class DespesaService {
  // Injeção do repositório de despesas
  private final DespesaRepository despesaRepository;

  // Construtor para injeção de dependência
  public DespesaService(DespesaRepository despesaRepository) {
    this.despesaRepository = despesaRepository;
  }

  /**
   * Salva uma nova despesa no banco de dados.
   * 
   * @param dto DTO com os dados da despesa a ser salva
   * @return DTO de resposta com os dados da despesa salva
   */
  @Transactional
  public DespesaResponseDTO salvarDespesa(DespesaRequestDTO dto) {
    System.out.println("LOG: DespesaService.salvarDespesa - Iniciando salvamento de despesa");
    Despesa despesa = mapDtoToEntity(dto);

    Despesa despesaSalva = despesaRepository.save(despesa);
    System.out.println("LOG: DespesaService.salvarDespesa - Despesa salva com ID: " + despesaSalva.getId());
    return new DespesaResponseDTO(despesaSalva);
  }

  /**
   * Atualiza uma despesa existente.
   * 
   * @param id  ID da despesa a ser atualizada
   * @param dto DTO com os novos dados
   * @return DTO de resposta com os dados atualizados
   */
  @Transactional
  public DespesaResponseDTO atualizarDespesa(Long id, DespesaRequestDTO dto) {
    System.out.println("LOG: DespesaService.atualizarDespesa - Buscando despesa para atualizar, ID: " + id);
    Despesa despesaExistente = despesaRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Despesa não encontrada com o ID: " + id));

    updateEntityFromDto(dto, despesaExistente);
    Despesa despesaAtualizada = despesaRepository.save(despesaExistente);
    System.out.println("LOG: DespesaService.atualizarDespesa - Despesa atualizada com sucesso, ID: " + id);

    return new DespesaResponseDTO(despesaAtualizada);
  }

  /**
   * Lista despesas com filtros opcionais de data e tipo.
   * 
   * @param inicio            Data/hora inicial do filtro
   * @param fim               Data/hora final do filtro
   * @param tipoDespesaString Tipo da despesa (opcional)
   * @return Lista de DTOs de resposta das despesas filtradas
   */
  @Transactional(readOnly = true)
  public List<DespesaResponseDTO> listarDespesas(LocalDateTime inicio, LocalDateTime fim, String tipoDespesaString) {
    System.out.println("LOG: DespesaService.listarDespesas - Iniciando listagem de despesas");
    List<Despesa> despesas;
    TipoDespesa tipoFiltro = null;

    // Validação do tipo de despesa, se informado
    if (tipoDespesaString != null && !tipoDespesaString.isEmpty()) {
      try {
        tipoFiltro = TipoDespesa.valueOf(tipoDespesaString.toUpperCase());
      } catch (IllegalArgumentException e) {
        System.err.println(
            "LOG: DespesaService.listarDespesas - Tipo de despesa inválido recebido no filtro: " + tipoDespesaString);
        throw new BusinessException("Tipo de despesa inválido: " + tipoDespesaString);
      }
    }

    // Filtros combinados de data e tipo
    if (inicio != null && fim != null) {
      if (tipoFiltro != null) {
        System.out.println("LOG: DespesaService.listarDespesas - Filtrando por TIPO e DATA");
        despesas = despesaRepository.findByTipoDespesaAndDataDespesaBetweenOrderByDataDespesaDesc(tipoFiltro, inicio,
            fim);
      } else {
        System.out.println("LOG: DespesaService.listarDespesas - Filtrando apenas por DATA");
        despesas = despesaRepository.findByDataDespesaBetweenOrderByDataDespesaDesc(inicio, fim);
      }
    } else if (tipoFiltro != null) {
      System.out.println("LOG: DespesaService.listarDespesas - Filtrando apenas por TIPO");
      despesas = despesaRepository.findByTipoDespesa(tipoFiltro);
    } else {
      System.out.println("LOG: DespesaService.listarDespesas - Filtrando por DATA DE HOJE (default)");
      LocalDateTime inicioHoje = LocalDateTime.now().toLocalDate().atStartOfDay();
      LocalDateTime fimHoje = inicioHoje.plusDays(1).minusNanos(1);
      despesas = despesaRepository.findByDataDespesaBetweenOrderByDataDespesaDesc(inicioHoje, fimHoje);
    }
    System.out.println("LOG: DespesaService.listarDespesas - Total de despesas encontradas: " + despesas.size());
    return despesas.stream().map(DespesaResponseDTO::new).collect(Collectors.toList());
  }

  /**
   * Busca uma despesa pelo ID.
   * 
   * @param id ID da despesa
   * @return DTO de resposta da despesa encontrada
   */
  @Transactional(readOnly = true)
  public DespesaResponseDTO buscarDespesaPorId(Long id) {
    System.out.println("LOG: DespesaService.buscarDespesaPorId - Buscando despesa por ID: " + id);
    Despesa despesa = despesaRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Despesa não encontrada com o ID: " + id));

    return new DespesaResponseDTO(despesa);
  }

  /**
   * Deleta uma despesa pelo ID.
   * 
   * @param id ID da despesa a ser deletada
   */
  @Transactional
  public void deletarDespesa(Long id) {
    System.out.println("LOG: DespesaService.deletarDespesa - Buscando despesa para deletar, ID: " + id);
    Despesa despesa = despesaRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Despesa não encontrada com o ID: " + id));
    despesaRepository.delete(despesa);
    System.out.println("LOG: DespesaService.deletarDespesa - Despesa deletada com sucesso, ID: " + id);
  }

  // =======================
  // Métodos auxiliares (MAPPER)
  // =======================

  /**
   * Converte um DTO de requisição em uma entidade Despesa.
   * 
   * @param dto DTO de requisição
   * @return Entidade Despesa
   */
  private Despesa mapDtoToEntity(DespesaRequestDTO dto) {
    System.out.println("LOG: DespesaService.mapDtoToEntity - Convertendo DTO para entidade");
    return Despesa.builder()
        .nome(dto.nome())
        .valor(dto.valor())
        .dataDespesa(dto.dataDespesa())
        .tipoDespesa(dto.tipoDespesa())
        .build();
  }

  /**
   * Atualiza uma entidade Despesa com dados de um DTO.
   * 
   * @param dto    DTO de requisição
   * @param entity Entidade a ser atualizada
   */
  private void updateEntityFromDto(DespesaRequestDTO dto, Despesa entity) {
    System.out.println("LOG: DespesaService.updateEntityFromDto - Atualizando entidade com dados do DTO");
    entity.setNome(dto.nome());
    entity.setValor(dto.valor());
    entity.setDataDespesa(dto.dataDespesa());
    entity.setTipoDespesa(dto.tipoDespesa());
  }
}