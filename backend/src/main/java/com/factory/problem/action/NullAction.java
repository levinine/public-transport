package com.factory.problem.action;

import com.factory.problem.state.PassengerState;
import com.factory.service.StationService;

public class NullAction extends MoveAction {

    public NullAction(StationService service) {
        super(service, "No Action", "0", "0", "0", "0");
    }

    @Override
    public int velocity() {
        return 1;
    }

    @Override
    public PassengerState execute(PassengerState state) {
        return state;
    }

    @Override
    public double distance() {
        return 0D;
    }

    @Override
    public double timeCost() {
        return 0D;
    }
}
