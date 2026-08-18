package com.tikitecha.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "squad_players")
public class SquadPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "squad_id", nullable = false)
    private Squad squad;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "purchase_price", nullable = false)
    private BigDecimal purchasePrice;

    @Column(name = "is_starting", nullable = false)
    private boolean isStarting;

    public SquadPlayer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Squad getSquad() { return squad; }
    public void setSquad(Squad squad) { this.squad = squad; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public boolean isStarting() { return isStarting; }
    public void setStarting(boolean starting) { this.isStarting = starting; }
}