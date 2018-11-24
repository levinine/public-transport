package com.factory.problem;

import com.factory.problem.action.MoveAction;
import com.factory.problem.action.PublicTransportAction;
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
}

