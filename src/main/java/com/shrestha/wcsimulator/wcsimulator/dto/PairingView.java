package com.shrestha.wcsimulator.wcsimulator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PairingView {
    private String home;
    private String away;
    private String homeSlot;
    private String awaySlot;
    private String stage;
    private String date;
    private String city;
    private double avgRank;
    private int matchId;
}
