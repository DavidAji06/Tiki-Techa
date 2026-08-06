package com.tikitecha.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")

public class HealthController {
    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            //do some query to check if  database is connected
            Object result = entityManager.createNativeQuery("SELECT 1").getSingleResult();

            response.put("status", "UP");
            response.put("database", "CONNECTED");
            response.put("db_test_query_result", result);
            response.put("message", "Tiki-Techa Java backend and PostgreSQL are running successfully.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("database", "DISCONNECTED");
            response.put("message", e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }
}