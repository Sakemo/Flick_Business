package br.com.king.flick_business.exception;

public class LimiteFiadoExcedido extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public LimiteFiadoExcedido() {
    super("O limite de fiado foi excedido.");
  }

  public LimiteFiadoExcedido(String message) {
    super(message);
  }

  public LimiteFiadoExcedido(String message, Throwable cause) {
    super(message, cause);
  }

  public LimiteFiadoExcedido(Throwable cause) {
    super(cause);
  }
}