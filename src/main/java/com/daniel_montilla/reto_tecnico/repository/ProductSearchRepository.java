package com.daniel_montilla.reto_tecnico.repository;

import com.daniel_montilla.reto_tecnico.entity.ProductSearch;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends JpaRepository<ProductSearch, Long> {

  @Query("SELECT ps FROM ProductSearch ps ORDER BY ps.createdAt DESC")
  List<ProductSearch> findRecent(Pageable pageable);
}