package com.daniel_montilla.reto_tecnico;

import com.daniel_montilla.reto_tecnico.dto.ProductDTO;
import com.daniel_montilla.reto_tecnico.entity.Product;
import com.daniel_montilla.reto_tecnico.repository.ProductRepository;
import com.daniel_montilla.reto_tecnico.service.ApiKeyAuthService;
import com.daniel_montilla.reto_tecnico.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ProductControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ApiKeyAuthService apiKeyAuthService;

  @MockitoBean
  private ProductService productService;

  private final String MOCK_API_KEY = "test-api-key";

  @BeforeEach
  void setUp() {
    // Mock the API key validation to always return true for tests
    when(apiKeyAuthService.isApiKeyValid(anyString())).thenReturn(true);
    // Clean the repository before each test to ensure isolation
    productRepository.deleteAll();
  }

  // --- GET /products ---
  @Test
  void getAllProducts_ShouldReturnListOfProducts() throws Exception {
    // Arrange: Create and save two products
    productRepository.save(Product.builder().sku("SKU001").name("Laptop Pro").description("A powerful laptop")
        .price(1499.99).quantity(50).build());
    productRepository.save(Product.builder().sku("SKU002").name("Wireless Mouse").description("An ergonomic mouse")
        .price(79.99).quantity(200).build());

    // Act & Assert
    mockMvc.perform(get("/products")
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name", is("Laptop Pro")));
  }

  // --- GET /products/{id} ---
  @Test
  void getProductById_WhenProductExists_ShouldReturnProduct() throws Exception {
    // Arrange: Create and save a product
    Product product = productRepository.save(Product.builder().sku("SKU003").name("Mechanical Keyboard")
        .description("A durable keyboard").price(120.00).quantity(75).build());

    // Act & Assert
    mockMvc.perform(get("/products/" + product.getId())
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(product.getId().intValue())))
        .andExpect(jsonPath("$.name", is("Mechanical Keyboard")));
  }

  @Test
  void getProductById_WhenProductDoesNotExist_ShouldReturnNotFound() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/products/9999")
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isNotFound());
  }

  // --- POST /products ---
  @Test
  void createProduct_WithValidData_ShouldCreateAndReturnProduct() throws Exception {
    // Arrange
    ProductDTO.CreateRequest createRequest = new ProductDTO.CreateRequest();
    createRequest.setSku("SKU-NEW-01");
    createRequest.setName("New Awesome Product");
    createRequest.setDescription("This is a brand new product.");
    createRequest.setPrice(99.99);
    createRequest.setQuantity(100);

    // Act & Assert
    mockMvc.perform(post("/products")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.sku", is("SKU-NEW-01")))
        .andExpect(jsonPath("$.name", is("New Awesome Product")));
  }

  // --- POST /products/batch ---
  @Test
  void createProducts_WithValidData_ShouldCreateAndReturnListOfProducts() throws Exception {
    // Arrange
    ProductDTO.CreateRequest product1 = new ProductDTO.CreateRequest();
    product1.setSku("BATCH-001");
    product1.setName("Batch Product One");
    product1.setDescription("First in batch");
    product1.setPrice(10.0);
    product1.setQuantity(10);

    ProductDTO.CreateRequest product2 = new ProductDTO.CreateRequest();
    product2.setSku("BATCH-002");
    product2.setName("Batch Product Two");
    product2.setDescription("Second in batch");
    product2.setPrice(20.0);
    product2.setQuantity(20);

    List<ProductDTO.CreateRequest> batchRequest = Arrays.asList(product1, product2);

    // Act & Assert
    mockMvc.perform(post("/products/batch")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(batchRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name", is("Batch Product One")))
        .andExpect(jsonPath("$[1].name", is("Batch Product Two")));
  }

  // --- PUT /products/{id} ---
  @Test
  void updateProduct_WhenProductExists_ShouldUpdateAndReturnProduct() throws Exception {
    // Arrange: Create an initial product
    Product existingProduct = productRepository.save(
        Product.builder().sku("OLD-SKU").name("Old Name").description("Old Desc").price(50.0).quantity(5).build());

    ProductDTO.UpdateRequest updateRequest = new ProductDTO.UpdateRequest();
    updateRequest.setName("Updated Product Name");
    updateRequest.setPrice(55.55);

    // Act & Assert
    mockMvc.perform(put("/products/" + existingProduct.getId())
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(existingProduct.getId().intValue())))
        .andExpect(jsonPath("$.name", is("Updated Product Name")))
        .andExpect(jsonPath("$.price", is(55.55)))
        .andExpect(jsonPath("$.sku", is("OLD-SKU"))); // Unchanged field
  }

  @Test
  void updateProduct_WhenProductDoesNotExist_ShouldReturnNotFound() throws Exception {
    // Arrange
    ProductDTO.UpdateRequest updateRequest = new ProductDTO.UpdateRequest();
    updateRequest.setName("This name won't be used");

    // Act & Assert
    mockMvc.perform(put("/products/9999")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isNotFound());
  }

  // --- DELETE /products/{id} ---
  @Test
  void deleteProduct_WhenProductExists_ShouldDeleteAndReturnNoContent() throws Exception {
    // Arrange: Create a product to be deleted
    Product productToDelete = productRepository.save(Product.builder().sku("DEL-SKU-01").name("Product to Delete")
        .description("...").price(1.0).quantity(1).build());
    Long productId = productToDelete.getId();

    // Act & Assert
    mockMvc.perform(delete("/products/" + productId)
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isNoContent());

    // Verify it's gone from the database
    assertFalse(productRepository.findById(productId).isPresent());
  }

  @Test
  void deleteProduct_WhenProductDoesNotExist_ShouldReturnNotFound() throws Exception {
    // Act & Assert
    mockMvc.perform(delete("/products/9999")
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isNotFound());
  }

  // --- GET /products/search ---
  @Test
  void searchProducts_ShouldCallServiceAndReturnResults() throws Exception {
    // Arrange
    Product p1 = Product.builder().id(1L).sku("S1").name("Gaming Laptop").price(2000.0).quantity(15).build();
    Product p2 = Product.builder().id(2L).sku("S2").name("Office Laptop").price(900.0).quantity(30).build();
    List<Product> mockResults = Arrays.asList(p1, p2);

    // Mock the service call
    when(productService.searchProducts("Laptop", "price", "desc", 10))
        .thenReturn(mockResults);

    // Act & Assert
    mockMvc.perform(get("/products/search")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .param("q", "Laptop")
        .param("sortBy", "price")
        .param("sortOrder", "desc")
        .param("minStock", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name", is("Gaming Laptop")));

    // Verify the service method was called with the correct parameters
    verify(productService).searchProducts("Laptop", "price", "desc", 10);
  }

  @Test
  void searchProducts_WhenMinStockIsInvalidString_ShouldReturnBadRequest() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/products/search")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .param("minStock", "not-a-number"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void searchProducts_WhenMinStockIsNegative_ShouldReturnBadRequest() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/products/search")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .param("minStock", "-5"))
        .andExpect(status().isBadRequest());
  }
}
