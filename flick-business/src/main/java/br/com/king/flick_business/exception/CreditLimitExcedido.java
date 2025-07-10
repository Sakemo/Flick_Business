package br.com.king.flick_business.exception;

public class CreditLimitExcedido extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public CreditLimitExcedido() {
    super("O limite de fiado foi excedido.");
  }

  public CreditLimitExcedido(String message) {
    super(message);
  }

  public CreditLimitExcedido(String message, Throwable cause) {
    super(message, cause);
  }

  public CreditLimitExcedido(Throwable cause) {
    super(cause);
  }
}