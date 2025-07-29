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
    var product = new Product();

    product.setSku(body.getSku());
    product.setName(body.getName());
    product.setDescription(body.getDescription());
    product.setPrice(body.getPrice());
    product.setQuantity(body.getQuantity());

    try {
      Product savedProduct = productRepository.save(product);
      return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/batch")
  public ResponseEntity<List<Product>> createProducts(@Valid @RequestBody List<ProductDTO.CreateRequest> requests) {
    try {
      List<Product> products = requests.stream()
          .map(request -> {
            var product = new Product();
            product.setSku(request.getSku());
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setQuantity(request.getQuantity());
            return product;
          })
          .toList();

      List<Product> savedProducts = productRepository.saveAll(products);
      return ResponseEntity.status(HttpStatus.CREATED).body(savedProducts);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
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
      @RequestParam(defaultValue = "asc") String sortOrder) {

    try {
      List<Product> products = productService.searchProducts(query, sortBy, sortOrder);
      System.out.println("Here! " + products);
      return ResponseEntity.ok(products);

    } catch (Exception e) {
      System.out.println("Error! " + e);
      return ResponseEntity.badRequest().build();
    }
  }
}