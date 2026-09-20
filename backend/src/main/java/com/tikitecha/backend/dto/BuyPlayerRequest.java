package com.tikitecha.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BuyPlayerRequest {
    private Integer playerId;

    @JsonProperty("isStarting")
    private boolean isStarting;

    public BuyPlayerRequest() {}

    public Integer getPlayerId() { return playerId; }
    public void setPlayerId(Integer playerId) { this.playerId = playerId; }
    public boolean isStarting() { return isStarting; }
    public void setStarting(boolean starting) { isStarting = starting; }
}