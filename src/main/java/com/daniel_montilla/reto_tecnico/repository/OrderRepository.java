package com.daniel_montilla.reto_tecnico.repository;

import com.daniel_montilla.reto_tecnico.entity.Order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  public List<Order> findAllByClientId(Long clientId);
}
