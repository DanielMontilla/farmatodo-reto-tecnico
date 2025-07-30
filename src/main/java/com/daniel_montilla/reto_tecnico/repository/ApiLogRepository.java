package com.daniel_montilla.reto_tecnico.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.daniel_montilla.reto_tecnico.entity.ApiLog;

@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, String> {
}