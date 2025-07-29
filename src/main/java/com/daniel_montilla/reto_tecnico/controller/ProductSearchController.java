package com.daniel_montilla.reto_tecnico.controller;

import com.daniel_montilla.reto_tecnico.entity.ProductSearch;
import com.daniel_montilla.reto_tecnico.service.ProductSearchService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product_searches")
public class ProductSearchController {

  private final ProductSearchService productSearchService;

  public ProductSearchController(ProductSearchService productSearchService) {
    this.productSearchService = productSearchService;
  }

  @GetMapping("/recent")
  public ResponseEntity<List<ProductSearch>> getRecentSearches() {
    List<ProductSearch> recentSearches = productSearchService.getRecentSearches();
    return ResponseEntity.ok(recentSearches);
  }

}