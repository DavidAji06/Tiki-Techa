    package com.tikitecha.backend.repository;

    import com.tikitecha.backend.model.SquadPlayer;
    import org.springframework.data.jpa.repository.JpaRepository;
    import java.util.List;

    public interface SquadPlayerRepository extends JpaRepository<SquadPlayer, Long> {
    List<SquadPlayer> findBySquadId(Long squadId);
    SquadPlayer findBySquadIdAndPlayerId(Long squadId, Integer playerId);
    }