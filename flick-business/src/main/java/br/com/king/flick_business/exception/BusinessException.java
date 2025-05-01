package br.com.king.flick_business.exception;

public class BusinessException extends RuntimeException {

  public BusinessException(String mensagem) {
    super(mensagem);
  }
}