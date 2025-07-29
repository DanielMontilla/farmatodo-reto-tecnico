package com.daniel_montilla.reto_tecnico.service;

import org.springframework.stereotype.Service;

@Service
public class ApiKeyAuthService {

  /**
   * Placeholder method to validate an API key.
   * In a real application, this would check against a database.
   * 
   * @param apiKey The API key from the request header.
   * @return true if the key is valid, false otherwise.
   */
  public boolean isApiKeyValid(String apiKey) {
    return apiKey != null && !apiKey.isBlank();
  }
}