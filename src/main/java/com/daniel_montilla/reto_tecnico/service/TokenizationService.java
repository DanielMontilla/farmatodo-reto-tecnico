package com.daniel_montilla.reto_tecnico.service;

import com.daniel_montilla.reto_tecnico.dto.CreditCardRequest;

public interface TokenizationService {
  String tokenizeCard(CreditCardRequest creditCardRequest);
}