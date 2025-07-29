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
    var client = new Client();

    client.setName(body.getName());
    client.setEmail(body.getEmail());
    client.setPhone(body.getPhone());
    client.setAddress(body.getAddress());

    try {
      Client savedClient = clientRepository.save(client);
      return ResponseEntity.status(HttpStatus.CREATED).body(savedClient);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
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