package com.daniel_montilla.reto_tecnico.service;

import com.daniel_montilla.reto_tecnico.exception.TokenizationRejectedException;
import com.daniel_montilla.reto_tecnico.exception.TokenGenerationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class TokenizationService {

  @Value("${tokenization.rejection.probability}")
  private double rejectionProbability;

  @Value("${tokenization.hmac.secret-key}")
  private String hmacSecretKey;

  private static final String HMAC_ALGORITHM = "HmacSHA256";

  public String tokenize(String str) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(hmacSecretKey.getBytes(), HMAC_ALGORITHM));
      byte[] hmac = mac.doFinal(str.getBytes());
      return Base64.getEncoder().encodeToString(hmac);
    } catch (Exception e) {
      throw new TokenGenerationException("Failed to generate HMAC token", e);
    }
  }

  public String tokenizeCard(String creditCardNumeber, String expirationDate, String cvv, String creditCardHolderName) {

    if (Math.random() < rejectionProbability) {
      throw new TokenizationRejectedException();
    }

    return this.tokenize(creditCardNumeber + ":" + cvv + ":" + expirationDate + ":" + creditCardHolderName);
  }
}
