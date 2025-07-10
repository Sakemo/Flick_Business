package br.com.king.flick_business.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.king.flick_business.dto.ProductRequestDTO;
import br.com.king.flick_business.dto.ProductResponseDTO;
import br.com.king.flick_business.entity.Category;
import br.com.king.flick_business.entity.Provider;
import br.com.king.flick_business.entity.Product;
import br.com.king.flick_business.exception.RecursoNaoEncontrado;
import br.com.king.flick_business.mapper.ProductMapper;
import br.com.king.flick_business.repository.ProductRepository;
import br.com.king.flick_business.repository.spec.ProductSpecification;

@Service
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;
  private final CategoryService categoryService;
  private final ProviderService providerService;

  public ProductService(ProductRepository productRepository,
      ProductMapper productMapper,
      CategoryService categoryService,
      ProviderService providerService) {
    this.productRepository = productRepository;
    this.productMapper = productMapper;
    this.categoryService = categoryService;
    this.providerService = providerService;
  }

  @Transactional
  public ProductResponseDTO save(ProductRequestDTO requestDTO) {
    System.out.println("LOG: ProductService.save - categoryId recebido: " + requestDTO.categoryId());
    Category category = categoryService.searchEntityById(requestDTO.categoryId());
    System.out
        .println(
            "LOG: ProductService.save - Category buscada: " + (category != null ? category.getName() : "NULA"));
    Provider provider = null;
    if (requestDTO.providerId() != null) {
      provider = providerService.searchEntityById(requestDTO.providerId());
    }
    Product product = productMapper.toEntity(requestDTO, category, provider);
    System.out.println("LOG: ProductService.save - Product mapeado par save, category do product: "
        + (product.getCategory() != null ? product.getCategory().getName() : "NULA"));
    Product productSalvo = productRepository.save(product);
    System.out.println(
        "LOG: ProductService.save - Product salvo, category do productSalvo: "
            + (productSalvo.getCategory() != null ? productSalvo.getCategory().getName() : "NULA"));
    return productMapper.toResponseDTO(productSalvo);
  }

  @Transactional
  public ProductResponseDTO atualizar(Long id, ProductRequestDTO requestDTO) {
    Product productExistente = productRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Product não encontrado com ID: " + id));
    System.out.println("LOG: ProductService.atualizar - categoryId recebido: " + (requestDTO.categoryId()));
    Category novaCategory = categoryService.searchEntityById(requestDTO.categoryId());
    System.out.println("LOG: ProductService.atualizar - category buscada: "
        + (novaCategory != null ? novaCategory.getName() : "NULA"));
    Provider novoProvider;
    if (requestDTO.providerId() != null) {
      novoProvider = providerService.searchEntityById(requestDTO.providerId());
    } else if (productExistente.getProvider() != null) {
      novoProvider = productExistente.getProvider();
    } else {
      novoProvider = null;
    }
    productMapper.updateEntityFromDTO(requestDTO, productExistente, novaCategory, novoProvider);
    Product productAtualizado = productRepository.save(productExistente);

    System.out.println("LOG: ProductService.atualizar - productMapper: " + (productMapper));
    System.out.println("LOG: ProductService.atualizar - product: " + (productAtualizado));
    return productMapper.toResponseDTO(productAtualizado);
  }

  @Transactional(readOnly = true)
  public List<ProductResponseDTO> listProducts(String name, Long categoryId, String orderBy) {
    if ("maisVendido".equalsIgnoreCase(orderBy) || "menosVendido".equalsIgnoreCase(orderBy)) {
      List<Product> products = productRepository.findWithFiltersAndSortByVendas(name, categoryId);
      return productMapper.toResponseDTOList(products);

    }
    Sort sort = createSort(orderBy);
    Specification<Product> spec = ProductSpecification.withFilter(name, categoryId);
    List<Product> products = productRepository.findAll(spec, sort);
    return productMapper.toResponseDTOList(products);
  }

  private Sort createSort(String orderBy) {
    if (orderBy == null || orderBy.isBlank()) {
      return Sort.by(Sort.Direction.ASC, "name");
    }

    return switch (orderBy) {
      case "nameDesc" -> Sort.by(Sort.Direction.DESC, "name");
      case "nameAsc" -> Sort.by(Sort.Direction.ASC, "name");
      case "maisBarato" -> Sort.by(Sort.Direction.ASC, "salePrice");
      case "maisCaro" -> Sort.by(Sort.Direction.DESC, "salePrice");
      case "maisAntigo" -> Sort.by(Sort.Direction.ASC, "criadoEm");
      case "maisRecente" -> Sort.by(Sort.Direction.DESC, "criadoEm");
      default -> Sort.by(Sort.Direction.ASC, "name");
    };
  }

  @Transactional
  public ProductResponseDTO copiarProduct(Long id) {
    // 1. Encontra o product original
    Product original = productRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Product não encontrado para cópia com ID: " + id));

    // 2. Define o name base para a busca de cópias
    String nameBase = original.getName();
    // Remove um sufixo " - Cópia (n)" se o product que está sendo copiado já for
    // uma cópia
    if (nameBase.matches(".* - Cópia \\(\\d+\\)$")) {
      nameBase = nameBase.substring(0, nameBase.lastIndexOf(" - Cópia"));
    }
    String prefixoBusca = nameBase + " - Cópia";

    // 3. Conta quantas cópias já existem
    List<Product> copiasExistentes = productRepository.findByNameStartingWith(prefixoBusca);
    int proximoNumeroCopia = copiasExistentes.size() + 1;

    // Validação extra para garantir que o name não se repita caso uma cópia
    // intermediária tenha sido deletada
    String novoName;
    boolean nameDisponivel = false;
    while (!nameDisponivel) {
      novoName = String.format("%s - Cópia (%d)", nameBase, proximoNumeroCopia);
      String finalNovoName = novoName; // Variável final para usar na lambda
      if (copiasExistentes.stream().noneMatch(p -> p.getName().equalsIgnoreCase(finalNovoName))) {
        nameDisponivel = true;
      } else {
        proximoNumeroCopia++;
      }
    }
    novoName = String.format("%s - Cópia (%d)", nameBase, proximoNumeroCopia);

    // 4. Cria a nova instância do product
    Product copia = new Product();
    copia.setName(novoName); // Usa o novo name com o contador

    // ... (resto da lógica de copiar atributos permanece a mesma)
    copia.setDescription(original.getDescription());
    copia.setSalePrice(original.getSalePrice());
    copia.setCostPrice(original.getCostPrice());
    copia.setUnitOfSale(original.getUnitOfSale());
    copia.setCategory(original.getCategory());
    copia.setProvider(original.getProvider());
    copia.setActive(false);
    copia.setStockQuantity(BigDecimal.ZERO);
    copia.setBarcode(null);

    // 5. Salva a nova entidade
    Product productCopiado = productRepository.save(copia);

    // 6. Retorna o DTO
    return productMapper.toResponseDTO(productCopiado);
  }

  @Transactional(readOnly = true)
  public ProductResponseDTO searchById(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Product não encontrado com ID: " + id));
    return productMapper.toResponseDTO(product);
  }

  @Transactional
  public void deleteLogicamente(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontrado("Product não encontrado com ID: " + id));
    if (product.isActive()) {
      product.setActive(false);
    } else {
      product.setActive(true);
    }
    productRepository.save(product);
  }

  @Transactional
  public void deleteFisicamente(Long id) {
    if (!productRepository.existsById(id)) {
      throw new RecursoNaoEncontrado("Product não encontrado com ID: " + id + "para deleção física");
      // TODO: Adicionar validações ANTES de delete caso ele esteja associado a uma
      // venda. Considere Desativalo.
    }
    productRepository.deleteById(id);
  }

  // TODO: ProductService.copiarProduct

}
