package com.daniel_montilla.reto_tecnico.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class PaymentFailedException extends Exception {
  public PaymentFailedException() {
    super("Payment failed.");
  }
}