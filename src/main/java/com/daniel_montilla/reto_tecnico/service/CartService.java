package com.daniel_montilla.reto_tecnico.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.daniel_montilla.reto_tecnico.entity.CartItem;
import com.daniel_montilla.reto_tecnico.entity.Client;
import com.daniel_montilla.reto_tecnico.entity.Product;
import com.daniel_montilla.reto_tecnico.repository.CartItemRepository;
import com.daniel_montilla.reto_tecnico.repository.ClientRepository;
import com.daniel_montilla.reto_tecnico.repository.ProductRepository;

import java.util.Map;

@Service
public class CartService {

  private final CartItemRepository cartItemRepository;
  private final ClientRepository clientRepository;
  private final ProductRepository productRepository;

  public CartService(CartItemRepository cartItemRepository, ClientRepository clientRepository,
      ProductRepository productRepository) {
    this.cartItemRepository = cartItemRepository;
    this.clientRepository = clientRepository;
    this.productRepository = productRepository;
  }

  public List<CartItem> getItemsOfClient(Long clientId) {
    return cartItemRepository.findAllWithProductByClientIdAndFulfilledFalse(clientId);
  }

  public CartItem addProductToCart(Long clientId, Long productId, int quantity) {

    Client clientRef = clientRepository.getReferenceById(clientId);
    Product productRef = productRepository.getReferenceById(productId);

    Optional<CartItem> existing = cartItemRepository.findByClientIdAndProductIdAndFulfilledFalse(clientId, productId);

    if (existing.isPresent()) {
      CartItem existingItem = existing.get();
      existingItem.setQuantity(existingItem.getQuantity() + quantity);
      return cartItemRepository.save(existingItem);
    }

    CartItem newItem = CartItem.builder()
        .client(clientRef)
        .product(productRef)
        .quantity(quantity)
        .build();

    return cartItemRepository.save(newItem);
  }

  public List<CartItem> addProductsToCart(Long clientId, List<Map.Entry<Long, Integer>> items) {
    return items.stream() // This has to be sequential sinse user may input two of the same product
        .map(entry -> addProductToCart(clientId, entry.getKey(), entry.getValue()))
        .toList();
  }

  public Optional<CartItem> removeProductFromCart(Long clientId, Long productId, int quantity) {
    Optional<CartItem> existing = cartItemRepository.findByClientIdAndProductIdAndFulfilledFalse(clientId, productId);

    if (existing.isEmpty()) {
      return Optional.empty();
    }

    CartItem item = existing.get();

    var newQuantity = item.getQuantity() - quantity;

    if (newQuantity <= 0) {
      cartItemRepository.delete(item);
      return Optional.empty();
    }

    item.setQuantity(newQuantity);
    CartItem updatedItem = cartItemRepository.save(item);
    return Optional.of(updatedItem);
  }

  public void clearCart(Long clientId) {
    cartItemRepository.deleteAllByClientId(clientId);
  }

  public BigDecimal getTotal(List<CartItem> cartItems) {
    return cartItems.stream()
        .map(item -> BigDecimal.valueOf(item.getQuantity()).multiply(BigDecimal.valueOf(item.getProduct().getPrice())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}