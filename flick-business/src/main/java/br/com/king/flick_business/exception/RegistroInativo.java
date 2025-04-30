package br.com.king.flick_business.exception;

public class RegistroInativo extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public RegistroInativo(String message) {
    super(message);
  }

  public RegistroInativo(String message, Throwable cause) {
    super(message, cause);
  }
}