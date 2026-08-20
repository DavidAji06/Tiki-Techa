package com.tikitecha.backend.repository;

import com.tikitecha.backend.model.MatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {
    List<MatchEvent> findByMatchIdOrderByMinuteAsc(Long matchId);
}