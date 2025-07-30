package com.daniel_montilla.reto_tecnico.repository;

import com.daniel_montilla.reto_tecnico.entity.CartItem;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
  /**
   * Finds all cart items for a client by their ID and eagerly fetches the
   * associated Product data.
   * This is a more convenient version of the method above.
   *
   * @param clientId The ID of the client whose cart items are to be retrieved.
   * @return A list of CartItem objects with their Product data pre-loaded.
   */
  @EntityGraph(attributePaths = { "product" })
  List<CartItem> findAllWithProductByClientIdAndFulfilledFalse(Long clientId);

  /**
   * Finds a specific cart item for a client and product.
   * This is useful for checking if a product is already in the cart.
   *
   * @param client    The client.
   * @param productId The ID of the product.
   * @return An Optional containing the CartItem if it exists.
   */
  Optional<CartItem> findByClientIdAndProductIdAndFulfilledFalse(Long clientId, Long productId);

  void deleteAllByClientId(Long clientId);

  List<CartItem> findAllByClientIdAndFulfilledFalse(Long clientId);

  /**
   * Updates all unfulfilled cart items for a given client to be fulfilled.
   * This is an efficient bulk update operation.
   *
   * @param clientId The ID of the client whose cart items should be marked as
   *                 fulfilled.
   */
  @Modifying
  @Query("UPDATE CartItem c SET c.fulfilled = true WHERE c.client.id = :clientId AND c.fulfilled = false")
  void fulfillAllByClientId(Long clientId);
}
