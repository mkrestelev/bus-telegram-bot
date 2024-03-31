package com.krestelev.antalyabus.service;

import com.krestelev.antalyabus.data.NearestBusesDto.Route;
import com.krestelev.antalyabus.data.NearestStopsDto;
import com.krestelev.antalyabus.data.RouteDto;
import com.krestelev.antalyabus.data.RouteDto.Path;
import com.krestelev.antalyabus.data.RouteDto.StopDto;
import com.krestelev.antalyabus.data.UserRequest;
import com.krestelev.antalyabus.data.NearestBusesDto;
import com.krestelev.antalyabus.data.NearestBusesDto.Bus;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static com.krestelev.antalyabus.data.NearestStopsDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KentKartService {

    private static final Pair<Double, Double> TUNEKTEPE_BUS_STATION_GEOLOCATION = Pair.of(36.8308009, 30.5962667);
    private static final String ANTALYA_REGION = "026";

    private final RestTemplate restTemplate;
    private final MessageFactory messageFactory;

    @Value("${kart.uri.nearest-bus}")
    private String nearestBusUri;

    @Value("${kart.uri.nearest-stop}")
    private String nearestStopUri;

    @Value("${kart.uri.route-info}")
    private String routeInfoUri;

    public List<Stop> getNearestStops(double latitude, double longitude) {
        NearestStopsDto nearestStopsDto = restTemplate.getForObject(
            nearestStopUri, NearestStopsDto.class, ANTALYA_REGION, latitude, longitude);

        return nearestStopsDto.getStops().stream()
            .limit(3)
            .toList();
    }

    public List<Bus> getNearestBuses(UserRequest userRequest) {
        NearestBusesDto nearestBusesDto = performGetBusesRequest(userRequest.getStopId());
        List<Bus> buses = new ArrayList<>(nearestBusesDto.getBuses());

        if (userRequest.isParticularBusRequested()) {
            buses.removeIf(bus -> !bus.getDisplayRouteCode().endsWith(userRequest.getBusId()));
        }
        return buses.stream()
            .map(KentKartService::markAsNotDepartedIfNeeded)
            .sorted(Comparator.comparing(Bus::getTimeDiff))
            .toList();
    }

    private static Bus markAsNotDepartedIfNeeded(Bus bus) {
        double epsilon = 0.002d;
        boolean busIsAtTheBusStation =
            Precision.equals(bus.getLat(), TUNEKTEPE_BUS_STATION_GEOLOCATION.getLeft(), epsilon)
                && Precision.equals(bus.getLng(), TUNEKTEPE_BUS_STATION_GEOLOCATION.getRight(), epsilon);

        if (busIsAtTheBusStation) {
            bus.setNotDeparted(true);
        }
        return bus;
    }

    private NearestBusesDto performGetBusesRequest(String stopId) {
        return restTemplate.getForObject(nearestBusUri, NearestBusesDto.class, ANTALYA_REGION, stopId);
    }

    public Set<String> getRoutes(String stopId) {
        return Optional.ofNullable(performGetBusesRequest(stopId))
            .map(nearestBusesDto -> {
                Set<String> routes = nearestBusesDto.getRoutes().stream()
                    .map(Route::getDisplayRouteCode)
                    .collect(Collectors.toSet());
                nearestBusesDto.getBuses().stream()
                    .map(Bus::getDisplayRouteCode)
                    .forEach(routes::add);
                return routes;
            })
            .orElse(Collections.emptySet());
    }

    public Optional<Bus> getParticularBus(long chatId, String stopId, String route) {
        UserRequest request = UserRequest.builder()
            .userId(chatId)
            .stopId(stopId)
            .busId(route)
            .build();

        List<Bus> nearestBuses = getNearestBuses(request);
        if (nearestBuses.isEmpty()) {
            return resolveNearestBusNotShownForStopYet(stopId, route);
        }
        return nearestBuses.stream()
            .min(Comparator.comparing(Bus::getTimeDiff));
    }

    private Optional<Bus> resolveNearestBusNotShownForStopYet(String stopId, String route) {
        String forwardDirection = "0";
        String backwardDirection = "1";

        RouteDto routeDto = restTemplate.getForObject(
            routeInfoUri, RouteDto.class, ANTALYA_REGION, forwardDirection, route);

        boolean requestedStopIsNotFoundOnForwardDirection = routeDto.getPathList().stream()
            .flatMap(path -> path.getBusStopList().stream())
            .noneMatch(busStopDto -> busStopDto.getStopId().equals(stopId));

        if (requestedStopIsNotFoundOnForwardDirection) {
            routeDto = restTemplate.getForObject(
                routeInfoUri, RouteDto.class, ANTALYA_REGION, backwardDirection, route);
        }

        Path path = routeDto.getPathList().get(0);

        Map<String, Integer> stopSequenceByStopId = path.getBusStopList().stream()
            .collect(Collectors.toMap(StopDto::getStopId, stopDto -> Integer.valueOf(stopDto.getSeq())));

        Integer sequenceOfRequestedStop = stopSequenceByStopId.get(stopId);

        return path.getBusList().stream()
            .filter(bus -> stopSequenceByStopId.get(bus.getStopId()) < sequenceOfRequestedStop)
            .max(Comparator.comparing(bus -> stopSequenceByStopId.get(bus.getStopId())))
            .map(busDto -> {
                Bus bus = new Bus();
                bus.setDisplayRouteCode(route);
                bus.setStopDiff(sequenceOfRequestedStop - stopSequenceByStopId.get(busDto.getStopId()));
                return bus;
            });
    }
}