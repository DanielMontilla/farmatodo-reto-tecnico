package com.daniel_montilla.reto_tecnico.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends Exception {
  public NotFoundException(String entity) {
    super(String.format("%s not found.", entity));
  }
}