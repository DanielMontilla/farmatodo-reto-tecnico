package com.daniel_montilla.reto_tecnico.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

public class ProductDTO {

  @Data
  public static class CreateRequest {
    @NotBlank(message = "SKU cannot be blank")
    @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
    private String sku;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    private Integer quantity;
  }

  @Data
  public static class UpdateRequest {
    @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
    private String sku;

    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double price;

    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    private Integer quantity;
  }
}