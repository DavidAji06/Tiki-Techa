package com.tikitecha.backend.repository;

import com.tikitecha.backend.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface TeamRepository extends JpaRepository<Team, Integer> {
    Optional<Team> findByName(String name);
}