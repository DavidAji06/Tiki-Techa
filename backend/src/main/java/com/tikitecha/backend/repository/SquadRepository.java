package com.tikitecha.backend.repository;

import com.tikitecha.backend.model.Squad;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;

public interface SquadRepository extends JpaRepository<Squad, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Squad findByUserId(Long userId);

    Optional<Squad> findSquadByUserId(Long userId);
}