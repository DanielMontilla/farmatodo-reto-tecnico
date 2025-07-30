package com.daniel_montilla.reto_tecnico;

import com.daniel_montilla.reto_tecnico.entity.ProductSearch;
import com.daniel_montilla.reto_tecnico.repository.ProductSearchRepository;
import com.daniel_montilla.reto_tecnico.service.ProductSearchService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProductSearchServiceTest {

  @MockitoBean
  private ProductSearchRepository productSearchRepository;

  @Autowired
  private ProductSearchService productSearchService;

  @Test
  void logSearch_WithValidSearchTerm_ShouldSaveLog() {
    // Arrange
    String searchTerm = "valid search";
    String sortBy = "price";
    String sortOrder = "desc";
    org.mockito.ArgumentCaptor<ProductSearch> productSearchCaptor = org.mockito.ArgumentCaptor
        .forClass(ProductSearch.class);

    // Act
    productSearchService.logSearch(searchTerm, sortBy, sortOrder);

    // Assert
    // Verify that the save method was called (with a timeout for the @Async
    // operation)
    // and capture the argument.
    verify(productSearchRepository, timeout(1000)).save(productSearchCaptor.capture());
    ProductSearch capturedSearch = productSearchCaptor.getValue();

    // Check if the captured object has the correct values
    assertEquals(searchTerm, capturedSearch.getSearchTerm());
    assertEquals(sortBy, capturedSearch.getSortBy());
    assertEquals(sortOrder, capturedSearch.getSortOrder());
  }

  @Test
  void logSearch_WithNullSearchTerm_ShouldNotSaveLog() {
    // Act
    productSearchService.logSearch(null, "name", "asc");

    // Assert
    // Verify that the save method was never called, allowing a brief moment for
    // async logic.
    verify(productSearchRepository, after(500).never()).save(any(ProductSearch.class));
  }

  @Test
  void logSearch_WithBlankSearchTerm_ShouldNotSaveLog() {
    // Act
    productSearchService.logSearch("   ", "name", "asc");

    // Assert
    // Verify that the save method was never called, allowing a brief moment for
    // async logic.
    verify(productSearchRepository, after(500).never()).save(any(ProductSearch.class));
  }

  @Test
  void getRecentSearches_ShouldCallRepositoryWithCorrectPaging() {
    // Arrange
    ProductSearch search = new ProductSearch();
    search.setSearchTerm("recent item");
    List<ProductSearch> expectedSearches = Collections.singletonList(search);
    org.mockito.ArgumentCaptor<Pageable> pageableCaptor = org.mockito.ArgumentCaptor.forClass(Pageable.class);

    // Mock the repository call to return our expected list
    when(productSearchRepository.findRecent(any(Pageable.class))).thenReturn(expectedSearches);

    // Act
    List<ProductSearch> actualSearches = productSearchService.getRecentSearches();

    // Assert
    // Verify the repository method was called and capture the Pageable argument
    verify(productSearchRepository).findRecent(pageableCaptor.capture());
    Pageable capturedPageable = pageableCaptor.getValue();

    assertEquals(expectedSearches, actualSearches, "The returned list should match the mocked list.");
    assertEquals(0, capturedPageable.getPageNumber(), "Page number should be 0.");
    assertEquals(10, capturedPageable.getPageSize(), "Page size should be 10.");
  }
}
