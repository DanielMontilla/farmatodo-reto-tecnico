package com.daniel_montilla.reto_tecnico.dto;

import java.util.Optional;

import com.daniel_montilla.reto_tecnico.entity.Order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class OrderDTO {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PlaceRequest {

    @NotNull(message = "Client ID cannot be null")
    private Long clientId;

    private String deliveryAddress;

    @NotNull(message = "Must have credit card details")
    private CreditCardDTO.CreditCardDetailsRequest creditCardDetails;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderItemResponse {
    private Long id;
    private double totalPrice;
    private Optional<String> deliveryAddress;
    private Order.Status status;
    private CartDTO.CartResponse cart;
  }
}
