package com.krestelev.antalyabus.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class NearestBusesDto {

    @JsonProperty("busList")
    private List<Bus> buses;

    @JsonProperty("routeList")
    private List<Route> routes;

    private StopInfo stopInfo;

    @Data
    public static class Bus {
        private String plate;
        private String displayRouteCode;
        private Integer stopDiff;
        private Integer timeDiff;
        private Double lat;
        private Double lng;
        private boolean notDeparted;
    }

    @Data
    public static class StopInfo {
        private String busStopName;
    }

    @Data
    public static class Route {
        private String displayRouteCode;
    }

}