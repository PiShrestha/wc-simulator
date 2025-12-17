package com.shrestha.wcsimulator.wcsimulator.domain;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Venue {
    private String city;
    private List<MatchSlot> assignedSlots;
}
