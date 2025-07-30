package com.daniel_montilla.reto_tecnico;

import com.daniel_montilla.reto_tecnico.service.ApiKeyAuthService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class PingControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ApiKeyAuthService apiKeyAuthService;

  private final String MOCK_API_KEY = "test-api-key";

  @BeforeEach
  void setUp() {
    // Mock the API key validation to always return true for tests
    // Even though /ping is likely unsecured, this maintains consistency
    when(apiKeyAuthService.isApiKeyValid(anyString())).thenReturn(true);
  }

  @Test
  void ping_ShouldReturnPong() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/ping")
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isOk())
        .andExpect(content().string("pong"));
  }
}
