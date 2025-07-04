package br.com.king.flick_business.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.king.flick_business.dto.ProdutoRequestDTO;
import br.com.king.flick_business.dto.ProdutoResponseDTO;
import br.com.king.flick_business.entity.Categoria;
import br.com.king.flick_business.entity.Fornecedor;
import br.com.king.flick_business.entity.Produto;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.mapper.ProdutoMapper;
import br.com.king.flick_business.repository.ProdutoRepository;
import br.com.king.flick_business.repository.spec.ProdutoSpecification;

@Service
public class ProdutoService {

  private final ProdutoRepository produtoRepository;
  private final ProdutoMapper produtoMapper;
  private final CategoriaService categoriaService;
  private final FornecedorService fornecedorService;

  public ProdutoService(ProdutoRepository produtoRepository,
      ProdutoMapper produtoMapper,
      CategoriaService categoriaService,
      FornecedorService fornecedorService) {
    this.produtoRepository = produtoRepository;
    this.produtoMapper = produtoMapper;
    this.categoriaService = categoriaService;
    this.fornecedorService = fornecedorService;
  }

  @Transactional
  public ProdutoResponseDTO salvar(ProdutoRequestDTO requestDTO) {
    System.out.println("LOG: ProdutoService.salvar - categoriaId recebido: " + requestDTO.categoriaId());
    Categoria categoria = categoriaService.buscarEntidadePorId(requestDTO.categoriaId());
    System.out
        .println(
            "LOG: ProdutoService.salvar - Categoria buscada: " + (categoria != null ? categoria.getNome() : "NULA"));
    Fornecedor fornecedor = null;
    if (requestDTO.fornecedorId() != null) {
      fornecedor = fornecedorService.buscarEntidadePorId(requestDTO.fornecedorId());
    }
    Produto produto = produtoMapper.toEntity(requestDTO, categoria, fornecedor);
    System.out.println("LOG: ProdutoService.salvar - Produto mapeado par salvar, categoria do produto: "
        + (produto.getCategoria() != null ? produto.getCategoria().getNome() : "NULA"));
    Produto produtoSalvo = produtoRepository.save(produto);
    System.out.println(
        "LOG: ProdutoService.salvar - Produto salvo, categoria do produtoSalvo: "
            + (produtoSalvo.getCategoria() != null ? produtoSalvo.getCategoria().getNome() : "NULA"));
    return produtoMapper.toResponseDTO(produtoSalvo);
  }

