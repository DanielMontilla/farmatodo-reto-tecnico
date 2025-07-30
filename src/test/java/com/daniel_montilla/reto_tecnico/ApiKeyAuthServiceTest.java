package com.daniel_montilla.reto_tecnico;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.daniel_montilla.reto_tecnico.service.ApiKeyAuthService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApiKeyAuthServiceTest {

  @Autowired
  private ApiKeyAuthService apiKeyAuthService;

  @Test
  void isApiKeyValid_WithValidKey_ShouldReturnTrue() {
    // Arrange
    String validKey = "a-real-api-key";

    // Act
    boolean result = apiKeyAuthService.isApiKeyValid(validKey);

    // Assert
    assertTrue(result, "A valid, non-blank API key should return true.");
  }

  @Test
  void isApiKeyValid_WithNullKey_ShouldReturnFalse() {
    // Arrange
    String nullKey = null;

    // Act
    boolean result = apiKeyAuthService.isApiKeyValid(nullKey);

    // Assert
    assertFalse(result, "A null API key should return false.");
  }

  @Test
  void isApiKeyValid_WithEmptyKey_ShouldReturnFalse() {
    // Arrange
    String emptyKey = "";

    // Act
    boolean result = apiKeyAuthService.isApiKeyValid(emptyKey);

    // Assert
    assertFalse(result, "An empty API key should return false.");
  }

  @Test
  void isApiKeyValid_WithBlankKey_ShouldReturnFalse() {
    // Arrange
    String blankKey = "   ";

    // Act
    boolean result = apiKeyAuthService.isApiKeyValid(blankKey);

    // Assert
    assertFalse(result, "A blank (whitespace) API key should return false.");
  }
}
