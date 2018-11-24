package com.factory.service;

import com.factory.common.AppConfig;
import com.factory.dto.CoordinateDto;
import com.factory.dto.LineDto;
import com.factory.dto.RouteDto;
import com.factory.dto.RoutesDto;
import com.factory.model.*;
import com.factory.problem.AStarSearch;
import com.factory.problem.Solution;
import com.factory.problem.TransportProblem;
import com.factory.problem.state.PassengerState;
import com.factory.util.Pair;
import com.factory.util.Util;
import com.google.gson.Gson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StationServiceImpl implements StationService {

    private final AppConfig appConfig;

    public static final int PUBLIC_TRANSPORT_SPEED = 40;

    private CityData cityData;

    private ZoneData zoneData;

    private Map<String, Stop> stationIndexByCoords;

    private Map<String, List<Stop>> stationIndexByLine;

    private Map<String, Line> lineIndexByName;

    private Map<String, Map<String, Long>> lineToStationTime = new HashMap<>();

    public StationServiceImpl(AppConfig appConfig) {
        this.appConfig = appConfig;

        initData();
    }

    public void initData() {
        loadLines();
        loadZones();
        indexStationsByCoords();
        indexLinesByName();
        indexStationsByLines();
        calculateLineToStationTime();
    }

    private void calculateLineToStationTime() {
        cityData.getLines().forEach(line -> {
            List<Stop> stops = stationIndexByLine.get(line.getName());
            lineToStationTime.putIfAbsent(line.getName(), new HashMap<>());

            if (!stops.isEmpty()) {
                Long totalTime = 0L;
                lineToStationTime.get(line.getName()).put(stops.get(0).getName(), totalTime);

                for (int i = 1; i < stops.size(); i++) {
                    Stop previousStop = stops.get(i - 1);
                    Stop currentStop = stops.get(i);

                    totalTime += ((Double) (Util.distance(previousStop.getLat(), previousStop.getLon(), currentStop.getLat(), currentStop.getLon()) * 3600000 / PUBLIC_TRANSPORT_SPEED)).longValue();
                    lineToStationTime.get(line.getName()).put(currentStop.getName(), totalTime);
                }
            }
        });
    }

    private void indexStationsByLines() {
        stationIndexByLine = new HashMap<>();
        cityData.getLines().forEach(line -> {
            List<Stop> stops = cityData.getStops().stream()
                    .filter(stop -> stop.getLines().contains(line.getName()))
                    .collect(Collectors.toList());
            List<Pair<Stop, Integer>> stopOrder = new ArrayList<>();
            stops.forEach(stop -> stopOrder.add(new Pair<>(stop, getStopOrder(stop, line))));
            stopOrder.sort(Comparator.comparingInt(Pair::getValue));
            stationIndexByLine.put(line.getName(), stopOrder.stream().map(Pair::getKey).collect(Collectors.toList()));
        });
    }

    private Integer getStopOrder(Stop stop, Line line) {
        int minIndex = 0;
        double minDistance = 999999999;
        for (int i = 0; i < line.getCoordinates().size() - 1; i++) {
            Coordinate c1 = line.getCoordinates().get(i);
            Coordinate c2 = line.getCoordinates().get(i + 1);
            double realDistance = Util.distance(c1.getLat(), c1.getLon(), stop.getLat(), stop.getLon()) +
                    Util.distance(stop.getLat(), stop.getLon(), c2.getLat(), c2.getLon());
            if (realDistance < minDistance) {
                minDistance = realDistance;
                minIndex = i;
            }
        }
        return minIndex;
    }

    private void indexLinesByName() {
        lineIndexByName = new HashMap<>();
        cityData.getLines().forEach(line -> lineIndexByName.putIfAbsent(line.getName(), line));
    }

    private void indexStationsByCoords() {
        stationIndexByCoords = new HashMap<>();
        cityData.getStops().forEach(stop -> stationIndexByCoords.putIfAbsent(getStationCoordIndex(stop), stop));
    }

    private String getStationCoordIndex(Stop stop) {
        return getCoordIndex(stop.getLat(), stop.getLon());
    }

    private String getCoordIndex(String endLat, String endLon) {
        return endLat + ", " + endLon;
    }

    private void loadLines() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpEntity entity = new HttpEntity(requestHeaders);
        ResponseEntity<String> response = restTemplate.exchange(appConfig.getLines(), HttpMethod.GET, entity, String.class);
        Gson g = new Gson();
        cityData = g.fromJson(response.getBody(), CityData.class);
        Set<String> stops = new HashSet<>();
        cityData.setStops(cityData.getStops().stream().filter(stop -> {
            if (stops.contains(stop.getName())) {
                return false;
            }
            stops.add(stop.getName());
            return true;
        }).collect(Collectors.toList()));
    }

    private void loadZones() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpEntity entity = new HttpEntity(requestHeaders);
        ResponseEntity<String> response = restTemplate.exchange(appConfig.getZones(), HttpMethod.GET, entity, String.class);
        Gson g = new Gson();
        zoneData = g.fromJson(response.getBody(), ZoneData.class);
    }

    @Override
    public Stop findByLatLon(String endLat, String endLon) {
        return stationIndexByCoords.get(getCoordIndex(endLat, endLon));
    }

    @Override
    public List<Pair<Line, Stop>> findByStartingStation(Stop stop) {
        List<Pair<Line, Stop>> result = new ArrayList<>();

        for (String line : stop.getLines()) {
            List<Stop> lineStops = stationIndexByLine.get(line);
            int nextIndex = lineStops.indexOf(stop) + 1;
            if (nextIndex < lineStops.size()) {
                result.add(new Pair<>(lineIndexByName.get(line), lineStops.get(nextIndex)));
            }
        }
        return result;
    }

    @Override
    public double getWaitTime(long currentTime, Line line, Stop station) {
        for (String timetableTime : line.getTimeTable()) {
            Double timetableTimeInMillis = getTimetableTimeInMillis(timetableTime);

            if (currentTime < lineToStationTime.get(line.getName()).get(station.getName()) + timetableTimeInMillis.longValue()) {
                return (lineToStationTime.get(line.getName()).get(station.getName())
                        + timetableTimeInMillis.longValue()
                        - currentTime) / 3600000.00;
            }
        }
        return 0;
    }

    private Double getTimetableTimeInMillis(String timetableTime) {
        String[] timeParts = timetableTime.split(":");
        int hour = Integer.valueOf(timeParts[0]);
        int minutes = Integer.valueOf(timeParts[1]);
        return (hour + minutes / 60.0) * 3600000;
    }

    @Override
    public List<Stop> findNearestForAllLines(PassengerState state) {
        List<Stop> result = new ArrayList<>();
        for (Line line : cityData.getLines()) {
            if (state.getStation() != null && state.getStation().getLines().contains(line.getName())) {
                continue;
            }

            List<Stop> stops = stationIndexByLine.get(line.getName());
            if (!stops.isEmpty()) {
                double minDist = 9999999;
                int minIndex = 0;

                for (int i = 0; i < stops.size(); i++) {
                    Stop stop = stops.get(i);
                    double dist = Util.distance(state.getLat(), state.getLon(), stop.getLat(), stop.getLon());
                    if (dist < minDist) {
                        minDist = dist;
                        minIndex = i;
                    }
                }
                result.add(stops.get(minIndex));
            }
        }
        return result;
    }

    @Override
    public RoutesDto search(String[] startCoords, String[] endCoords, int hours, int minutes) {
        TransportProblem problem = new TransportProblem(startCoords[1], startCoords[0],
                endCoords[1], endCoords[0], this);
        TransportProblem.startTime = ((Double) ((hours + minutes / 60.0) * 3600000)).longValue();
        AStarSearch search = new AStarSearch(this);

        List<Solution> solutions = search.search(problem, 3);
        List<RouteDto> routes = new ArrayList<>();

        solutions.stream().forEach(solution -> routes.add(solution.toRouteDto()));
        return new RoutesDto(routes);
    }


    @Override
    public Double getZoneCost(String zoneName) {
        for (Zone zone : zoneData.getZones()) {
            if (zone.getName().equals(zoneName)) {
                return zone.getPrice();
            }
        }

        return 0D;
    }

    @Override
    public List<LineDto> findAllLines() {
        return cityData.getLines().stream().map(line -> {
            LineDto lineDto = new LineDto();
            lineDto.setName(line.getName());
            line.getCoordinates().stream().forEach(coordinate -> lineDto.getCoordinates().add(new CoordinateDto(Double.parseDouble(coordinate.getLat()), Double.parseDouble(coordinate.getLon()))));
            return lineDto;
        }).collect(Collectors.toList());
    }
}
