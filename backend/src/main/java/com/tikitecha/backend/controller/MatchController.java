package com.tikitecha.backend.controller;

import com.tikitecha.backend.dto.SimulateMatchRequest;
import com.tikitecha.backend.model.Match;
import com.tikitecha.backend.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
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
}