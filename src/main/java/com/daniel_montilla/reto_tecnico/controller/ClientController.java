package com.daniel_montilla.reto_tecnico.controller;

import com.daniel_montilla.reto_tecnico.dto.ClientsDTO;
import com.daniel_montilla.reto_tecnico.entity.Client;
import com.daniel_montilla.reto_tecnico.repository.ClientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/clients")
public class ClientController {

  private final ClientRepository clientRepository;

  public ClientController(ClientRepository clientRepository) {
    this.clientRepository = clientRepository;
  }

  @GetMapping
  public ResponseEntity<List<Client>> getAllClients() {
    List<Client> clients = clientRepository.findAll();
    return ResponseEntity.ok(clients);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Client> getClientById(@PathVariable Long id) {
    Optional<Client> client = clientRepository.findById(id);
    return client.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Client> createClient(@Valid @RequestBody ClientsDTO.CreateRequest body) {
    Client client = clientRepository.save(Client.builder()
        .name(body.getName())
        .email(body.getEmail())
        .phone(body.getPhone())
        .address(body.getAddress())
        .build());
    return ResponseEntity.status(HttpStatus.CREATED).body(client);
  }

  @PostMapping("/batch")
  public ResponseEntity<List<Client>> createClients(@Valid @RequestBody List<ClientsDTO.CreateRequest> body) {
    List<Client> clients = clientRepository.saveAll(
        body
            .stream()
            .map(item -> Client.builder()
                .name(item.getName())
                .email(item.getEmail())
                .phone(item.getPhone())
                .address(item.getAddress())
                .build())
            .toList());

    return ResponseEntity.status(HttpStatus.CREATED).body(clients);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Client> updateClient(@PathVariable Long id, @Valid @RequestBody ClientsDTO.UpdateRequest body) {
    return clientRepository.findById(id)
        .map(existingClient -> {
          existingClient.setName(body.getName());
          existingClient.setEmail(body.getEmail());
          existingClient.setPhone(body.getPhone());
          existingClient.setAddress(body.getAddress());
          Client updatedClient = clientRepository.save(existingClient);
          return ResponseEntity.ok(updatedClient);
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
    return clientRepository.findById(id)
        .map(client -> {
          clientRepository.delete(client);
          return ResponseEntity.noContent().<Void>build();
        })
        .orElse(ResponseEntity.notFound().build());
  }
}