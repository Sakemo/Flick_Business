package br.com.king.flick_business.exception;

public class PrecoInvalido extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public PrecoInvalido(String mensagem) {
    super(mensagem);
  }

  public PrecoInvalido(String mensagem, Throwable causa) {
    super(mensagem, causa);
  }
}