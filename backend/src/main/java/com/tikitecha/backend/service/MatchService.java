package com.tikitecha.backend.service;

import java.util.List;
import java.util.Random;
import java.util.HashMap; import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tikitecha.backend.model.Match;
import com.tikitecha.backend.model.MatchEvent;
import com.tikitecha.backend.model.MatchEventType;
import com.tikitecha.backend.model.Squad;
import com.tikitecha.backend.model.SquadPlayer;
import com.tikitecha.backend.repository.MatchEventRepository;
import com.tikitecha.backend.repository.MatchRepository;
import com.tikitecha.backend.repository.SquadPlayerRepository;
import com.tikitecha.backend.repository.SquadRepository;

@Service
public class MatchService {

    private final SquadPlayerRepository squadPlayerRepository;
    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private static final double BASE_RATE = 1.3;
    private static final double MIN_RATING = 0.1;
    private static final Random random = new Random();
    private final SquadRepository squadRepository;
    private record SimulatedScore(int homeGoals, int awayGoals) {}

    private static final double TACTIC_MODIFIER = 1.15; // placeholder value, can be adjusted based on tactics in the future

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

    public Match simulateMatch(Long homeSquadId, Long awaySquadId) {
    return runSimulation(homeSquadId, awaySquadId, 1.0, 1.0);
}

public Match simulateVsAi(Long userSquadId, Long aiSquadId) {
    List<SquadPlayer> userStarters = squadPlayerRepository.findBySquadId(userSquadId).stream()
            .filter(SquadPlayer::isStarting)
            .toList();

    double userAttack = calculateAttackRating(userStarters);
    double userDefense = calculateDefenseRating(userStarters);

    // the AI scouts the user's weaker side and adjusts its own tactic accordingly
    double aiAttackModifier = 1.0;
    double aiDefenseModifier = 1.0;

    if (userDefense < userAttack) {
        aiAttackModifier = TACTIC_MODIFIER; // exploit the weaker defense
    } else {
        aiDefenseModifier = TACTIC_MODIFIER; // opponent's attack is weaker — sit back
    }

    return runSimulation(aiSquadId, userSquadId, aiAttackModifier, aiDefenseModifier);
}

    @Transactional
    private Match runSimulation(Long homeSquadId, Long awaySquadId, double homeAttackModifier, double homeDefenseModifier) {
        Squad homeSquad = squadRepository.findById(homeSquadId)
                .orElseThrow(() -> new IllegalArgumentException("Home squad not found"));
        Squad awaySquad = squadRepository.findById(awaySquadId)
                .orElseThrow(() -> new IllegalArgumentException("Away squad not found"));

        List<SquadPlayer> homeStarters = squadPlayerRepository.findBySquadId(homeSquadId).stream()
                .filter(SquadPlayer::isStarting).toList();
        List<SquadPlayer> awayStarters = squadPlayerRepository.findBySquadId(awaySquadId).stream()
                .filter(SquadPlayer::isStarting).toList();

        SimulatedScore score = computeScore(homeSquadId, awaySquadId, homeAttackModifier, homeDefenseModifier);

        Match match = new Match();
        match.setHomeSquad(homeSquad);
        match.setAwaySquad(awaySquad);
        match.setHomeScore(score.homeGoals());
        match.setAwayScore(score.awayGoals());
        match.setPlayedAt(java.time.LocalDateTime.now());
        matchRepository.save(match);

        generateMatchEvents(match, score.homeGoals(), score.awayGoals(), homeStarters, awayStarters);

        return match;
    }

    public Map<String, Object> benchmarkVsAi(Long userSquadId, Long aiSquadId, int runs) {
        List<SquadPlayer> userStarters = squadPlayerRepository.findBySquadId(userSquadId).stream()
                .filter(SquadPlayer::isStarting)
                .toList();

        double userAttack = calculateAttackRating(userStarters);
        double userDefense = calculateDefenseRating(userStarters);

        double aiAttackModifier = 1.0;
        double aiDefenseModifier = 1.0;
        if (userDefense < userAttack) {
            aiAttackModifier = TACTIC_MODIFIER;
        } else {
            aiDefenseModifier = TACTIC_MODIFIER;
        }

        int aiWins = 0;
        int draws = 0;
        int userWins = 0;

        for (int i = 0; i < runs; i++) {
            SimulatedScore score = computeScore(aiSquadId, userSquadId, aiAttackModifier, aiDefenseModifier);
            if (score.homeGoals() > score.awayGoals()) {
                aiWins++;
            } else if (score.homeGoals() < score.awayGoals()) {
                userWins++;
            } else {
                draws++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("runs", runs);
        result.put("aiWins", aiWins);
        result.put("draws", draws);
        result.put("userWins", userWins);
        result.put("aiWinRate", (double) aiWins / runs);
        return result;
    }

    

    private SimulatedScore computeScore(Long homeSquadId, Long awaySquadId, double homeAttackModifier, double homeDefenseModifier) {
        List<SquadPlayer> homeStarters = squadPlayerRepository.findBySquadId(homeSquadId).stream()
                .filter(SquadPlayer::isStarting)
                .toList();
        List<SquadPlayer> awayStarters = squadPlayerRepository.findBySquadId(awaySquadId).stream()
                .filter(SquadPlayer::isStarting)
                .toList();

        double homeAttack = calculateAttackRating(homeStarters) * homeAttackModifier;
        double homeDefense = calculateDefenseRating(homeStarters) * homeDefenseModifier;
        double awayAttack = calculateAttackRating(awayStarters);
        double awayDefense = calculateDefenseRating(awayStarters);

        double homeExpectedGoals = calculateExpectedGoals(homeAttack, awayDefense);
        double awayExpectedGoals = calculateExpectedGoals(awayAttack, homeDefense);

        return new SimulatedScore(samplePoissonGoals(homeExpectedGoals), samplePoissonGoals(awayExpectedGoals));
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