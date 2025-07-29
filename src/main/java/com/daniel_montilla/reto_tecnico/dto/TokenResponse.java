package com.daniel_montilla.reto_tecnico.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {
  private String token;
  private String status;
  private String message;
}
