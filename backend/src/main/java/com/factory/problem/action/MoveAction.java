package com.factory.problem.action;

import com.factory.problem.state.PassengerState;
import com.factory.service.StationService;
import com.factory.util.Util;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class MoveAction {

    protected StationService stationService;

    protected String description;

    protected String startLat;

    protected String startLon;

    protected String endLat;

    protected String endLon;

    protected String lineNumber;

    protected String startingStation;

    protected String endingStation;

    protected Double price;

    public PassengerState execute(PassengerState state) {
        PassengerState newState = (PassengerState) state.clone();
        newState.setLat(endLat);
        newState.setLon(endLon);
        newState.setStation(stationService.findByLatLon(endLat, endLon));
        newState.setTimeElapsed(state.getTimeElapsed() + timeCost());
        newState.setLine(null);
        return newState;
    }

    public double distance() {
        return Util.distance(startLat, startLon, endLat, endLon);
    }

    public double timeCost() {
        return distance() / velocity();
    }

    public abstract int velocity();
}
