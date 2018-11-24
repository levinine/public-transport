package com.factory.service;

import com.factory.model.Line;
import com.factory.model.Stop;
import com.factory.problem.state.PassengerState;
import com.factory.util.Pair;

import java.util.List;

public interface StationService {

    Stop findByLatLon(String endLat, String endLon);

    List<Pair<Line, Stop>> findByStartingStation(Stop stop);

    double getWaitTime(long currentTime, Line line, Stop station);

    List<Stop> findNearestForAllLines(PassengerState state);

    void search(String[] startCoords, String[] endCoords, String[] time);

    void initData();
}
