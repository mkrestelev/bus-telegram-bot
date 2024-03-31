package com.krestelev.antalyabus.data;

import java.util.List;
import lombok.Data;

@Data
public class RouteDto {

    private List<Path> pathList;

    @Data
    public static class Path {
        private String direction;
        private List<BusDto> busList;
        private List<StopDto> busStopList;
    }

    @Data
    public static class StopDto {
        private String seq;
        private String stopId;
    }

    @Data
    public static class BusDto {
        private String stopId;
        private Double lat;
        private Double lng;
    }
}
