package br.com.king.flick_business.dto;

import br.com.king.flick_business.enums.TipoPessoa;

public record ProviderDTO(Long id, String name, TipoPessoa tipoPessoa, String cnpjCpf, String telefone, String email,
                String notas) {
}