package com.tikitecha.backend.dto;

public class SimulateMatchRequest{
    private Long homeSquadId;
    private Long awaySquadId;

    public SimulateMatchRequest() {}

    public Long getHomeSquadId() {
        return homeSquadId;
    }

    public Long getAwaySquadId() {
        return awaySquadId;
    }
}