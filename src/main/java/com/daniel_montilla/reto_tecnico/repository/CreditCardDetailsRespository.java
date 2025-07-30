package com.daniel_montilla.reto_tecnico.repository;

import com.daniel_montilla.reto_tecnico.entity.CreditCardDetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardDetailsRespository extends JpaRepository<CreditCardDetails, String> {
}
