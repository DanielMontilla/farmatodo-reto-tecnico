package com.daniel_montilla.reto_tecnico.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class TokenizationRejectedException extends RuntimeException {
  public TokenizationRejectedException() {
    super("Tokenization rejected due to risk assessment.");
  }
}