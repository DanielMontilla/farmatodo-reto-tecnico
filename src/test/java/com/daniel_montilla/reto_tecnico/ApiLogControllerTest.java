package com.daniel_montilla.reto_tecnico;

import com.daniel_montilla.reto_tecnico.entity.ApiLog;
import com.daniel_montilla.reto_tecnico.repository.ApiLogRepository;
import com.daniel_montilla.reto_tecnico.service.ApiKeyAuthService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ApiLogControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ApiLogRepository apiLogRepository;

  @MockitoBean
  private ApiKeyAuthService apiKeyAuthService;

  private final String MOCK_API_KEY = "test-api-key";

  @BeforeEach
  void setUp() {
    when(apiKeyAuthService.isApiKeyValid(anyString())).thenReturn(true);
    apiLogRepository.deleteAll();
  }

  @Test
  void getAllApiLogs_WhenLogsExist_ShouldReturnListOfLogs() throws Exception {
    // Arrange
    apiLogRepository.save(ApiLog.builder()
        .httpMethod("POST")
        .endpointUrl("/clients")
        .responseStatus(201)
        .requestBody("{\"name\":\"Test Client\"}")
        .responseBody("{\"id\":1}")
        .build());

    apiLogRepository.save(ApiLog.builder()
        .httpMethod("GET")
        .endpointUrl("/products/1")
        .responseStatus(200)
        .requestBody(null)
        .responseBody("{\"id\":1, \"name\":\"Test Product\"}")
        .build());

    // Act & Assert
    mockMvc.perform(get("/api-logs")
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].httpMethod", is("POST")))
        .andExpect(jsonPath("$[1].endpointUrl", is("/products/1")));
  }

  @Test
  void getAllApiLogs_WhenNoLogsExist_ShouldReturnEmptyList() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/api-logs")
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }
}
