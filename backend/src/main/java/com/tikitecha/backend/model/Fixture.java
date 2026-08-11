package com.tikitecha.backend.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "fixtures")
public class Fixture {

    @Id
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    private OffsetDateTime kickoffTime;
    private Integer gameweek;

    public Fixture() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }
    public OffsetDateTime getKickoffTime() { return kickoffTime; }
    public void setKickoffTime(OffsetDateTime kickoffTime) { this.kickoffTime = kickoffTime; }
    public Integer getGameweek() { return gameweek; }
    public void setGameweek(Integer gameweek) { this.gameweek = gameweek; }
}