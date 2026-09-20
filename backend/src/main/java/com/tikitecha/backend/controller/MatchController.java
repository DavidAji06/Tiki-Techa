package com.tikitecha.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.tikitecha.backend.dto.SimulateMatchRequest;
import com.tikitecha.backend.dto.SimulateVsAiRequest;
import com.tikitecha.backend.model.Match;
import com.tikitecha.backend.model.MatchEvent;
import com.tikitecha.backend.repository.MatchEventRepository;
import com.tikitecha.backend.service.MatchService;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;
    private final MatchEventRepository matchEventRepository;

    @Value("${ai.manager.squad-id}")
    private Long aiManagerSquadId;

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

    @PostMapping("/vs-ai")
    public ResponseEntity<?> simulateVsAi(@RequestBody SimulateVsAiRequest request) {
        try {
            Match match = matchService.simulateVsAi(request.getUserSquadId(), aiManagerSquadId);
            return ResponseEntity.ok(match);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/events")
    public List<MatchEvent> getMatchEvents(@PathVariable Long id) {
        return matchEventRepository.findByMatchIdOrderByMinuteAsc(id);
    }

    @GetMapping("/vs-ai/benchmark")
    public ResponseEntity<?> benchmarkVsAi(
            @RequestParam Long userSquadId,
            @RequestParam(defaultValue = "50") int runs
    ) {
        try {
            return ResponseEntity.ok(matchService.benchmarkVsAi(userSquadId, aiManagerSquadId, runs));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}