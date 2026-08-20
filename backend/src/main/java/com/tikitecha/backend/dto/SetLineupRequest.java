package com.tikitecha.backend.dto;

import java.util.List;

public class SetLineupRequest {
    private List<Integer> startingPlayerIds;

    public SetLineupRequest() {}

    public List<Integer> getStartingPlayerIds() { return startingPlayerIds; }
    public void setStartingPlayerIds(List<Integer> startingPlayerIds) { this.startingPlayerIds = startingPlayerIds; }
}