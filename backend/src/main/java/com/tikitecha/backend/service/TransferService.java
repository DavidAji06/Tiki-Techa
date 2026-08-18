package com.tikitecha.backend.service;

import com.tikitecha.backend.model.*;
import com.tikitecha.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransferService {

    private final SquadRepository squadRepository;
    private final SquadPlayerRepository squadPlayerRepository;
    private final PlayerRepository playerRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(
            SquadRepository squadRepository,
            SquadPlayerRepository squadPlayerRepository,
            PlayerRepository playerRepository,
            TransactionRepository transactionRepository
    ) {
        this.squadRepository = squadRepository;
        this.squadPlayerRepository = squadPlayerRepository;
        this.playerRepository = playerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void buyPlayer(Long userId, Integer playerId, boolean isStarting) {

        // find the user's squad — acquires the pessimistic lock
        Squad squad = squadRepository.findByUserId(userId);
        if (squad == null) {
            throw new IllegalStateException("No squad found for this user");
        }

        // find player being bought
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // check the player isn't already in the squad
        SquadPlayer existing = squadPlayerRepository.findBySquadIdAndPlayerId(squad.getId(), playerId);
        if (existing != null) {
            throw new IllegalStateException("Player already in squad");
        }

        // check budget
        BigDecimal price = player.getNowCost();
        if (squad.getBudgetRemaining().compareTo(price) < 0) {
            throw new IllegalStateException("Insufficient budget");
        }

        // fetch everyone currently in the squad — needed for both remaining checks
        List<SquadPlayer> currentSquad = squadPlayerRepository.findBySquadId(squad.getId());

        // positional limits (FPL rules: max 2 GK, 5 DEF, 5 MID, 3 FWD)
        long samePositionCount = currentSquad.stream()
                .filter(sp -> sp.getPlayer().getPositionId().equals(player.getPositionId()))
                .count();

        int maxForPosition = switch (player.getPositionId()) {
            case 1 -> 2; // GK
            case 2 -> 5; // DEF
            case 3 -> 5; // MID
            case 4 -> 3; // FWD
            default -> throw new IllegalStateException("Unknown position");
        };

        if (samePositionCount >= maxForPosition) {
            throw new IllegalStateException("Position limit reached for this squad");
        }

        // max 3 players from any one real-world team
        long sameTeamCount = currentSquad.stream()
                .filter(sp -> sp.getPlayer().getTeam().getId().equals(player.getTeam().getId()))
                .count();

        if (sameTeamCount >= 3) {
            throw new IllegalStateException("Maximum 3 players from the same team already reached");
        }

        // all checks passed — commit the purchase
        squad.setBudgetRemaining(squad.getBudgetRemaining().subtract(price));
        squadRepository.save(squad);

        SquadPlayer squadPlayer = new SquadPlayer();
        squadPlayer.setSquad(squad);
        squadPlayer.setPlayer(player);
        squadPlayer.setPurchasePrice(price);
        squadPlayer.setStarting(isStarting);
        squadPlayerRepository.save(squadPlayer);

        Transaction transaction = new Transaction();
        transaction.setUser(squad.getUser());
        transaction.setPlayer(player);
        transaction.setType(TransactionType.BUY);
        transaction.setPrice(price);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void sellPlayer(Long userId, Integer playerId) {
        // find the user's squad — acquires the pessimistic lock
        Squad squad = squadRepository.findByUserId(userId);
        if (squad == null) {
            throw new IllegalStateException("No squad found for this user");
        }

        // find the player in the squad
        SquadPlayer squadPlayer = squadPlayerRepository.findBySquadIdAndPlayerId(squad.getId(), playerId);
        if (squadPlayer == null) {
            throw new IllegalStateException("Player not in squad");
        }

        // fetch the player to get the current price
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        BigDecimal sellPrice = player.getNowCost();

        // commit the sale
        squad.setBudgetRemaining(squad.getBudgetRemaining().add(sellPrice));
        squadRepository.save(squad);

        squadPlayerRepository.delete(squadPlayer);

        Transaction transaction = new Transaction();
        transaction.setUser(squad.getUser());
        transaction.setPlayer(player);
        transaction.setType(TransactionType.SELL);
        transaction.setPrice(sellPrice);
        transactionRepository.save(transaction);
    }
}