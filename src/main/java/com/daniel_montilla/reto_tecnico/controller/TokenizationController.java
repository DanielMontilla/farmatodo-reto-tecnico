package com.daniel_montilla.reto_tecnico.controller;

import com.daniel_montilla.reto_tecnico.dto.CreditCardDTO;
import com.daniel_montilla.reto_tecnico.service.TokenizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/tokenize")
public class TokenizationController {

  private final TokenizationService tokenizationService;

  public TokenizationController(TokenizationService tokenizationService) {
    this.tokenizationService = tokenizationService;
  }

  @PostMapping
  public ResponseEntity<String> createCreditCardToken(
      @Valid @RequestBody CreditCardDTO.CreditCardDetailsRequest request) {
    String token = tokenizationService.tokenizeCard(request.getCardNumber(), request.getExpirationDate(),
        request.getCvv(), request.getCardHolderName());
    return ResponseEntity.ok(token);
  }
}