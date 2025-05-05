package br.com.king.flick_business.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.king.flick_business.dto.DespesaRequestDTO;
import br.com.king.flick_business.dto.DespesaResponseDTO;
import br.com.king.flick_business.repository.DespesaRepository;
import br.com.king.flick_business.entity.Despesa;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DespesaService {
  private final DespesaRepository despesaRepository;

  public DespesaService(DespesaRepository despesaRepository) {
    this.despesaRepository = despesaRepository;
  }

  // Salvar despesa
  @Transactional
  public DespesaResponseDTO salvarDespesa(DespesaRequestDTO dto) {
    Despesa despesa = mapDtoToEntity(dto);

    Despesa despesaSalva = despesaRepository.save(despesa);
    return new DespesaResponseDTO(despesaSalva);
  }

  // Atualizar despesa
  @Transactional
  public DespesaResponseDTO atualizarDespesa(Long id, DespesaRequestDTO dto) {
    Despesa despesaExistente = despesaRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Despesa não encontrada com o ID: " + id));

    updateEntityFromDto(dto, despesaExistente);
    Despesa despesaAtualizada = despesaRepository.save(despesaExistente);

    return new DespesaResponseDTO(despesaAtualizada);
  }

  // Listar despesas
  @Transactional
  public List<DespesaResponseDTO> listarDespesas(LocalDateTime inicio, LocalDateTime fim) {
    List<Despesa> despesas;
    if (inicio != null && fim != null) {
      despesas = despesaRepository.findByDataDespesaBetweenOrderByDataDespesaDesc(inicio, fim);
    } else {
      // Se não foram passados os parâmetros de data, busca as despesas do dia de hoje
      LocalDateTime inicioHoje = LocalDateTime.now().toLocalDate().atStartOfDay();
      LocalDateTime fimHoje = inicioHoje.plusDays(1).minusNanos(1);
      despesas = despesaRepository.findByDataDespesaBetweenOrderByDataDespesaDesc(inicioHoje, fimHoje);
    }

    return despesas.stream().map(DespesaResponseDTO::new).collect(Collectors.toList());
  }

  // Buscar despesa por ID
  @Transactional(readOnly = true)
  public DespesaResponseDTO buscarDespesaPorId(Long id) {
    Despesa despesa = despesaRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Despesa não encontrada com o ID: " + id));

    return new DespesaResponseDTO(despesa);
  }

  // Deletar despesa
  @Transactional
  public void deletarDespesa(Long id) {
    Despesa despesa = despesaRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Despesa não encontrada com o ID: " + id));
    despesaRepository.delete(despesa);
  }

  // MAPPER //
  private Despesa mapDtoToEntity(DespesaRequestDTO dto) {
    return Despesa.builder()
        .nome(dto.nome())
        .valor(dto.valor())
        .dataDespesa(dto.dataDespesa())
        .tipoDespesa(dto.tipoDespesa())
        .build();
  }

  private void updateEntityFromDto(DespesaRequestDTO dto, Despesa entity) {
    entity.setNome(dto.nome());
    entity.setValor(dto.valor());
    entity.setDataDespesa(dto.dataDespesa());
    entity.setTipoDespesa(dto.tipoDespesa());
  }
}