package com.daniel_montilla.reto_tecnico.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CartDTO {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateRequest {
    @NotBlank(message = "Client ID cannot be blank")
    private Long clientId;

    @NotBlank(message = "Product ID cannot be blank")
    private Long productId;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DeleteRequest {
    @NotBlank(message = "Client ID cannot be blank")
    private Long clientId;

    @NotBlank(message = "Product ID cannot be blank")
    private Long productId;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateManyRequest {
    @NotBlank(message = "Client ID cannot be blank")
    private Long clientId;

    @NotEmpty(message = "Products list cannot be empty")
    private List<Product> products;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
      @NotNull(message = "Product ID cannot be null")
      private Long id;

      @NotNull(message = "Quantity cannot be null")
      @Positive(message = "Quantity must be positive")
      private Integer quantity;
    }
  }

  @Data
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class CartItemResponse {
    private Long id;
    private Product product;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Product {
      private Long id;
      private String sku;
      private String name;
      private double price;
      private Integer quantity;
    }
  }

  @Data
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class CartResponse {
    private double total;
    private List<Product> products;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Product {
      private Long id;
      private String sku;
      private String name;
      private double price;
      private Integer quantity;
    }
  }
}