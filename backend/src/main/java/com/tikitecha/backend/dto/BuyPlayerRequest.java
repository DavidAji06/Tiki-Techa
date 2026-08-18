package com.tikitecha.backend.dto;

public class BuyPlayerRequest {
    private Integer playerId;
    private boolean isStarting;

    public BuyPlayerRequest() {}

    public Integer getPlayerId() { return playerId; }
    public void setPlayerId(Integer playerId) { this.playerId = playerId; }
    public boolean isStarting() { return isStarting; }
    public void setStarting(boolean starting) { isStarting = starting; }
}