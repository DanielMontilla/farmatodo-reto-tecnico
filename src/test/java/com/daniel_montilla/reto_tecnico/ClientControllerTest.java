package com.daniel_montilla.reto_tecnico;

import com.daniel_montilla.reto_tecnico.dto.ClientsDTO;
import com.daniel_montilla.reto_tecnico.entity.Client;
import com.daniel_montilla.reto_tecnico.repository.ClientRepository;
import com.daniel_montilla.reto_tecnico.service.ApiKeyAuthService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ClientControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ApiKeyAuthService apiKeyAuthService;

	private final String MOCK_API_KEY = "test-api-key";

	@BeforeEach
	void setUp() {
		// Mock the API key validation to always return true for tests
		when(apiKeyAuthService.isApiKeyValid(anyString())).thenReturn(true);
	}

	// --- GET /clients ---
	@Test
	void getAllClients_ShouldReturnListOfClients() throws Exception {
		// Arrange: Create and save two clients
		clientRepository.save(Client.builder().name("John Doe").email("john.doe@example.com").phone("111222333").build());
		clientRepository
				.save(Client.builder().name("Jane Smith").email("jane.smith@example.com").phone("444555666").build());

		// Act & Assert
		mockMvc.perform(get("/clients")
				.header("Authorization", "Bearer " + MOCK_API_KEY))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].name", is("John Doe")))
				.andExpect(jsonPath("$[1].name", is("Jane Smith")));
	}

	// --- GET /clients/{id} ---
	@Test
	void getClientById_WhenClientExists_ShouldReturnClient() throws Exception {
		// Arrange: Create and save a client
		Client client = clientRepository
				.save(Client.builder().name("Single Client").email("single@client.com").phone("987654321").build());

		// Act & Assert
		mockMvc.perform(get("/clients/" + client.getId())
				.header("Authorization", "Bearer " + MOCK_API_KEY))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id", is(client.getId().intValue())))
				.andExpect(jsonPath("$.name", is("Single Client")));
	}

	@Test
	void getClientById_WhenClientDoesNotExist_ShouldReturnNotFound() throws Exception {
		// Act & Assert
		mockMvc.perform(get("/clients/999")
				.header("Authorization", "Bearer " + MOCK_API_KEY))
				.andExpect(status().isNotFound());
	}

	// --- POST /clients ---
	@Test
	void createClient_WithValidData_ShouldCreateAndReturnClient() throws Exception {
		// Arrange
		ClientsDTO.CreateRequest createRequest = new ClientsDTO.CreateRequest();
		createRequest.setName("New Client");
		createRequest.setEmail("new.client@example.com");
		createRequest.setPhone("123123123");
		createRequest.setAddress("123 New St");

		// Act & Assert
		mockMvc.perform(post("/clients")
				.header("Authorization", "Bearer " + MOCK_API_KEY)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name", is("New Client")))
				.andExpect(jsonPath("$.email", is("new.client@example.com")));
	}

	// --- POST /clients/batch ---
	@Test
	void createClients_WithValidData_ShouldCreateAndReturnListOfClients() throws Exception {
		// Arrange
		ClientsDTO.CreateRequest client1 = new ClientsDTO.CreateRequest();
		client1.setName("Batch Client 1");
		client1.setEmail("batch1@example.com");
		client1.setPhone("1000001");

		ClientsDTO.CreateRequest client2 = new ClientsDTO.CreateRequest();
		client2.setName("Batch Client 2");
		client2.setEmail("batch2@example.com");
		client2.setPhone("1000002");

		List<ClientsDTO.CreateRequest> batchRequest = Arrays.asList(client1, client2);

		// Act & Assert
		mockMvc.perform(post("/clients/batch")
				.header("Authorization", "Bearer " + MOCK_API_KEY)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(batchRequest)))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].name", is("Batch Client 1")))
				.andExpect(jsonPath("$[1].name", is("Batch Client 2")));
	}

	// --- PUT /clients/{id} ---
	@Test
	void updateClient_WhenClientExists_ShouldUpdateAndReturnClient() throws Exception {
		// Arrange: Create an initial client
		Client existingClient = clientRepository
				.save(Client.builder().name("Original Name").email("original@email.com").phone("5555555").build());

		// Create an update request DTO
		ClientsDTO.UpdateRequest updateRequest = new ClientsDTO.UpdateRequest();
		updateRequest.setName("Updated Name");
		updateRequest.setEmail("updated@email.com");
		updateRequest.setPhone("6666666");
		updateRequest.setAddress("Updated Address");

		// Act & Assert
		mockMvc.perform(put("/clients/" + existingClient.getId())
				.header("Authorization", "Bearer " + MOCK_API_KEY)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(existingClient.getId().intValue())))
				.andExpect(jsonPath("$.name", is("Updated Name")))
				.andExpect(jsonPath("$.email", is("updated@email.com")));
	}

	@Test
	void updateClient_WhenClientDoesNotExist_ShouldReturnNotFound() throws Exception {
		// Arrange
		ClientsDTO.UpdateRequest updateRequest = new ClientsDTO.UpdateRequest();
		updateRequest.setName("Doesn't Matter");
		updateRequest.setEmail("no@matter.com");
		updateRequest.setPhone("1234567");

		// Act & Assert
		mockMvc.perform(put("/clients/999")
				.header("Authorization", "Bearer " + MOCK_API_KEY)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isNotFound());
	}

	// --- DELETE /clients/{id} ---
	@Test
	void deleteClient_WhenClientExists_ShouldDeleteClientAndReturnNoContent() throws Exception {
		// Arrange: Create a client to be deleted
		Client clientToDelete = clientRepository
				.save(Client.builder().name("To Be Deleted").email("delete@me.com").phone("0000000").build());
		Long clientId = clientToDelete.getId();

		// Act & Assert
		mockMvc.perform(delete("/clients/" + clientId)
				.header("Authorization", "Bearer " + MOCK_API_KEY))
				.andExpect(status().isNoContent());

		// Verify the client was actually deleted from the database
		assertFalse(clientRepository.findById(clientId).isPresent());
	}

	@Test
	void deleteClient_WhenClientDoesNotExist_ShouldReturnNotFound() throws Exception {
		// Act & Assert
		mockMvc.perform(delete("/clients/999")
				.header("Authorization", "Bearer " + MOCK_API_KEY))
				.andExpect(status().isNotFound());
	}
}