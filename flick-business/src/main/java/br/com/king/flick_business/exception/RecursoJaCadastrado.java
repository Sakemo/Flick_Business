/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package br.com.king.flick_business.exception;

public class RecursoJaCadastrado extends RuntimeException {
  public RecursoJaCadastrado(String message) {
    super(message);
  }

  public RecursoJaCadastrado(String message, Throwable cause) {
    super(message, cause);
  }
}