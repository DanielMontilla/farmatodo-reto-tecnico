package com.daniel_montilla.reto_tecnico.service;

import com.daniel_montilla.reto_tecnico.entity.ApiLog;
import com.daniel_montilla.reto_tecnico.repository.ApiLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ApiLoggingService {

  @Autowired
  private ApiLogRepository apiLogRepository;

  @Async
  public void logRequest(ApiLog log) {
    apiLogRepository.save(log);
  }
}