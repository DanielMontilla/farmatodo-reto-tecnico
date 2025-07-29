package com.daniel_montilla.reto_tecnico.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clients")
public class Client {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Name cannot be blank")
  @Column(nullable = false)
  private String name;

  @NotBlank(message = "Email cannot be blank")
  @Email(message = "Please provide a valid email address")
  @Column(nullable = false, unique = true)
  private String email;

  @NotBlank(message = "Phone cannot be blank")
  @Size(min = 7, message = "Phone number must be at least 7 digits")
  @Column(nullable = false, unique = true)
  private String phone;

  @Column(nullable = true)
  private String address;
}