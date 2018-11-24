package com.factory.problem.action;


import com.factory.service.StationService;

public class WalkAction extends MoveAction {

    public WalkAction(StationService stationService, String description, String startLat,
                      String startLon, String endLat, String endLon) {
        super(stationService, description, startLat, startLon, endLat, endLon);
    }

    public int velocity() {
        return 4;
    }
}
