package com.daniel_montilla.reto_tecnico;

import com.daniel_montilla.reto_tecnico.entity.CartItem;
import com.daniel_montilla.reto_tecnico.entity.Client;
import com.daniel_montilla.reto_tecnico.entity.Product;
import com.daniel_montilla.reto_tecnico.repository.CartItemRepository;
import com.daniel_montilla.reto_tecnico.repository.ClientRepository;
import com.daniel_montilla.reto_tecnico.repository.ProductRepository;
import com.daniel_montilla.reto_tecnico.service.CartService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class CartServiceTest {

  @MockitoBean
  private CartItemRepository cartItemRepository;
  @MockitoBean
  private ClientRepository clientRepository;
  @MockitoBean
  private ProductRepository productRepository;

  @Autowired
  private CartService cartService;

  private Client client;
  private Product product;

  @BeforeEach
  void setUp() {
    client = Client.builder().id(1L).name("Test Client").build();
    product = Product.builder().id(10L).name("Test Product").price(100.0).build();
  }

  @Test
  void getItemsOfClient_ShouldCallRepository() {
    // Act
    cartService.getItemsOfClient(1L);
    // Assert
    verify(cartItemRepository).findAllWithProductByClientIdAndFulfilledFalse(1L);
  }

  @Test
  void addProductToCart_WhenItemIsNew_ShouldCreateAndSaveNewItem() {
    // Arrange
    when(cartItemRepository.findByClientIdAndProductIdAndFulfilledFalse(1L, 10L)).thenReturn(Optional.empty());
    when(clientRepository.getReferenceById(1L)).thenReturn(client);
    when(productRepository.getReferenceById(10L)).thenReturn(product);
    when(cartItemRepository.save(any(CartItem.class))).thenReturn(new CartItem());

    // Act
    cartService.addProductToCart(1L, 10L, 2);

    // Assert
    verify(cartItemRepository).save(any(CartItem.class));
  }

  @Test
  void addProductToCart_WhenItemExists_ShouldUpdateQuantityAndSave() {
    // Arrange
    CartItem existingItem = CartItem.builder().id(100L).client(client).product(product).quantity(3).build();
    when(cartItemRepository.findByClientIdAndProductIdAndFulfilledFalse(1L, 10L)).thenReturn(Optional.of(existingItem));
    when(cartItemRepository.save(any(CartItem.class))).thenReturn(existingItem);

    // Act
    cartService.addProductToCart(1L, 10L, 2);

    // Assert
    verify(cartItemRepository).save(existingItem);
    assertEquals(5, existingItem.getQuantity());
  }

  @Test
  void addProductsToCart_ShouldCallAddProductForEachItem() {
    // Arrange
    Product product2 = Product.builder().id(11L).name("Test Product 2").price(200.0).build();
    List<Map.Entry<Long, Integer>> items = List.of(Map.entry(10L, 2), Map.entry(11L, 3));

    when(cartItemRepository.findByClientIdAndProductIdAndFulfilledFalse(anyLong(), anyLong()))
        .thenReturn(Optional.empty());
    when(clientRepository.getReferenceById(1L)).thenReturn(client);
    when(productRepository.getReferenceById(10L)).thenReturn(product);
    when(productRepository.getReferenceById(11L)).thenReturn(product2);
    when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    cartService.addProductsToCart(1L, items);

    // Assert
    verify(cartItemRepository, times(2)).save(any(CartItem.class));
  }

  @Test
  void removeProductFromCart_WhenPartialRemove_ShouldUpdateQuantity() {
    // Arrange
    CartItem existingItem = CartItem.builder().id(100L).client(client).product(product).quantity(5).build();
    when(cartItemRepository.findByClientIdAndProductIdAndFulfilledFalse(1L, 10L)).thenReturn(Optional.of(existingItem));
    when(cartItemRepository.save(any(CartItem.class))).thenReturn(existingItem);

    // Act
    Optional<CartItem> result = cartService.removeProductFromCart(1L, 10L, 2);

    // Assert
    assertTrue(result.isPresent());
    assertEquals(3, result.get().getQuantity());
    verify(cartItemRepository).save(existingItem);
  }

  @Test
  void removeProductFromCart_WhenFullRemove_ShouldDeleteItem() {
    // Arrange
    CartItem existingItem = CartItem.builder().id(100L).client(client).product(product).quantity(5).build();
    when(cartItemRepository.findByClientIdAndProductIdAndFulfilledFalse(1L, 10L)).thenReturn(Optional.of(existingItem));

    // Act
    Optional<CartItem> result = cartService.removeProductFromCart(1L, 10L, 5);

    // Assert
    assertTrue(result.isEmpty());
    verify(cartItemRepository).delete(existingItem);
  }

  @Test
  void removeProductFromCart_WhenItemNotFound_ShouldReturnEmpty() {
    // Arrange
    when(cartItemRepository.findByClientIdAndProductIdAndFulfilledFalse(1L, 10L)).thenReturn(Optional.empty());

    // Act
    Optional<CartItem> result = cartService.removeProductFromCart(1L, 10L, 1);

    // Assert
    assertTrue(result.isEmpty());
    verify(cartItemRepository, never()).delete(any());
    verify(cartItemRepository, never()).save(any());
  }

  @Test
  void clearCart_ShouldCallRepository() {
    // Act
    cartService.clearCart(1L);
    // Assert
    verify(cartItemRepository).deleteAllByClientId(1L);
  }

  @Test
  void getTotal_ShouldCalculateCorrectTotal() {
    // Arrange
    Product p1 = Product.builder().id(1L).price(10.0).build();
    Product p2 = Product.builder().id(2L).price(25.5).build();
    List<CartItem> items = List.of(
        CartItem.builder().product(p1).quantity(2).build(), // 2 * 10.0 = 20.0
        CartItem.builder().product(p2).quantity(3).build() // 3 * 25.5 = 76.5
    );

    // Act
    BigDecimal total = cartService.getTotal(items);

    // Assert
    assertEquals(0, new BigDecimal("96.5").compareTo(total));
  }
}
