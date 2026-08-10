package com.tikitecha.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity //tells hibernate that this class maps to a table
@Table(name = "users") 

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //marks primary key and auto increment
    private Long id;

    @Column(nullable = false, unique = true) //db level constraing must not be null + must be unique
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    final LocalDateTime createdAt = LocalDateTime.now();

    protected User() {}
    
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }


    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }

}