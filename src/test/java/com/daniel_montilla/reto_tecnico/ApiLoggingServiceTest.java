package com.daniel_montilla.reto_tecnico;

import com.daniel_montilla.reto_tecnico.entity.ApiLog;
import com.daniel_montilla.reto_tecnico.repository.ApiLogRepository;
import com.daniel_montilla.reto_tecnico.service.ApiLoggingService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ApiLoggingServiceTest {

  @Mock
  private ApiLogRepository apiLogRepository;

  @InjectMocks
  private ApiLoggingService apiLoggingService;

  @Test
  void logRequest_ShouldCallRepositorySave() {
    // Arrange
    ApiLog log = ApiLog.builder()
        .httpMethod("GET")
        .endpointUrl("/test-endpoint")
        .responseStatus(200)
        .build();

    // Act
    apiLoggingService.logRequest(log);

    // Assert
    // Verify that the save method of the repository was called exactly once
    // with any ApiLog object. This confirms the service delegates the save
    // operation.
    verify(apiLogRepository).save(any(ApiLog.class));
  }
}
