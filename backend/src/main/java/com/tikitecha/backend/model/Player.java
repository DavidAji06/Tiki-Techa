package com.tikitecha.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "players")
public class Player {
    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "position_id", nullable = false)
    private Integer positionId;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @Column(name = "now_cost", nullable = false)
    private BigDecimal nowCost;

    @Column(name = "selected_by_percent", nullable = false)
    private Double selectedByPercent;

    @Column(name = "minutes", nullable = false)
    private Integer minutes;

    @Column(name = "goals_scored", nullable = false)
    private Integer goalsScored;

    @Column(name = "assists", nullable = false)
    private Integer assists;

    @Column(name = "clean_sheets", nullable = false)
    private Integer cleanSheets;

    @Column(name = "goals_conceded", nullable = false)
    private Integer goalsConceded;

    @Column(name = "own_goals", nullable = false)
    private Integer ownGoals;

    @Column(name = "penalties_saved", nullable = false)
    private Integer penaltiesSaved;

    @Column(name = "penalties_missed", nullable = false)
    private Integer penaltiesMissed;

    @Column(name = "yellow_cards", nullable = false)
    private Integer yellowCards;

    @Column(name = "red_cards", nullable = false)
    private Integer redCards;

    public Player() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
    public Integer getPositionId() { return positionId; }
    public void setPositionId(Integer positionId) { this.positionId = positionId; }
    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }
    public BigDecimal getNowCost() { return nowCost; }
    public void setNowCost(BigDecimal nowCost) { this.nowCost = nowCost; }
    public Double getSelectedByPercent() { return selectedByPercent; }
    public void setSelectedByPercent(Double selectedByPercent) { this.selectedByPercent = selectedByPercent; }
    public Integer getMinutes() { return minutes; }
    public void setMinutes(Integer minutes) { this.minutes = minutes; }
    public Integer getGoalsScored() { return goalsScored; }
    public void setGoalsScored(Integer goalsScored) { this.goalsScored = goalsScored; }
    public Integer getAssists() { return assists; }
    public void setAssists(Integer assists) { this.assists = assists; }
    public Integer getCleanSheets() { return cleanSheets; }
    public void setCleanSheets(Integer cleanSheets) { this.cleanSheets = cleanSheets; }
    public Integer getGoalsConceded() { return goalsConceded; }
    public void setGoalsConceded(Integer goalsConceded) { this.goalsConceded = goalsConceded; }
    public Integer getOwnGoals() { return ownGoals; }
    public void setOwnGoals(Integer ownGoals) { this.ownGoals = ownGoals; }
    public Integer getPenaltiesSaved() { return penaltiesSaved; }
    public void setPenaltiesSaved(Integer penaltiesSaved) { this.penaltiesSaved = penaltiesSaved; }
    public Integer getPenaltiesMissed() { return penaltiesMissed; }
    public void setPenaltiesMissed(Integer penaltiesMissed) { this.penaltiesMissed = penaltiesMissed; }
    public Integer getYellowCards() { return yellowCards; }
    public void setYellowCards(Integer yellowCards) { this.yellowCards = yellowCards; }
    public Integer getRedCards() { return redCards; }
    public void setRedCards(Integer redCards) { this.redCards = redCards; }
}