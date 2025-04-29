/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package br.com.king.flick_business.exception;

public class RecursoNaoDeletavel extends RuntimeException {
  public RecursoNaoDeletavel(String message) {
    super(message);
  }

  public RecursoNaoDeletavel(String message, Throwable cause) {
    super(message, cause);
  }

}