  @Transactional
  public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO requestDTO) {
    Produto produtoExistente = produtoRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Produto não encontrado com ID: " + id));
    System.out.println("LOG: ProdutoService.atualizar - categoriaId recebido: " + (requestDTO.categoriaId()));
    Categoria novaCategoria = categoriaService.buscarEntidadePorId(requestDTO.categoriaId());
    System.out.println("LOG: ProdutoService.atualizar - categoria buscada: "
        + (novaCategoria != null ? novaCategoria.getNome() : "NULA"));
    Fornecedor novoFornecedor;
    if (requestDTO.fornecedorId() != null) {
      novoFornecedor = fornecedorService.buscarEntidadePorId(requestDTO.fornecedorId());
    } else if (produtoExistente.getFornecedor() != null) {
      novoFornecedor = produtoExistente.getFornecedor();
    } else {
      novoFornecedor = null;
    }
    produtoMapper.updateEntityFromDTO(requestDTO, produtoExistente, novaCategoria, novoFornecedor);
    Produto produtoAtualizado = produtoRepository.save(produtoExistente);

    System.out.println("LOG: ProdutoService.atualizar - produtoMapper: " + (produtoMapper));
    System.out.println("LOG: ProdutoService.atualizar - produto: " + (produtoAtualizado));
    return produtoMapper.toResponseDTO(produtoAtualizado);
  }

  @Transactional(readOnly = true)
  public List<ProdutoResponseDTO> listProducts(String name, Long categoriaId, String orderBy) {
    if ("maisVendido".equalsIgnoreCase(orderBy) || "menosVendido".equalsIgnoreCase(orderBy)) {
      List<Produto> produtos = produtoRepository.findWithFiltersAndSortByVendas(name, categoriaId);
      return produtoMapper.toResponseDTOList(produtos);

    }
    Sort sort = createSort(orderBy);
    Specification<Produto> spec = ProdutoSpecification.withFilter(name, categoriaId);
    List<Produto> produtos = produtoRepository.findAll(spec, sort);
    return produtoMapper.toResponseDTOList(produtos);
  }

  private Sort createSort(String orderBy) {
    if (orderBy == null || orderBy.isBlank()) {
      return Sort.by(Sort.Direction.ASC, "nome");
    }

    return switch (orderBy) {
      case "nomeDesc" -> Sort.by(Sort.Direction.DESC, "nome");
      case "nomeAsc" -> Sort.by(Sort.Direction.ASC, "nome");
      case "maisBarato" -> Sort.by(Sort.Direction.ASC, "precoVenda");
      case "maisCaro" -> Sort.by(Sort.Direction.DESC, "precoVenda");
      case "maisAntigo" -> Sort.by(Sort.Direction.ASC, "criadoEm");
      case "maisRecente" -> Sort.by(Sort.Direction.DESC, "criadoEm");
      default -> Sort.by(Sort.Direction.ASC, "nome");
    };
  }

  @Transactional
  public ProdutoResponseDTO copiarProduto(Long id) {
    // 1. Encontra o produto original
    Produto original = produtoRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Produto não encontrado para cópia com ID: " + id));

    // 2. Define o nome base para a busca de cópias
    String nomeBase = original.getNome();
    // Remove um sufixo " - Cópia (n)" se o produto que está sendo copiado já for
    // uma cópia
    if (nomeBase.matches(".* - Cópia \\(\\d+\\)$")) {
      nomeBase = nomeBase.substring(0, nomeBase.lastIndexOf(" - Cópia"));
    }
    String prefixoBusca = nomeBase + " - Cópia";

    // 3. Conta quantas cópias já existem
    List<Produto> copiasExistentes = produtoRepository.findByNomeStartingWith(prefixoBusca);
    int proximoNumeroCopia = copiasExistentes.size() + 1;

    // Validação extra para garantir que o nome não se repita caso uma cópia
    // intermediária tenha sido deletada
    String novoNome;
    boolean nomeDisponivel = false;
    while (!nomeDisponivel) {
      novoNome = String.format("%s - Cópia (%d)", nomeBase, proximoNumeroCopia);
      String finalNovoNome = novoNome; // Variável final para usar na lambda
      if (copiasExistentes.stream().noneMatch(p -> p.getNome().equalsIgnoreCase(finalNovoNome))) {
        nomeDisponivel = true;
      } else {
        proximoNumeroCopia++;
      }
    }
    novoNome = String.format("%s - Cópia (%d)", nomeBase, proximoNumeroCopia);

    // 4. Cria a nova instância do produto
    Produto copia = new Produto();
    copia.setNome(novoNome); // Usa o novo nome com o contador

    // ... (resto da lógica de copiar atributos permanece a mesma)
    copia.setDescricao(original.getDescricao());
    copia.setPrecoVenda(original.getPrecoVenda());
    copia.setPrecoCustoUnitario(original.getPrecoCustoUnitario());
    copia.setTipoUnidadeVenda(original.getTipoUnidadeVenda());
    copia.setCategoria(original.getCategoria());
    copia.setFornecedor(original.getFornecedor());
    copia.setAtivo(false);
    copia.setQuantidadeEstoque(BigDecimal.ZERO);
    copia.setCodigoBarras(null);

    // 5. Salva a nova entidade
    Produto produtoCopiado = produtoRepository.save(copia);

    // 6. Retorna o DTO
    return produtoMapper.toResponseDTO(produtoCopiado);
  }

  @Transactional(readOnly = true)
  public ProdutoResponseDTO buscarPorId(Long id) {
    Produto produto = produtoRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Produto não encontrado com ID: " + id));
    return produtoMapper.toResponseDTO(produto);
  }

  @Transactional
  public void deletarLogicamente(Long id) {
    Produto produto = produtoRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Produto não encontrado com ID: " + id));
    if (produto.isAtivo()) {
      produto.setAtivo(false);
    } else {
      produto.setAtivo(true);
    }
    produtoRepository.save(produto);
  }

  @Transactional
  public void deletarFisicamente(Long id) {
    if (!produtoRepository.existsById(id)) {
      throw new RecursoNaoEncontrado("Produto não encontrado com ID: " + id + "para deleção física");
      // TODO: Adicionar validações ANTES de deletar caso ele esteja associado a uma
      // venda. Considere Desativalo.
    }
    produtoRepository.deleteById(id);
  }

  // TODO: ProdutoService.copiarProduto

}
