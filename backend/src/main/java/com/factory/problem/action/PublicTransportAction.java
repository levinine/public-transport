package com.factory.problem.action;

import com.factory.model.Line;
import com.factory.problem.TransportProblem;
import com.factory.problem.state.PassengerState;
import com.factory.service.StationService;
import com.factory.service.StationServiceImpl;

public class PublicTransportAction extends MoveAction {

    private Line line;

    private double waitTime = 0;

    public PublicTransportAction(StationService stationService, String description, String startLat, String startLon, String endLat, String endLon, Line line) {
        super(stationService, description, startLat, startLon, endLat, endLon);
        this.line = line;
    }

    public int velocity() {
        return StationServiceImpl.PUBLIC_TRANSPORT_SPEED;
    }

    @Override
    public PassengerState execute(PassengerState state) {
        PassengerState nextState;
        if (state.getLine() == line) {
            nextState = super.execute(state);
            nextState.setLine(line);
        } else {
            nextState = super.execute(state);
        }

        waitTime = nextState.getLine() == line ? 0 : stationService.getWaitTime(state.getTimeElapsedInMillis()
                + TransportProblem.startTime, line, state.getStation());

        nextState.setTimeElapsed(nextState.getTimeElapsed() + waitTime);
        nextState.setLine(line);
        return nextState;
    }

    @Override
    public double timeCost() {
        return super.timeCost();
    }

    public Line getLine() {
        return line;
    }
}
