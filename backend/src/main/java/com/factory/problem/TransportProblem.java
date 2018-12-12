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

    public List<Pair<MoveAction, PassengerState>> getSuccessors(PassengerState state, List<Pair<MoveAction, PassengerState>> currentPath) {
        List<Pair<MoveAction, PassengerState>> result = new ArrayList<>();
        addWalkSuccessors(state, result);
        addPublicTransportSuccessors(state, result, currentPath);
        return result;
    }

    private void addPublicTransportSuccessors(PassengerState state, List<Pair<MoveAction, PassengerState>> result, List<Pair<MoveAction, PassengerState>> currentPath) {
        if (state.getStation() != null) {
            stationService.findByStartingStation(state.getStation()).forEach(lineStop -> {
                final Stop nextStop = lineStop.getValue();
				final String description = String.format("Ride from station [%s], to station [%s], on line [%s]",
				        state.getStation().getName(), nextStop.getName(), lineStop.getKey().getName());
				MoveAction action = new PublicTransportAction(stationService, description,
                        state.getLat(), state.getLon(), nextStop.getLat(), nextStop.getLon(), lineStop.getKey(),
                        state.getStation().getName(), nextStop.getName(), stationService.getZoneCost(nextStop.getZone()));
                PassengerState nextState = action.execute(state);
                // If we walked to here, add wait time for the chosen line
                if (currentPath.get(currentPath.size() - 1).getKey() instanceof WalkAction) {
                	final double waitTime = stationService.getWaitTime(startTime + state.getTimeElapsedInMillis(), lineStop.getKey(), lineStop.getValue());
                	nextState.setTimeElapsed(nextState.getTimeElapsed() + waitTime);
                }
                result.add(new Pair<>(action, nextState));
            });
        }
    }

    private void addWalkSuccessors(PassengerState state, List<Pair<MoveAction, PassengerState>> result) {
        if (state.getStation() != null) {
            WalkAction action = new WalkAction(stationService, "Walk to goal", state.getLat(), state.getLon(), endLat, endLon);
            result.add(new Pair<>(action, action.execute(state)));
            return;
        }
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
