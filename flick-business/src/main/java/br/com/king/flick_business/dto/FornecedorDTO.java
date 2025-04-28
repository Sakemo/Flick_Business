package br.com.king.flick_business.dto;

import br.com.king.flick_business.enums.TipoPessoa;

public record FornecedorDTO(Long id, String nome, TipoPessoa tipoPessoa, String cnpjCpf, String telefone, String email, String notas) {}