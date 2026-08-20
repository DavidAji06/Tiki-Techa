package com.tikitecha.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tikitecha.backend.dto.SetLineupRequest;
import com.tikitecha.backend.model.Squad;
import com.tikitecha.backend.model.SquadPlayer;
import com.tikitecha.backend.model.User;
import com.tikitecha.backend.repository.SquadPlayerRepository;
import com.tikitecha.backend.repository.SquadRepository;
import com.tikitecha.backend.repository.UserRepository;
import com.tikitecha.backend.service.TransferService;

@RestController
@RequestMapping("/api/squads")
public class SquadController {

    private final SquadRepository squadRepository;
    private final UserRepository userRepository;
    private final SquadPlayerRepository squadPlayerRepository;
    private final TransferService transferService;

    public SquadController(SquadRepository squadRepository, UserRepository userRepository, SquadPlayerRepository squadPlayerRepository, TransferService transferService) {
        this.squadRepository = squadRepository;
        this.userRepository = userRepository;
        this.squadPlayerRepository = squadPlayerRepository;
        this.transferService = transferService;
    }

    @Transactional
    @PostMapping
    public ResponseEntity<?> createSquad() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        Squad existing = squadRepository.findByUserId(user.getId());
        if (existing != null) {
            return ResponseEntity.status(409).body("Squad already exists for this user");
        }

        Squad squad = new Squad();
        squad.setUser(user);
        squadRepository.save(squad);

        return ResponseEntity.status(201).body(squad);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMySquad() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        Squad squad = squadRepository.findSquadByUserId(user.getId())
                .orElse(null);

        if (squad == null) {
            return ResponseEntity.status(404).body("No squad found for this user");
        }

        List<SquadPlayer> players = squadPlayerRepository.findBySquadId(squad.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("squad", squad);
        response.put("players", players);

        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/lineup")
    public ResponseEntity<?> setLineup(@RequestBody SetLineupRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        try {
            transferService.setLineup(user.getId(), request.getStartingPlayerIds());
            return ResponseEntity.ok("Lineup updated successfully");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}