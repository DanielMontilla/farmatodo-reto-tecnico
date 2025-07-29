package com.daniel_montilla.reto_tecnico.service;

import com.daniel_montilla.reto_tecnico.entity.Product;
import com.daniel_montilla.reto_tecnico.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductSearchService productSearchService;

  @Value("${products.search.min-stock}")
  private int configuredMinStock;

  public ProductService(ProductRepository productRepository, ProductSearchService productSearchService) {
    this.productRepository = productRepository;
    this.productSearchService = productSearchService;
  }

  public List<Product> searchProducts(String searchTerm, String sortBy, String sortOrder) {
    productSearchService.logSearch(searchTerm, sortBy, sortOrder);

    Specification<Product> spec = (root, _, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (searchTerm != null && !searchTerm.isBlank()) {
        String likePattern = "%" + searchTerm.toLowerCase() + "%";
        Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern);
        Predicate descriptionPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
            likePattern);
        predicates.add(criteriaBuilder.or(namePredicate, descriptionPredicate));
      }

      predicates.add(criteriaBuilder.greaterThan(root.get("quantity"), configuredMinStock));

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };

    Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);

    return productRepository.findAll(spec, sort);
  }
}
