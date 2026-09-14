package com.tikitecha.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tikitecha.backend.dto.SimulateMatchRequest;
import com.tikitecha.backend.model.Match;
import com.tikitecha.backend.model.MatchEvent;
import com.tikitecha.backend.repository.MatchEventRepository;
import com.tikitecha.backend.service.MatchService;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;
    private final MatchEventRepository matchEventRepository;

    public MatchController(MatchService matchService, MatchEventRepository matchEventRepository) {
        this.matchService = matchService;
        this.matchEventRepository = matchEventRepository;
    }

    @PostMapping
    public ResponseEntity<?> simulateMatch(@RequestBody SimulateMatchRequest request) {
        try {
            Match match = matchService.simulateMatch(request.getHomeSquadId(), request.getAwaySquadId());
            return ResponseEntity.ok(match);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/events")
    public List<MatchEvent> getMatchEvents(@PathVariable Long id) {
        return matchEventRepository.findByMatchIdOrderByMinuteAsc(id);
    }
}