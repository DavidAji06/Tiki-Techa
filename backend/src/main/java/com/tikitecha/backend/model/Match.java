package com.tikitecha.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    

    @ManyToOne
    @JoinColumn(name = "home_squad_id", nullable = false)
    private Squad homeSquad;

    @ManyToOne
    @JoinColumn(name = "away_squad_id", nullable = false)
    private Squad awaySquad;

    @Column(name = "home_score", nullable = false)
    private Integer homeScore;

    @Column(name = "away_score", nullable = false)
    private Integer awayScore;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Squad getHomeSquad() {
        return homeSquad;
    }

    public void setHomeSquad(Squad homeSquad) {
        this.homeSquad = homeSquad;
    }

    public Squad getAwaySquad() {
        return awaySquad;
    }

    public void setAwaySquad(Squad awaySquad) {
        this.awaySquad = awaySquad;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(LocalDateTime playedAt) {
        this.playedAt = playedAt;
    }
}