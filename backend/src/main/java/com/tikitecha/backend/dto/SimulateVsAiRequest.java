package com.tikitecha.backend.dto;

public class SimulateVsAiRequest {
    private Long userSquadId;

    public SimulateVsAiRequest() {}

    public Long getUserSquadId() { return userSquadId; }
    public void setUserSquadId(Long userSquadId) { this.userSquadId = userSquadId; }
}