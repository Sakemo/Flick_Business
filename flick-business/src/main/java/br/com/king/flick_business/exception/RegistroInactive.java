package br.com.king.flick_business.exception;

public class RegistroInactive extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public RegistroInactive(String message) {
    super(message);
  }

  public RegistroInactive(String message, Throwable cause) {
    super(message, cause);
  }
}