package com.daniel_montilla.reto_tecnico.repository;

import com.daniel_montilla.reto_tecnico.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
}