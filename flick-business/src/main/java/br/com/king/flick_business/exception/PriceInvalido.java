package br.com.king.flick_business.exception;

public class PriceInvalido extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public PriceInvalido(String mensagem) {
    super(mensagem);
  }

  public PriceInvalido(String mensagem, Throwable causa) {
    super(mensagem, causa);
  }
}