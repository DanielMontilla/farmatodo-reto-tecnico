package com.daniel_montilla.reto_tecnico.controller;

import com.daniel_montilla.reto_tecnico.dto.CartDTO;
import com.daniel_montilla.reto_tecnico.dto.OrderDTO;
import com.daniel_montilla.reto_tecnico.entity.Order;
import com.daniel_montilla.reto_tecnico.exception.NoItemsInCartException;
import com.daniel_montilla.reto_tecnico.exception.NotFoundException;
import com.daniel_montilla.reto_tecnico.exception.TokenizationRejectedException;
import com.daniel_montilla.reto_tecnico.service.OrderService;

import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/order")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping("/place")
  public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderDTO.PlaceRequest body) throws NotFoundException {
    try {
      Order newOrder = orderService.placeOrder(
          body.getClientId(),
          Optional.ofNullable(body.getDeliveryAddress()),
          body.getCreditCardDetails().getCardNumber(),
          body.getCreditCardDetails().getExpirationDate(),
          body.getCreditCardDetails().getCvv(),
          body.getCreditCardDetails().getCardHolderName());

      URI location = ServletUriComponentsBuilder
          .fromCurrentContextPath()
          .path("/order/{id}")
          .buildAndExpand(newOrder.getId())
          .toUri();

      return ResponseEntity.created(location).build();

    } catch (NoItemsInCartException e) {
      return ResponseEntity
          .badRequest()
          .body(Map.of("error", "Cannot place an order with an empty cart."));
    } catch (TokenizationRejectedException e) {
      return ResponseEntity
          .badRequest()
          .body(Map.of("error", "Credit card rejected. Try again."));
    }
  }

  @GetMapping("/{clientId}")
  public ResponseEntity<List<OrderDTO.OrderItemResponse>> getClientOrders(@PathVariable Long clientId) {
    List<Order> clientOrders = orderService.getOrdersOfClient(clientId);

    List<OrderDTO.OrderItemResponse> response = clientOrders.stream()
        .map(this::mapOrderToDto)
        .collect(Collectors.toList());

    return ResponseEntity.ok(response);
  }

  private OrderDTO.OrderItemResponse mapOrderToDto(Order order) {
    List<CartDTO.CartResponse.Product> cartProducts = order.getCartItems().stream()
        .map(item -> CartDTO.CartResponse.Product.builder()
            .id(item.getProduct().getId())
            .sku(item.getProduct().getSku())
            .name(item.getProduct().getName())
            .price(item.getProduct().getPrice())
            .quantity(item.getQuantity())
            .build())
        .collect(Collectors.toList());

    CartDTO.CartResponse cartResponse = CartDTO.CartResponse.builder()
        .total(order.getTotalPrice().doubleValue())
        .products(cartProducts)
        .build();

    return OrderDTO.OrderItemResponse.builder()
        .id(order.getId())
        .totalPrice(order.getTotalPrice().doubleValue())
        .deliveryAddress(Optional.ofNullable(order.getDeliveryAddress()))
        .status(order.getStatus())
        .cart(cartResponse)
        .build();
  }
}