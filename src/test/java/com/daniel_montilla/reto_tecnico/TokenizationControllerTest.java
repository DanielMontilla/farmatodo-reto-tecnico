package com.daniel_montilla.reto_tecnico;

import com.daniel_montilla.reto_tecnico.dto.CreditCardDTO;
import com.daniel_montilla.reto_tecnico.exception.TokenizationRejectedException;
import com.daniel_montilla.reto_tecnico.service.ApiKeyAuthService;
import com.daniel_montilla.reto_tecnico.service.TokenizationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class TokenizationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private TokenizationService tokenizationService;

  @MockitoBean
  private ApiKeyAuthService apiKeyAuthService;

  private final String MOCK_API_KEY = "test-api-key";
  private CreditCardDTO.CreditCardDetailsRequest validRequest;

  @BeforeEach
  void setUp() {
    // Mock the API key validation to always return true for tests
    when(apiKeyAuthService.isApiKeyValid(anyString())).thenReturn(true);

    // Create a reusable valid request object for the tests
    validRequest = new CreditCardDTO.CreditCardDetailsRequest();
    validRequest.setCardNumber("49927398716");
    validRequest.setExpirationDate("12/25");
    validRequest.setCvv("123");
    validRequest.setCardHolderName("John Doe");
  }

  @Test
  void createCreditCardToken_WhenTokenizationSucceeds_ShouldReturnOkWithToken() throws Exception {
    // Arrange
    String expectedToken = "generated-hmac-token-for-success";
    // Configure the mock service to return a specific token when tokenizeCard is
    // called
    when(tokenizationService.tokenizeCard(validRequest.getCardNumber())).thenReturn(expectedToken);

    // Act & Assert
    mockMvc.perform(post("/tokenize")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedToken));
  }

  @Test
  void createCreditCardToken_WhenTokenizationIsRejected_ShouldReturnUnprocessableEntity() throws Exception {
    // Arrange
    // Configure the mock service to throw the rejection exception when tokenizeCard
    // is called
    when(tokenizationService.tokenizeCard(validRequest.getCardNumber()))
        .thenThrow(new TokenizationRejectedException());

    // Act & Assert
    mockMvc.perform(post("/tokenize")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isUnprocessableEntity());
  }
}
