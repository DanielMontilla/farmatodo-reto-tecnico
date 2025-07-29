package com.daniel_montilla.reto_tecnico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.CreditCardNumber;

@Data
public class CreditCardRequest {

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

  /**
   * Creates a consistent, ordered string representation of the card data.
   * The order of fields is crucial for generating a deterministic hash.
   * 
   * @return A canonical string for hashing.
   */
  public String toCanonicalString() {
    return this.cardNumber + ":" + this.cvv + ":" + this.expirationDate + ":" + this.cardHolderName;
  }
}