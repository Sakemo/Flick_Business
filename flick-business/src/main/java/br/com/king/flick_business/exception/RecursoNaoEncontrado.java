package br.com.king.flick_business.exception;

public class RecursoNaoEncontrado extends RuntimeException {

    public RecursoNaoEncontrado(String mensagem) {
        super(mensagem);
    }
}

/*
 * TODO:
 * - [x] Criar Exceptions
 * - [ ] Exceptions genéricas (e.g., NotFoundException, BusinessException)
 * - [ ] Exceptions específicas do domínio
 */