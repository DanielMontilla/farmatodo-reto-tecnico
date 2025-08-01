package com.daniel_montilla.reto_tecnico.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class TokenGenerationException extends RuntimeException {
  public TokenGenerationException(String message, Throwable cause) {
    super(message, cause);
  }
}