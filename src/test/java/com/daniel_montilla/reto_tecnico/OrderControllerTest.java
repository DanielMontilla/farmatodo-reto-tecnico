package com.daniel_montilla.reto_tecnico;

import com.daniel_montilla.reto_tecnico.dto.CreditCardDTO;
import com.daniel_montilla.reto_tecnico.dto.OrderDTO;
import com.daniel_montilla.reto_tecnico.entity.Client;
import com.daniel_montilla.reto_tecnico.entity.Order;
import com.daniel_montilla.reto_tecnico.entity.Product;
import com.daniel_montilla.reto_tecnico.exception.NoItemsInCartException;
import com.daniel_montilla.reto_tecnico.exception.NotFoundException;
import com.daniel_montilla.reto_tecnico.exception.TokenizationRejectedException;
import com.daniel_montilla.reto_tecnico.repository.ClientRepository;
import com.daniel_montilla.reto_tecnico.repository.ProductRepository;
import com.daniel_montilla.reto_tecnico.service.ApiKeyAuthService;
import com.daniel_montilla.reto_tecnico.service.OrderService;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class OrderControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ClientRepository clientRepository;

  @Autowired
  private ProductRepository productRepository;

  @MockitoBean
  private OrderService orderService;

  @MockitoBean
  private ApiKeyAuthService apiKeyAuthService;

  private final String MOCK_API_KEY = "test-api-key";

  private Client testClient;
  private OrderDTO.PlaceRequest placeRequest;

  @BeforeEach
  void setUp() {
    when(apiKeyAuthService.isApiKeyValid(anyString())).thenReturn(true);

    productRepository.deleteAll();
    clientRepository.deleteAll();

    testClient = clientRepository.save(
        Client.builder().name("Order Client").email("order@test.com").phone("87654321").address("123 Main St").build());

    CreditCardDTO.CreditCardDetailsRequest cardDetails = new CreditCardDTO.CreditCardDetailsRequest();
    cardDetails.setCardNumber("49927398716");
    cardDetails.setExpirationDate("12/28");
    cardDetails.setCvv("123");
    cardDetails.setCardHolderName("Test Holder");

    placeRequest = OrderDTO.PlaceRequest.builder()
        .clientId(testClient.getId())
        .deliveryAddress("456 Test Ave")
        .creditCardDetails(cardDetails)
        .build();
  }

  // --- POST /order/place ---
  @Test
  void placeOrder_WhenSuccessful_ShouldReturnCreated() throws Exception {
    // Arrange
    Order createdOrder = Order.builder().id(1L).client(testClient).build();
    when(orderService.placeOrder(
        any(Long.class),
        any(),
        anyString(),
        anyString(),
        anyString(),
        anyString())).thenReturn(createdOrder);

    // Act & Assert
    mockMvc.perform(post("/order/place")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(placeRequest)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "http://localhost/order/1"));
  }

  @Test
  void placeOrder_WhenCartIsEmpty_ShouldReturnBadRequest() throws Exception {
    // Arrange
    when(orderService.placeOrder(
        any(Long.class),
        any(),
        anyString(),
        anyString(),
        anyString(),
        anyString())).thenThrow(new NoItemsInCartException());

    // Act & Assert
    mockMvc.perform(post("/order/place")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(placeRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("Cannot place an order with an empty cart.")));
  }

  @Test
  void placeOrder_WhenCardIsRejected_ShouldReturnBadRequest() throws Exception {
    // Arrange
    when(orderService.placeOrder(
        any(Long.class),
        any(),
        anyString(),
        anyString(),
        anyString(),
        anyString())).thenThrow(new TokenizationRejectedException());

    // Act & Assert
    mockMvc.perform(post("/order/place")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(placeRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("Credit card rejected. Try again.")));
  }

  @Test
  void placeOrder_WhenClientIsNotFound_ShouldReturnNotFound() throws Exception {
    // Arrange
    when(orderService.placeOrder(
        any(Long.class),
        any(),
        anyString(),
        anyString(),
        anyString(),
        anyString())).thenThrow(new NotFoundException("client"));

    // Act & Assert
    mockMvc.perform(post("/order/place")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(placeRequest)))
        .andExpect(status().isNotFound());
  }

  // --- GET /order/{clientId} ---
  @Test
  void getClientOrders_WhenOrdersExist_ShouldReturnOrders() throws Exception {
    // Arrange
    productRepository
        .save(Product.builder().sku("P1").name("PName").description("...").price(100.0).quantity(10).build());
    Order order = Order.builder()
        .id(1L)
        .client(testClient)
        .totalPrice(BigDecimal.valueOf(100.0))
        .status(Order.Status.COMPLETED)
        .deliveryAddress(testClient.getAddress())
        .cartItems(Collections.emptyList())
        .build();
    when(orderService.getOrdersOfClient(testClient.getId())).thenReturn(List.of(order));

    // Act & Assert
    mockMvc.perform(get("/order/" + testClient.getId())
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].totalPrice", is(100.0)))
        .andExpect(jsonPath("$[0].status", is("COMPLETED")));
  }

  @Test
  void getClientOrders_WhenNoOrdersExist_ShouldReturnEmptyList() throws Exception {
    // Arrange
    when(orderService.getOrdersOfClient(testClient.getId())).thenReturn(Collections.emptyList());

    // Act & Assert
    mockMvc.perform(get("/order/" + testClient.getId())
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }
}
