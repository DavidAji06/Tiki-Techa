package com.tikitecha.backend.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tikitecha.backend.model.Player;
import com.tikitecha.backend.repository.PlayerRepository;
import com.tikitecha.backend.specification.PlayerSpecification;


@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerRepository playerRepository;

    public PlayerController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @GetMapping
public List<Player> getPlayers(
        @RequestParam(required = false) Integer positionId,
        @RequestParam(required = false) Integer teamId,
        @RequestParam(required = false) BigDecimal maxCost
) {
    ArrayList<Specification<Player>> filters = new ArrayList<>();

    if (positionId != null) {
        filters.add(PlayerSpecification.hasPosition(positionId));
    }
    if (teamId != null) {
        filters.add(PlayerSpecification.hasTeam(teamId));
    }
    if (maxCost != null) {
        filters.add(PlayerSpecification.costLessThanOrEqual(maxCost));
    }

    if (filters.isEmpty()) {
        return playerRepository.findAll();
    }

    Specification<Player> spec = filters.get(0);
    for (int i = 1; i < filters.size(); i++) {
        spec = spec.and(filters.get(i));
    }

    return playerRepository.findAll(spec);
}
}