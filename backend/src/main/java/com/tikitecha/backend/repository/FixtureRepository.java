package com.tikitecha.backend.repository;

import com.tikitecha.backend.model.Fixture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixtureRepository extends JpaRepository<Fixture, Integer> {}