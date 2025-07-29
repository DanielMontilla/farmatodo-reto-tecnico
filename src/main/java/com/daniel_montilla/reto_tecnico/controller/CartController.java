package com.daniel_montilla.reto_tecnico.controller;

import com.daniel_montilla.reto_tecnico.dto.CartDTO;
import com.daniel_montilla.reto_tecnico.dto.CartDTO.CartItemResponse;
import com.daniel_montilla.reto_tecnico.dto.CartDTO.CartResponse;
import com.daniel_montilla.reto_tecnico.entity.CartItem;
import com.daniel_montilla.reto_tecnico.service.CartService;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cart")
public class CartController {

  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping("/{clientId}")
  public ResponseEntity<CartResponse> getCartItems(@PathVariable Long clientId) {

    var items = cartService.getItemsOfClient(clientId);

    return ResponseEntity.ok(CartResponse.builder()
        .products(items.stream()
            .map(item -> CartResponse.Product.builder()
                .id(item.getProduct().getId())
                .name(item.getProduct().getName())
                .sku(item.getProduct().getSku())
                .price(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .build())
            .toList())
        .build());
  }

  @PostMapping("/add")
  public ResponseEntity<CartItemResponse> addItemToCart(@RequestBody CartDTO.CreateRequest body) {
    CartItem item = cartService.addProductToCart(
        body.getClientId(),
        body.getProductId(),
        body.getQuantity());

    return ResponseEntity.ok(CartItemResponse.builder()
        .id(item.getId())
        .product(CartItemResponse.Product.builder()
            .id(item.getProduct().getId())
            .name(item.getProduct().getName())
            .sku(item.getProduct().getSku())
            .price(item.getProduct().getPrice())
            .quantity(item.getQuantity())
            .build())
        .build());
  }

  @PostMapping("/add-many")
  public ResponseEntity<List<CartItemResponse>> addItemsToCart(@RequestBody CartDTO.CreateManyRequest body) {
    List<CartItemResponse> items = cartService.addProductsToCart(body.getClientId(),
        body.getProducts().stream().map(product -> Map.entry(product.getId(), product.getQuantity())).toList()).stream()
        .map(
            item -> CartItemResponse.builder()
                .id(item.getId())
                .product(CartItemResponse.Product.builder()
                    .id(item.getProduct().getId())
                    .name(item.getProduct().getName())
                    .sku(item.getProduct().getSku())
                    .price(item.getProduct().getPrice())
                    .quantity(item.getQuantity())
                    .build())
                .build())
        .toList();

    return ResponseEntity.ok(items);
  }

  @PostMapping("/remove")
  public ResponseEntity<Optional<CartItemResponse>> removeItemFromCart(@RequestBody CartDTO.DeleteRequest body) {
    Optional<CartItemResponse> item = cartService.removeProductFromCart(body.getClientId(), body.getProductId(),
        body.getQuantity())
        .map(cartItem -> CartItemResponse.builder()
            .id(cartItem.getId())
            .product(CartItemResponse.Product.builder().id(cartItem.getProduct().getId())
                .name(cartItem.getProduct().getName())
                .sku(cartItem.getProduct().getSku())
                .price(cartItem.getProduct().getPrice())
                .quantity(cartItem.getQuantity()).build())
            .build());

    return ResponseEntity.ok(item);
  }

  @DeleteMapping("/clear/{clientId}")
  public ResponseEntity<Void> clearCart(@PathVariable Long clientId) {
    cartService.clearCart(clientId);
    return ResponseEntity.noContent().build();
  }
}