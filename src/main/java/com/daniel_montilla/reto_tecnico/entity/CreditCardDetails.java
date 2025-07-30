package com.daniel_montilla.reto_tecnico.entity;

import com.daniel_montilla.reto_tecnico.security.AesEncryptor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credit_card_details")
public class CreditCardDetails {
  @Id
  private String token;

  @Convert(converter = AesEncryptor.class)
  @Column(nullable = false)
  private String creditCardNumber;

  @Convert(converter = AesEncryptor.class)
  @Column(nullable = false)
  private String expirationDate;

  @Convert(converter = AesEncryptor.class)
  @Column(nullable = false)
  private String cvv;

  @Convert(converter = AesEncryptor.class)
  @Column(nullable = false)
  private String cardHolderName;
}
