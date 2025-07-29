package com.daniel_montilla.reto_tecnico.service;

import com.daniel_montilla.reto_tecnico.entity.ProductSearch;
import com.daniel_montilla.reto_tecnico.repository.ProductSearchRepository;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

@Service
public class ProductSearchService {

  private final ProductSearchRepository productSearchRepository;

  public ProductSearchService(ProductSearchRepository productSearchRepository) {
    this.productSearchRepository = productSearchRepository;
  }

  @Async
  public void logSearch(String searchTerm, String sortBy, String sortOrder) {
    if (searchTerm == null || searchTerm.isBlank()) {
      return;
    }

    ProductSearch search = new ProductSearch();
    search.setSearchTerm(searchTerm);
    search.setSortBy(sortBy);
    search.setSortOrder(sortOrder);

    productSearchRepository.save(search);
  }

  public List<ProductSearch> getRecentSearches() {
    return productSearchRepository.findRecent(PageRequest.of(0, 10));
  }
}
