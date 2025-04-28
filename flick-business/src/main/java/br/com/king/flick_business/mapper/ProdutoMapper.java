package br.com.king.flick_business.mapper;

import br.com.king.flick_business.dto.*;
import br.com.king.flick_business.entity.Categoria;
import br.com.king.flick_business.entity.Fornecedor;
import br.com.king.flick_business.entity.Produto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProdutoMapper {
  public Produto toEntity(ProdutoRequestDTO requestDTO, Categoria categoria, Fornecedor fornecedor) {
    return Produto.builder()
        .nome(requestDTO.nome())
        .descricao(requestDTO.descricao())
        .codigoBarras(requestDTO.codigoBarras())
        .ativo(requestDTO.ativo())
        .precoVenda(requestDTO.precoVenda())
        .precoCustoUnitario(requestDTO.precoCustoUnitario())
        .quantidadeEstoque(requestDTO.quantidadeEstoque())
        .tipoUnidadeVenda(requestDTO.tipoUnidadeVenda())
        .categoria(categoria)
        .fornecedor(fornecedor)
        .build();
  }

  public void updateEntityFromDTO(ProdutoRequestDTO requestDTO, Produto produtoExtistente, Categoria categoria,
      Fornecedor fornecedor) {
    produtoExtistente.setNome(requestDTO.nome());
    produtoExtistente.setDescricao(requestDTO.descricao());
    produtoExtistente.setCodigoBarras(requestDTO.codigoBarras());
    produtoExtistente.setAtivo(requestDTO.ativo());
    produtoExtistente.setPrecoVenda(requestDTO.precoVenda());
    produtoExtistente.setPrecoCustoUnitario(requestDTO.precoCustoUnitario());
    produtoExtistente.setQuantidadeEstoque(requestDTO.quantidadeEstoque());
    produtoExtistente.setTipoUnidadeVenda(requestDTO.tipoUnidadeVenda());
    produtoExtistente.setCategoria(categoria);
    produtoExtistente.setFornecedor(fornecedor);
  }

  public ProdutoResponseDTO toResponseDTO(Produto produto) {
    return new ProdutoResponseDTO(produto);
  }

  public List<ProdutoResponseDTO> toResponseDTOList(List<Produto> produtos) {
    return produtos.stream()
        .map(this::toResponseDTO)
        .collect(Collectors.toList());
  }
}