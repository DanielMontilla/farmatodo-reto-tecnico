package com.daniel_montilla.reto_tecnico.controller;

import com.daniel_montilla.reto_tecnico.entity.ApiLog;
import com.daniel_montilla.reto_tecnico.repository.ApiLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api-logs")
public class ApiLogController {

  private final ApiLogRepository apiLogRepository;

  @Autowired
  public ApiLogController(ApiLogRepository apiLogRepository) {
    this.apiLogRepository = apiLogRepository;
  }

  @GetMapping
  public ResponseEntity<List<ApiLog>> getAllApiLogs() {
    List<ApiLog> logs = apiLogRepository.findAll();
    return ResponseEntity.ok(logs);
  }
}