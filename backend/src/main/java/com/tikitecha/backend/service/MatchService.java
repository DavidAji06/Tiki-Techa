package com.tikitecha.backend.service;

import com.tikitecha.backend.model.*;
import com.tikitecha.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.List;

@Service
public class MatchService {

    private final SquadPlayerRepository squadPlayerRepository;
    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private static final double BASE_RATE = 1.3;
    private static final double MIN_RATING = 0.1;
    private static final Random random = new Random();
    private final SquadRepository squadRepository;

    public MatchService(
            SquadPlayerRepository squadPlayerRepository,
            MatchRepository matchRepository,
            MatchEventRepository matchEventRepository,
            SquadRepository squadRepository
    ) {
        this.squadPlayerRepository = squadPlayerRepository;
        this.matchRepository = matchRepository;
        this.matchEventRepository = matchEventRepository;
        this.squadRepository = squadRepository;
    }

    private double calculateAttackRating(List<SquadPlayer> startingXI) {
    return startingXI.stream()
            .filter(sp -> sp.getPlayer().getPositionId() == 3 || sp.getPlayer().getPositionId() == 4)
            .mapToDouble(sp -> sp.getPlayer().getGoalsScored() + sp.getPlayer().getAssists())
            .average()
            .orElse(0.0);
    }

    private double calculateDefenseRating(List<SquadPlayer> startingXI) {
        List<SquadPlayer> defenders = startingXI.stream()
                .filter(sp -> sp.getPlayer().getPositionId() == 1 || sp.getPlayer().getPositionId() == 2)
                .toList();

        double avgCleanSheets = defenders.stream()
                .mapToDouble(sp -> sp.getPlayer().getCleanSheets())
                .average()
                .orElse(0.0);

        double avgGoalsConceded = defenders.stream()
                .mapToDouble(sp -> sp.getPlayer().getGoalsConceded())
                .average()
                .orElse(0.0);

        return avgCleanSheets - (avgGoalsConceded * 0.1); //0.1 just a guess value, just to keep clean sheets more weighted but may change
    }

    private double calculateExpectedGoals(double attackRating, double opponentDefenseRating) {
        double safeDefense = Math.max(opponentDefenseRating, MIN_RATING);
        return BASE_RATE * (attackRating / safeDefense);
        }

        private int samplePoissonGoals(double lambda) {
        double threshold = Math.exp(-lambda);
        int goals = 0;
        double product = 1.0;

        do {
            goals++;
            product *= random.nextDouble();
        } while (product > threshold);

        return goals - 1;
    }

    @Transactional
    public Match simulateMatch(Long homeSquadId, Long awaySquadId) {
        List<SquadPlayer> homeStarters = squadPlayerRepository.findBySquadId(homeSquadId).stream()
                .filter(SquadPlayer::isStarting)
                .toList();

        List<SquadPlayer> awayStarters = squadPlayerRepository.findBySquadId(awaySquadId).stream()
                .filter(SquadPlayer::isStarting)
                .toList();

        Squad homeSquad = squadRepository.findById(homeSquadId)
        .orElseThrow(() -> new IllegalArgumentException("Home squad not found"));

        Squad awaySquad = squadRepository.findById(awaySquadId)
        .orElseThrow(() -> new IllegalArgumentException("Away squad not found"));


        double homeAttack = calculateAttackRating(homeStarters);
        double homeDefense = calculateDefenseRating(homeStarters);
        double awayAttack = calculateAttackRating(awayStarters);
        double awayDefense = calculateDefenseRating(awayStarters);

        double homeExpectedGoals = calculateExpectedGoals(homeAttack, awayDefense);
        double awayExpectedGoals = calculateExpectedGoals(awayAttack, homeDefense);

        int homeGoals = samplePoissonGoals(homeExpectedGoals);
        int awayGoals = samplePoissonGoals(awayExpectedGoals);

        // build and save the Match itself
        Match match = new Match();
        match.setHomeSquad(homeSquad);
        match.setAwaySquad(awaySquad);
        match.setHomeScore(homeGoals);
        match.setAwayScore(awayGoals);
        match.setPlayedAt(java.time.LocalDateTime.now());
        matchRepository.save(match);

        generateMatchEvents(match, homeGoals, awayGoals, homeStarters, awayStarters);

        return match;
    }

    private void generateMatchEvents(Match match, int homeGoals, int awayGoals, List<SquadPlayer> homeStarters, List<SquadPlayer> awayStarters) {
    generateGoalEvents(match, homeGoals, homeStarters);
    generateGoalEvents(match, awayGoals, awayStarters);
    generateCardEvents(match, homeStarters);
    generateCardEvents(match, awayStarters);
}

    private void generateGoalEvents(Match match, int goalCount, List<SquadPlayer> starters) {
        List<SquadPlayer> attackers = starters.stream()
                .filter(sp -> sp.getPlayer().getPositionId() == 3 || sp.getPlayer().getPositionId() == 4)
                .toList();

        if (attackers.isEmpty()) {
            return; // should never happen but just in case
        }

        for (int i = 0; i < goalCount; i++) {
            SquadPlayer scorer = attackers.get(random.nextInt(attackers.size()));

            MatchEvent event = new MatchEvent();
            event.setMatch(match);
            event.setPlayer(scorer.getPlayer());
            event.setEventType(MatchEventType.GOAL);
            event.setMinute(random.nextInt(90) + 1);
            matchEventRepository.save(event);
        }
    }

    private void generateCardEvents(Match match, List<SquadPlayer> starters) {
        for (SquadPlayer sp : starters) {
            double roll = random.nextDouble();

            if (roll < 0.03) { // 3% chance of a yellow card per player
                MatchEvent event = new MatchEvent();
                event.setMatch(match);
                event.setPlayer(sp.getPlayer());
                event.setEventType(MatchEventType.YELLOW_CARD);
                event.setMinute(random.nextInt(90) + 1);
                matchEventRepository.save(event);
            } else if (roll < 0.031) { // additional ~0.1% chance of a red card
                MatchEvent event = new MatchEvent();
                event.setMatch(match);
                event.setPlayer(sp.getPlayer());
                event.setEventType(MatchEventType.RED_CARD);
                event.setMinute(random.nextInt(90) + 1);
                matchEventRepository.save(event);
            }
        }
    }
}