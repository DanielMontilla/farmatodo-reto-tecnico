package com.daniel_montilla.reto_tecnico.controller;

import com.daniel_montilla.reto_tecnico.dto.ProductDTO;
import com.daniel_montilla.reto_tecnico.entity.Product;
import com.daniel_montilla.reto_tecnico.repository.ProductRepository;
import com.daniel_montilla.reto_tecnico.service.ProductService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {

  private final ProductRepository productRepository;
  private final ProductService productService;

  public ProductController(ProductRepository productRepository, ProductService productService) {
    this.productRepository = productRepository;
    this.productService = productService;
  }

  @GetMapping
  public ResponseEntity<List<Product>> getAllProducts() {
    List<Product> products = productRepository.findAll();
    return ResponseEntity.ok(products);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Product> getProductById(@PathVariable Long id) {
    Optional<Product> product = productRepository.findById(id);
    return product.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDTO.CreateRequest body) {
    Product product = Product.builder()
        .sku(body.getSku())
        .name(body.getName())
        .description(body.getDescription())
        .price(body.getPrice())
        .quantity(body.getQuantity())
        .build();

    Product savedProduct = productRepository.save(product);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
  }

  @PostMapping("/batch")
  public ResponseEntity<List<Product>> createProducts(@Valid @RequestBody List<ProductDTO.CreateRequest> body) {
    List<Product> products = productRepository.saveAll(body.stream()
        .map(request -> Product.builder()
            .sku(request.getSku())
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .quantity(request.getQuantity())
            .build())
        .toList());

    return ResponseEntity.status(HttpStatus.CREATED).body(products);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Product> updateProduct(@PathVariable Long id,
      @Valid @RequestBody ProductDTO.UpdateRequest body) {
    return productRepository.findById(id)
        .map(existingProduct -> {
          if (body.getSku() != null) {
            existingProduct.setSku(body.getSku());
          }
          if (body.getName() != null) {
            existingProduct.setName(body.getName());
          }
          if (body.getDescription() != null) {
            existingProduct.setDescription(body.getDescription());
          }
          if (body.getPrice() != null) {
            existingProduct.setPrice(body.getPrice());
          }
          if (body.getQuantity() != null) {
            existingProduct.setQuantity(body.getQuantity());
          }
          Product updatedProduct = productRepository.save(existingProduct);
          return ResponseEntity.ok(updatedProduct);
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    return productRepository.findById(id)
        .map(product -> {
          productRepository.delete(product);
          return ResponseEntity.noContent().<Void>build();
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/search")
  public ResponseEntity<List<Product>> searchProducts(
      @RequestParam(name = "q", required = false) String query,
      @RequestParam(defaultValue = "name") String sortBy,
      @RequestParam(defaultValue = "asc") String sortOrder,
      @RequestParam(defaultValue = "0") String minStock) {

    int minStockInt;
    try {
      minStockInt = Integer.parseInt(minStock);
      if (minStockInt < 0) {
        return ResponseEntity.badRequest().build();
      }
    } catch (NumberFormatException e) {
      return ResponseEntity.badRequest().build();
    }

    List<Product> products = productService.searchProducts(query, sortBy, sortOrder, minStockInt);
    return ResponseEntity.ok(products);
  }
}