package com.daniel_montilla.reto_tecnico;

import com.daniel_montilla.reto_tecnico.dto.CartDTO;
import com.daniel_montilla.reto_tecnico.entity.CartItem;
import com.daniel_montilla.reto_tecnico.entity.Client;
import com.daniel_montilla.reto_tecnico.entity.Product;
import com.daniel_montilla.reto_tecnico.repository.CartItemRepository;
import com.daniel_montilla.reto_tecnico.repository.ClientRepository;
import com.daniel_montilla.reto_tecnico.repository.ProductRepository;
import com.daniel_montilla.reto_tecnico.service.ApiKeyAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CartControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ApiKeyAuthService apiKeyAuthService;

	private final String MOCK_API_KEY = "test-api-key";

	@BeforeEach
	void setUp() {
		when(apiKeyAuthService.isApiKeyValid(anyString())).thenReturn(true);
	}

	@Test
	void add_ShouldCreateANewCartItem() throws Exception {
		var client = clientRepository.save(Client.builder()
				.name("John Doe")
				.email("john.doe@example.com")
				.phone("1234567890")
				.address("123 Main St")
				.build());

		var product = productRepository.save(Product.builder()
				.name("Test Product")
				.sku("TEST-PRODUCT-123")
				.description("A test product description")
				.price(29.99)
				.quantity(100)
				.build());

		CartDTO.CreateRequest createRequest = CartDTO.CreateRequest.builder()
				.clientId(client.getId())
				.productId(product.getId())
				.quantity(1)
				.build();

		var result = mockMvc.perform(post("/cart/add")
				.header("Authorization", "Bearer " + MOCK_API_KEY)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
				.andExpect(status().isOk())
				.andReturn();

		var responseBody = result.getResponse().getContentAsString();
		var responseItem = objectMapper.readValue(responseBody, CartDTO.CartItemResponse.class);
		var newCartItemId = responseItem.getId();

		var dbItemOptional = cartItemRepository.findById(newCartItemId);

		assertTrue(dbItemOptional.isPresent(), "CartItem should be saved in the database");
		CartItem dbItem = dbItemOptional.get();
		assertEquals(createRequest.getQuantity(), dbItem.getQuantity());
		assertEquals(client.getId(), dbItem.getClient().getId());
		assertEquals(client.getId(), dbItem.getProduct().getId());
	}

	@Test
	void add_IfCartItemWithSameProductExists_ShouldJustUpdateTheExistingCartItem() throws Exception {
		var client = clientRepository.save(Client.builder()
				.name("John Doe")
				.email("john.doe@example.com")
				.phone("1234567890")
				.address("123 Main St")
				.build());

		var product = productRepository.save(Product.builder()
				.name("Test Product 1")
				.sku("TEST-PRODUCT-1")
				.description("A test product description")
				.price(29.99)
				.quantity(100)
				.build());

		CartDTO.CreateRequest createRequest1 = CartDTO.CreateRequest.builder()
				.clientId(client.getId())
				.productId(product.getId())
				.quantity(1)
				.build();

		var result1 = mockMvc.perform(post("/cart/add")
				.header("Authorization", "Bearer " + MOCK_API_KEY)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest1)))
				.andExpect(status().isOk())
				.andReturn();

		var responseBody1 = result1.getResponse().getContentAsString();
		var responseItem1 = objectMapper.readValue(responseBody1, CartDTO.CartItemResponse.class);
		var newCartItemId1 = responseItem1.getId();

		var prevItem = cartItemRepository.findById(newCartItemId1);

		assertTrue(prevItem.isPresent(), "CartItem should be saved in the database");
		CartItem prevDbItem = prevItem.get();
		assertEquals(createRequest1.getQuantity(), prevDbItem.getQuantity());
		assertEquals(client.getId(), prevDbItem.getClient().getId());
		assertEquals(client.getId(), prevDbItem.getProduct().getId());

		CartDTO.CreateRequest createRequest2 = CartDTO.CreateRequest.builder()
				.clientId(client.getId())
				.productId(product.getId())
				.quantity(1)
				.build();

		var result2 = mockMvc.perform(post("/cart/add")
				.header("Authorization", "Bearer " + MOCK_API_KEY)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest2)))
				.andExpect(status().isOk())
				.andReturn();

		var responseBody2 = result2.getResponse().getContentAsString();
		var responseItem2 = objectMapper.readValue(responseBody2, CartDTO.CartItemResponse.class);
		var newCartItemId2 = responseItem2.getId();

		var nextItem = cartItemRepository.findById(newCartItemId2);
		assertTrue(nextItem.isPresent(), "CartItem should be saved in the database");
		CartItem nextDbItem = nextItem.get();
		assertEquals(nextDbItem.getQuantity(), createRequest1.getQuantity() + createRequest2.getQuantity());
		assertEquals(client.getId(), nextDbItem.getClient().getId());
		assertEquals(client.getId(), nextDbItem.getProduct().getId());

		assertEquals(nextDbItem.getId(), prevDbItem.getId());
	}

	@Test
	void remove_ShouldDecreaseQuantity_WhenPartialRemove() throws Exception {

		var client = clientRepository.save(Client.builder()
				.name("Test Client")
				.email("test@test.com")
				.phone("1234567890")
				.address("123 Main St")
				.build());

		var product = productRepository.save(Product.builder()
				.name("Test Product")
				.description("Test description")
				.sku("TP-01")
				.price(10.0)
				.quantity(100)
				.build());

		cartItemRepository.save(CartItem.builder()
				.client(client)
				.product(product)
				.quantity(5)
				.build());

		CartDTO.CreateRequest removeRequest = CartDTO.CreateRequest.builder()
				.clientId(client.getId())
				.productId(product.getId())
				.quantity(2)
				.build();

		mockMvc.perform(post("/cart/remove")
				.header("Authorization", "Bearer " + MOCK_API_KEY)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(removeRequest)))
				.andExpect(status().isOk());

		var updatedItemOptional = cartItemRepository.findByClientIdAndProductIdAndFulfilledFalse(client.getId(),
				product.getId());
		assertTrue(updatedItemOptional.isPresent(), "Cart item should still exist.");
		assertEquals(3, updatedItemOptional.get().getQuantity(),
				"Quantity should be decreased to 3.");
	}

	@Test
	void remove_ShouldDeleteItem_WhenRemovingAllUnits() throws Exception {
		var client = clientRepository.save(Client.builder()
				.name("Test Client")
				.email("test@test.com")
				.phone("1234567890")
				.address("123 Main St")
				.build());

		var product = productRepository.save(Product.builder()
				.name("Test Product")
				.description("Test description")
				.sku("TP-02")
				.price(10.0)
				.quantity(100)
				.build());

		var cartItem = cartItemRepository.save(CartItem.builder()
				.client(client)
				.product(product)
				.quantity(2)
				.build());

		CartDTO.CreateRequest removeRequest = CartDTO.CreateRequest.builder()
				.clientId(client.getId())
				.productId(product.getId())
				.quantity(2)
				.build();

		mockMvc.perform(post("/cart/remove")
				.header("Authorization", "Bearer " + MOCK_API_KEY)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(removeRequest)))
				.andExpect(status().isOk())
				.andExpect(content().string("null"));

		var deletedItemOptional = cartItemRepository.findById(cartItem.getId());
		assertTrue(deletedItemOptional.isEmpty(), "Cart item should be deleted.");
	}

	@Test
	void clearCart_ShouldRemoveAllItemsForAClient() throws Exception {
		var client = clientRepository.save(Client.builder()
				.name("Test Client")
				.email("test@test.com")
				.phone("1234567890")
				.address("123 Main St")
				.build());

		var product1 = productRepository.save(Product.builder()
				.name("Product 1")
				.description("Test description")
				.sku("P1")
				.price(10.0)
				.quantity(100)
				.build());

		var product2 = productRepository.save(Product.builder()
				.name("Product 2")
				.description("Test description")
				.sku("P2")
				.price(20.0)
				.quantity(100)
				.build());

		cartItemRepository.save(CartItem.builder().client(client).product(product1).quantity(1).build());
		cartItemRepository.save(CartItem.builder().client(client).product(product2).quantity(2).build());

		assertEquals(2, cartItemRepository.findAllByClientIdAndFulfilledFalse(client.getId()).size(),
				"Cart should have 2 items before clearing.");

		mockMvc.perform(delete("/cart/clear/" + client.getId())
				.header("Authorization", "Bearer " + MOCK_API_KEY))
				.andExpect(status().isNoContent());

		List<CartItem> itemsAfterClear = cartItemRepository.findAllByClientIdAndFulfilledFalse(client.getId());
		assertTrue(itemsAfterClear.isEmpty(), "Cart should be empty after clearing.");
	}
}