package com.tikitecha.backend.repository;

import com.tikitecha.backend.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PlayerRepository extends JpaRepository<Player, Integer>, JpaSpecificationExecutor<Player> {
    List<Player> findByPositionId(Integer positionId);
}