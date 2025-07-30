package com.daniel_montilla.reto_tecnico;

import com.daniel_montilla.reto_tecnico.entity.ProductSearch;
import com.daniel_montilla.reto_tecnico.service.ApiKeyAuthService;
import com.daniel_montilla.reto_tecnico.service.ProductSearchService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ProductSearchControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ProductSearchService productSearchService;

  @MockitoBean
  private ApiKeyAuthService apiKeyAuthService;

  private final String MOCK_API_KEY = "test-api-key";

  @BeforeEach
  void setUp() {
    when(apiKeyAuthService.isApiKeyValid(anyString())).thenReturn(true);
  }

  @Test
  void getRecentSearches_WhenSearchesExist_ShouldReturnListOfSearches() throws Exception {
    // Arrange
    ProductSearch search1 = new ProductSearch();
    search1.setId(1L);
    search1.setSearchTerm("laptop");
    search1.setSortBy("price");
    search1.setSortOrder("asc");
    search1.setCreatedAt(LocalDateTime.now());

    ProductSearch search2 = new ProductSearch();
    search2.setId(2L);
    search2.setSearchTerm("keyboard");
    search2.setSortBy("name");
    search2.setSortOrder("desc");
    search2.setCreatedAt(LocalDateTime.now().minusHours(1));

    List<ProductSearch> recentSearches = Arrays.asList(search1, search2);
    when(productSearchService.getRecentSearches()).thenReturn(recentSearches);

    // Act & Assert
    mockMvc.perform(get("/product_searches/recent")
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].searchTerm", is("laptop")))
        .andExpect(jsonPath("$[1].searchTerm", is("keyboard")));
  }

  @Test
  void getRecentSearches_WhenNoSearchesExist_ShouldReturnEmptyList() throws Exception {
    // Arrange
    when(productSearchService.getRecentSearches()).thenReturn(Collections.emptyList());

    // Act & Assert
    mockMvc.perform(get("/product_searches/recent")
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }
}
