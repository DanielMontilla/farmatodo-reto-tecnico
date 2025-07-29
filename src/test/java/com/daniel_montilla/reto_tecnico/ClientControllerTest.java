package com.daniel_montilla.reto_tecnico;

import com.daniel_montilla.reto_tecnico.config.SecurityConfig;
import com.daniel_montilla.reto_tecnico.controller.ClientController;
import com.daniel_montilla.reto_tecnico.dto.ClientsDTO;
import com.daniel_montilla.reto_tecnico.entity.Client;
import com.daniel_montilla.reto_tecnico.repository.ClientRepository;
import com.daniel_montilla.reto_tecnico.filter.ApiKeyAuthFilter;
import com.daniel_montilla.reto_tecnico.service.ApiKeyAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
@Import({ SecurityConfig.class, ApiKeyAuthFilter.class })
class ClientControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ClientRepository clientRepository;

  @MockitoBean
  private ApiKeyAuthService apiKeyAuthService;

  @Autowired
  private ObjectMapper objectMapper;

  private Client client1;
  private Client client2;
  private final String MOCK_API_KEY = "test-api-key";

  @BeforeEach
  void setUp() {
    when(apiKeyAuthService.isApiKeyValid(anyString())).thenReturn(true);

    client1 = new Client();
    client1.setId(1L);
    client1.setName("John Doe");
    client1.setEmail("john.doe@example.com");
    client1.setPhone("1234567890");
    client1.setAddress("123 Main St");

    client2 = new Client();
    client2.setId(2L);
    client2.setName("Jane Smith");
    client2.setEmail("jane.smith@example.com");
    client2.setPhone("0987654321");
    client2.setAddress("456 Oak Ave");
  }

  @Test
  void createClient_withValidData_shouldReturnCreatedClient() throws Exception {
    ClientsDTO.CreateRequest createRequest = new ClientsDTO.CreateRequest();
    createRequest.setName("John Doe");
    createRequest.setEmail("john.doe@example.com");
    createRequest.setPhone("1234567890");
    createRequest.setAddress("123 Main St");

    when(clientRepository.save(any(Client.class))).thenReturn(client1);

    mockMvc.perform(post("/clients")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is("John Doe")))
        .andExpect(jsonPath("$.email", is("john.doe@example.com")));
  }

  @Test
  void createClient_withBlankName_shouldReturnBadRequest() throws Exception {
    ClientsDTO.CreateRequest createRequest = new ClientsDTO.CreateRequest();
    createRequest.setName(""); // Invalid - blank name
    createRequest.setEmail("john.doe@example.com");
    createRequest.setPhone("1234567890");
    createRequest.setAddress("123 Main St");

    mockMvc.perform(post("/clients")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createClient_withInvalidEmail_shouldReturnBadRequest() throws Exception {
    ClientsDTO.CreateRequest createRequest = new ClientsDTO.CreateRequest();
    createRequest.setName("John Doe");
    createRequest.setEmail("invalid-email"); // Invalid email format
    createRequest.setPhone("1234567890");
    createRequest.setAddress("123 Main St");

    mockMvc.perform(post("/clients")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createClient_withShortPhone_shouldReturnBadRequest() throws Exception {
    ClientsDTO.CreateRequest createRequest = new ClientsDTO.CreateRequest();
    createRequest.setName("John Doe");
    createRequest.setEmail("john.doe@example.com");
    createRequest.setPhone("123"); // Too short - less than 7 digits
    createRequest.setAddress("123 Main St");

    mockMvc.perform(post("/clients")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createClient_withoutAddress_shouldReturnCreatedClient() throws Exception {
    ClientsDTO.CreateRequest createRequest = new ClientsDTO.CreateRequest();
    createRequest.setName("John Doe");
    createRequest.setEmail("john.doe@example.com");
    createRequest.setPhone("1234567890");
    // No address - this should be valid since address is optional

    when(clientRepository.save(any(Client.class))).thenReturn(client1);

    mockMvc.perform(post("/clients")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isCreated());
  }

  @Test
  void createClient_whenRepositoryThrowsException_shouldReturnBadRequest() throws Exception {
    ClientsDTO.CreateRequest createRequest = new ClientsDTO.CreateRequest();
    createRequest.setName("John Doe");
    createRequest.setEmail("john.doe@example.com");
    createRequest.setPhone("1234567890");
    createRequest.setAddress("123 Main St");

    when(clientRepository.save(any(Client.class))).thenThrow(new RuntimeException("Database error"));

    mockMvc.perform(post("/clients")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getAllClients_shouldReturnListOfClients() throws Exception {
    List<Client> clients = Arrays.asList(client1, client2);
    when(clientRepository.findAll()).thenReturn(clients);

    mockMvc.perform(get("/clients")
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name", is("John Doe")));
  }

  @Test
  void getClientById_whenClientExists_shouldReturnClient() throws Exception {
    when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));

    mockMvc.perform(get("/clients/1")
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)));
  }

  @Test
  void updateClient_withValidData_shouldReturnUpdatedClient() throws Exception {
    ClientsDTO.UpdateRequest updateRequest = new ClientsDTO.UpdateRequest();
    updateRequest.setName("Johnathan Doe");
    updateRequest.setEmail("john.doe.new@example.com");
    updateRequest.setPhone("1112223333");
    updateRequest.setAddress("123 New St");

    Client updatedClient = new Client();
    updatedClient.setId(1L);
    updatedClient.setName("Johnathan Doe");
    updatedClient.setEmail("john.doe.new@example.com");
    updatedClient.setPhone("1112223333");
    updatedClient.setAddress("123 New St");

    when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));
    when(clientRepository.save(any(Client.class))).thenReturn(updatedClient);

    mockMvc.perform(put("/clients/1")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is("Johnathan Doe")))
        .andExpect(jsonPath("$.email", is("john.doe.new@example.com")));
  }

  @Test
  void updateClient_withNonExistentId_shouldReturnNotFound() throws Exception {
    ClientsDTO.UpdateRequest updateRequest = new ClientsDTO.UpdateRequest();
    updateRequest.setName("Johnathan Doe");
    updateRequest.setEmail("john.doe.new@example.com");
    updateRequest.setPhone("1112223333");
    updateRequest.setAddress("123 New St");

    when(clientRepository.findById(999L)).thenReturn(Optional.empty());

    mockMvc.perform(put("/clients/999")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateClient_withBlankName_shouldReturnBadRequest() throws Exception {
    ClientsDTO.UpdateRequest updateRequest = new ClientsDTO.UpdateRequest();
    updateRequest.setName(""); // Invalid - blank name
    updateRequest.setEmail("john.doe@example.com");
    updateRequest.setPhone("1234567890");
    updateRequest.setAddress("123 Main St");

    mockMvc.perform(put("/clients/1")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateClient_withInvalidEmail_shouldReturnBadRequest() throws Exception {
    ClientsDTO.UpdateRequest updateRequest = new ClientsDTO.UpdateRequest();
    updateRequest.setName("John Doe");
    updateRequest.setEmail("invalid-email"); // Invalid email format
    updateRequest.setPhone("1234567890");
    updateRequest.setAddress("123 Main St");

    mockMvc.perform(put("/clients/1")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateClient_withShortPhone_shouldReturnBadRequest() throws Exception {
    ClientsDTO.UpdateRequest updateRequest = new ClientsDTO.UpdateRequest();
    updateRequest.setName("John Doe");
    updateRequest.setEmail("john.doe@example.com");
    updateRequest.setPhone("123"); // Too short - less than 7 digits
    updateRequest.setAddress("123 Main St");

    mockMvc.perform(put("/clients/1")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateClient_withOptionalFieldsOnly_shouldReturnUpdatedClient() throws Exception {
    ClientsDTO.UpdateRequest updateRequest = new ClientsDTO.UpdateRequest();
    updateRequest.setName("Johnathan Doe");
    updateRequest.setAddress("123 New St");
    // Email and phone are not required for updates

    Client updatedClient = new Client();
    updatedClient.setId(1L);
    updatedClient.setName("Johnathan Doe");
    updatedClient.setAddress("123 New St");

    when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));
    when(clientRepository.save(any(Client.class))).thenReturn(updatedClient);

    mockMvc.perform(put("/clients/1")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is("Johnathan Doe")));
  }

  @Test
  void deleteClient_whenClientExists_shouldReturnNoContent() throws Exception {
    when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));
    doNothing().when(clientRepository).delete(client1);

    mockMvc.perform(delete("/clients/1")
        .header("Authorization", "Bearer " + MOCK_API_KEY))
        .andExpect(status().isNoContent());
  }

  @Test
  void createClient_withDuplicateEmail_shouldReturnBadRequest() throws Exception {
    ClientsDTO.CreateRequest createRequest = new ClientsDTO.CreateRequest();
    createRequest.setName("Jane Smith");
    createRequest.setEmail("john.doe@example.com"); // Same email as existing client
    createRequest.setPhone("9876543210");
    createRequest.setAddress("456 Oak Ave");

    // Simulate database unique constraint violation
    when(clientRepository.save(any(Client.class)))
        .thenThrow(new DataIntegrityViolationException("Email already exists"));

    mockMvc.perform(post("/clients")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createClient_withDuplicatePhone_shouldReturnBadRequest() throws Exception {
    ClientsDTO.CreateRequest createRequest = new ClientsDTO.CreateRequest();
    createRequest.setName("Jane Smith");
    createRequest.setEmail("jane.smith@example.com");
    createRequest.setPhone("1234567890"); // Same phone as existing client
    createRequest.setAddress("456 Oak Ave");

    // Simulate database unique constraint violation
    when(clientRepository.save(any(Client.class)))
        .thenThrow(new DataIntegrityViolationException("Phone already exists"));

    mockMvc.perform(post("/clients")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateClient_withDuplicateEmail_shouldReturnBadRequest() throws Exception {
    ClientsDTO.UpdateRequest updateRequest = new ClientsDTO.UpdateRequest();
    updateRequest.setName("John Doe Updated");
    updateRequest.setEmail("jane.smith@example.com"); // Trying to use another client's email
    updateRequest.setPhone("1234567890");
    updateRequest.setAddress("123 Main St Updated");

    when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));
    // Simulate database unique constraint violation when trying to save with
    // duplicate email
    when(clientRepository.save(any(Client.class)))
        .thenThrow(new DataIntegrityViolationException("Email already exists"));

    mockMvc.perform(put("/clients/1")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateClient_withDuplicatePhone_shouldReturnBadRequest() throws Exception {
    ClientsDTO.UpdateRequest updateRequest = new ClientsDTO.UpdateRequest();
    updateRequest.setName("John Doe Updated");
    updateRequest.setEmail("john.doe@example.com");
    updateRequest.setPhone("0987654321"); // Trying to use another client's phone
    updateRequest.setAddress("123 Main St Updated");

    when(clientRepository.findById(1L)).thenReturn(Optional.of(client1));
    when(clientRepository.save(any(Client.class)))
        .thenThrow(new DataIntegrityViolationException("Phone already exists"));

    mockMvc.perform(put("/clients/1")
        .header("Authorization", "Bearer " + MOCK_API_KEY)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isBadRequest());
  }
}
