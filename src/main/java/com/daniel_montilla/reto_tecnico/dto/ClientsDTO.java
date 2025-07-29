package com.daniel_montilla.reto_tecnico.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class ClientsDTO {

  @Data
  public static class CreateRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Phone cannot be blank")
    @Size(min = 7, message = "Phone number must be at least 7 digits")
    private String phone;

    private String address;
  }

  @Data
  public static class UpdateRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Email(message = "Please provide a valid email address")
    private String email;

    @Size(min = 7, message = "Phone number must be at least 7 digits")
    private String phone;

    private String address;
  }
}