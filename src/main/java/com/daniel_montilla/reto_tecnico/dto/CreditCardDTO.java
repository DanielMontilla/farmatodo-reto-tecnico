package com.daniel_montilla.reto_tecnico.dto;

import org.hibernate.validator.constraints.CreditCardNumber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class CreditCardDTO {
  @Data
  public static class CreditCardDetailsRequest {
    @NotBlank(message = "Card number cannot be blank")
    @CreditCardNumber(message = "Please provide a valid credit card number")
    private String cardNumber;

    @NotBlank(message = "Expiration date cannot be blank")
    @Pattern(regexp = "^(0[1-9]|1[0-2])\\/([0-9]{2})$", message = "Expiration date must be in MM/YY format")
    private String expirationDate;

    @NotBlank(message = "CVV cannot be blank")
    @Size(min = 3, max = 4, message = "CVV must be 3 or 4 digits")
    private String cvv;

    @NotBlank(message = "Cardholder name cannot be blank")
    private String cardHolderName;
  }
}
