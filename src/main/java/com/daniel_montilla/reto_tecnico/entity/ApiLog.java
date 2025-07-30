package com.daniel_montilla.reto_tecnico.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "api_logs")
public class ApiLog {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String uuid;

  private String httpMethod;
  private String endpointUrl;
  private int responseStatus;

  @Column(columnDefinition = "TEXT")
  private String requestBody;

  @Column(columnDefinition = "TEXT")
  private String responseBody;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
