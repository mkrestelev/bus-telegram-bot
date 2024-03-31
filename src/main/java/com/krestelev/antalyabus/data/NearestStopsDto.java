package com.krestelev.antalyabus.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class NearestStopsDto {

    @JsonProperty("stopList")
    private List<Stop> stops;

    @Data
    public static class Stop {
        private double distance;
        private String stopId;
        private String stopName;
    }
}
