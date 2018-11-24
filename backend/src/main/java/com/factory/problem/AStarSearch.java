package com.factory.problem;

import com.factory.problem.action.MoveAction;
import com.factory.problem.action.NullAction;
import com.factory.problem.action.WalkAction;
import com.factory.problem.state.PassengerState;
import com.factory.service.StationService;
import com.factory.util.Pair;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

@AllArgsConstructor
public class AStarSearch {

    private static final int MAX_QUEUE_SIZE = 10000;

    private StationService stationService;

    public List<Solution> search(TransportProblem problem, int solutionCount) {
        List<Solution> solutions = new ArrayList<>();
        PriorityQueue<List<Pair<MoveAction, PassengerState>>> queue = new PriorityQueue<>(getComparator(problem));
        queue.add(getNewActionList(problem.getStartState()));

        while (!queue.isEmpty() && solutions.size() < solutionCount) {
            List<Pair<MoveAction, PassengerState>> currentPath = queue.poll();
            PassengerState currentState = getCurrentStateFromPath(currentPath);

            if (queue.size() >= MAX_QUEUE_SIZE) {
                Solution trivialSolution = getTrivialSolution(problem);
                trivialSolution.print();
                solutions.add(trivialSolution);
                break;
            }

            if (problem.isEndState(currentState)) {
                Solution potentialSolution = new Solution(currentPath);
                boolean isDifferentSolution = true;
                for (Solution previousSolution : solutions) {
                    if (!potentialSolution.isDifferent(previousSolution)) {
                        isDifferentSolution = false;
                        break;
                    }
                }

                if (isDifferentSolution) {
                    solutions.add(potentialSolution);
                    potentialSolution.print();
                }
                continue;
            }

            for (Pair<MoveAction, PassengerState> next : problem.getSuccessors(currentState)) {
                List<Pair<MoveAction, PassengerState>> clonedPath = clonePath(currentPath);
                clonedPath.add(next);
                queue.add(clonedPath);
            }
        }
        return solutions;
    }

    private Solution getTrivialSolution(TransportProblem problem) {
        WalkAction action = new WalkAction(stationService, "Walk to goal.",
                problem.getStartLat(), problem.getStartLon(), problem.getEndLat(), problem.getEndLon());
        PassengerState nextState = action.execute(problem.getStartState());
        List<Pair<MoveAction, PassengerState>> trivialPath = new ArrayList<>();
        trivialPath.add(new Pair<>(new NullAction(stationService), problem.getStartState()));
        trivialPath.add(new Pair<>(action, nextState));
        return new Solution(trivialPath);
    }

    private PassengerState getCurrentStateFromPath(List<Pair<MoveAction, PassengerState>> currentPath) {
        return currentPath.get(currentPath.size() - 1).getValue();
    }

    private List<Pair<MoveAction, PassengerState>> clonePath(List<Pair<MoveAction, PassengerState>> currentPath) {
        return currentPath.stream()
                .map(i -> new Pair<>(i.getKey(), i.getValue())).collect(Collectors.toList());
    }

    private List<Pair<MoveAction, PassengerState>> getNewActionList(PassengerState startState) {
        List<Pair<MoveAction, PassengerState>> list = new ArrayList<>();
        list.add(new Pair<>(new NullAction(stationService), startState));
        return list;
    }

    private Comparator<List<Pair<MoveAction, PassengerState>>> getComparator(TransportProblem problem) {
        return (o1, o2) -> {
            if (problem.aStarCost(o1) < problem.aStarCost(o2)) {
                return -1;
            }
            return 1;
        };
    }
}
