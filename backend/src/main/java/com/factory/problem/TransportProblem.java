package com.factory.problem;

import com.factory.model.Stop;
import com.factory.problem.action.MoveAction;
import com.factory.problem.action.PublicTransportAction;
import com.factory.problem.action.WalkAction;
import com.factory.problem.state.PassengerState;
import com.factory.service.StationService;
import com.factory.util.Pair;
import com.factory.util.Util;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class TransportProblem {

    private String startLat;

    private String startLon;

    private String endLat;

    private String endLon;

    public static Long startTime;

    private StationService stationService;

    public boolean isEndState(PassengerState state) {
        return state.getLat().equals(endLat) && state.getLon().equals(endLon);
    }

    public List<Pair<MoveAction, PassengerState>> getSuccessors(PassengerState state) {
        List<Pair<MoveAction, PassengerState>> result = new ArrayList<>();
        addWalkSuccessors(state, result);
        addPublicTransportSuccessors(state, result);
        return result;
    }

    private void addPublicTransportSuccessors(PassengerState state, List<Pair<MoveAction, PassengerState>> result) {
        if (state.getStation() != null) {
            stationService.findByStartingStation(state.getStation()).forEach(lineStop -> {
                MoveAction action = new PublicTransportAction(stationService,
                        String.format("Ride from station [%s], to station [%s], on line [%s]",
                                state.getStation().getName(), lineStop.getValue().getName(), lineStop.getKey().getName()),
                        state.getLat(), state.getLon(), lineStop.getValue().getLat(), lineStop.getValue().getLon(), lineStop.getKey(),
                        state.getStation().getName(), lineStop.getValue().getName());
                PassengerState nextState = action.execute(state);
                result.add(new Pair<>(action, nextState));
            });
        }
    }

    private void addWalkSuccessors(PassengerState state, List<Pair<MoveAction, PassengerState>> result) {
        stationService.findNearestForAllLines(state).forEach(stop -> {
            if (state.getStation() != null && state.getStation() == stop) {
                return;
            }
            Stop start = stationService.findByLatLon(state.getLat(), state.getLon());
            String fromStation = start == null ? "" : start.getName();


            MoveAction action = new WalkAction(stationService, String.format("Walk from %s, %s [%s] to %s, %s [%s].",
                    state.getLat(), state.getLon(), fromStation, stop.getLat(), stop.getLon(), stop.getName()), state.getLat(), state.getLon(),
                    stop.getLat(), stop.getLon());
            result.add(new Pair<>(action, action.execute(state)));
        });
        WalkAction action = new WalkAction(stationService, "Walk to goal.",
                state.getLat(), state.getLon(), endLat, endLon);
        result.add(new Pair<>(action, action.execute(state)));
    }

    public PassengerState getStartState() {
        return new PassengerState(startLat, startLon, 0, null, null);
    }

    private Double getCostOfActions(List<Pair<MoveAction, PassengerState>> actions) {
        return actions.get(actions.size() - 1).getValue().getTimeElapsed();
    }

    private Double heuristic(PassengerState state) {
        return Util.distance(state.getLat(), state.getLon(), endLat, endLon) / 40;
    }

    public Double aStarCost(List<Pair<MoveAction, PassengerState>> actions) {
        return getCostOfActions(actions) + heuristic(actions.get(actions.size() - 1).getValue());
    }

    public String getStartLat() {
        return startLat;
    }

    public String getStartLon() {
        return startLon;
    }

    public String getEndLat() {
        return endLat;
    }

    public String getEndLon() {
        return endLon;
    }
}
