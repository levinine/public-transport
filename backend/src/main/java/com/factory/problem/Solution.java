package com.factory.problem;

import com.factory.dto.ActivityDto;
import com.factory.dto.CoordinateDto;
import com.factory.dto.RouteDto;
import com.factory.problem.action.MoveAction;
import com.factory.problem.action.NullAction;
import com.factory.problem.action.PublicTransportAction;
import com.factory.problem.action.WalkAction;
import com.factory.problem.state.PassengerState;
import com.factory.util.Pair;
import com.google.common.collect.Sets;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Solution {

    private List<Pair<MoveAction, PassengerState>> actions;

    private Set<String> usedLineActions = new HashSet<>();

    public Solution(List<Pair<MoveAction, PassengerState>> actions) {
        this.actions = actions;
        actions.forEach(pair -> {
            if (pair.getKey() instanceof PublicTransportAction) {
                PublicTransportAction action = (PublicTransportAction) pair.getKey();
                usedLineActions.add(action.getLine().getName());
            }
        });
    }

    public boolean isDifferent(Solution previousSolution) {
        return previousSolution.getUsedLineActions().size() == 0 ||
                Sets.difference(previousSolution.getUsedLineActions(), usedLineActions).size() != 0 ||
                Sets.difference(usedLineActions, previousSolution.getUsedLineActions()).size() != 0;
    }

    public void print() {
        System.out.println("//////Solution//////");
        actions.forEach(action -> {
            System.out.println(action.getKey().getDescription() + " time spent: " + action.getKey().timeCost() * 60);
        });
        System.out.println(String.format("Time elapsed: %.4f minutes", actions
                .get(actions.size() - 1).getValue().getTimeElapsed() * 60));
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("//////Solution//////");
        actions.forEach(action -> {
            stringBuilder.append(action.getKey().getDescription() + " time spent: " + action.getKey().timeCost() * 60);
        });
        stringBuilder.append(String.format("Time elapsed: %.4f minutes", actions
                .get(actions.size() - 1).getValue().getTimeElapsed() * 60));

        return stringBuilder.toString();
    }

    public RouteDto toRouteDto() {
        RouteDto routeDto = new RouteDto();
        routeDto.setEstimatedTime(actions.get(actions.size() - 1).getValue().getTimeElapsed() * 60);

        for (int i = 0; i < actions.size(); i++) {
            Pair<MoveAction, PassengerState> action = actions.get(i);
            MoveAction currentAction = action.getKey();

            if (currentAction instanceof NullAction) {
                continue;
            } else if (currentAction instanceof WalkAction) {
                routeDto.getActivities().add(mapWalkActionToActivityDto(action.getKey()));
            } else if (currentAction instanceof PublicTransportAction) {
                int lastStationIndex = i;
                double totalEstimatedTime = currentAction.timeCost() * 60;

                for (int j = i + 1; j < actions.size(); j++) {
                    MoveAction tempAction = actions.get(j).getKey();

                    if (tempAction instanceof PublicTransportAction && currentAction.getLineNumber().equals(tempAction.getLineNumber())) {
                        totalEstimatedTime += tempAction.timeCost() * 60;
                        lastStationIndex = j;
                    }
                }

                routeDto.getActivities().add(mapPublicTransportActionToActivityDto(currentAction, actions.get(lastStationIndex).getKey(), totalEstimatedTime, lastStationIndex - i));
                i = lastStationIndex;
            }
        }
        return routeDto;
    }

    private ActivityDto mapPublicTransportActionToActivityDto(MoveAction startingStation, MoveAction endingStation, double totalEstimatedTime, int numberOfStations) {
        ActivityDto activityDto = new ActivityDto();
        activityDto.setEstimatedTime(totalEstimatedTime);
        activityDto.setLine(startingStation.getLineNumber());
        activityDto.setStartStation(startingStation.getStartingStation());
        activityDto.setEndStation(endingStation.getEndingStation());
        activityDto.setStartCoordinate(new CoordinateDto(startingStation.getStartLat(), startingStation.getStartLon()));
        activityDto.setEndCoordinate(new CoordinateDto(endingStation.getEndLat(), endingStation.getEndLon()));
        activityDto.setNumberOfStations(numberOfStations);
        activityDto.setType(2);
        return activityDto;
    }

    private ActivityDto mapWalkActionToActivityDto(MoveAction action) {
        ActivityDto activityDto = new ActivityDto();
        activityDto.setEstimatedTime(action.timeCost() * 60);
        activityDto.setLine(null);
        activityDto.setStartStation(null);
        activityDto.setEndStation(null);
        activityDto.setStartCoordinate(new CoordinateDto(action.getStartLat(), action.getStartLon()));
        activityDto.setEndCoordinate(new CoordinateDto(action.getEndLat(), action.getEndLon()));
        activityDto.setNumberOfStations(null);
        activityDto.setType(1);
        return activityDto;
    }
}

