package com.daniel_montilla.reto_tecnico.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter
@Component
public class AesEncryptor implements AttributeConverter<String, String> {

  @Value("${encryption.aes.secret-key}")
  private String secretKey;

  private static final String ALGORITHM = "AES";

  @Override
  public String convertToDatabaseColumn(String attribute) {
    if (attribute == null)
      return null;
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, keySpec);
      byte[] encrypted = cipher.doFinal(attribute.getBytes());
      return Base64.getEncoder().encodeToString(encrypted);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to encrypt attribute", e);
    }
  }

  @Override
  public String convertToEntityAttribute(String dbData) {
    if (dbData == null)
      return null;
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, keySpec);
      byte[] decoded = Base64.getDecoder().decode(dbData);
      byte[] decrypted = cipher.doFinal(decoded);
      return new String(decrypted);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to decrypt attribute", e);
    }
  }
}
