package ubb.scs.map.guisocialnetwork.utils;

import ubb.scs.map.guisocialnetwork.domain.entities.Utilizator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class IterativeDFS {
    public static Pair<Integer, ArrayList<Utilizator>> conexComponent(Map<Utilizator, List<Utilizator>> graph, Utilizator startNode) {
        Stack<Utilizator> stack = new Stack<>();
        ArrayList<Utilizator> visited = new ArrayList<>();
        stack.push(startNode);
        int longestRoadLength = 0;
        int currentRoadLength = 0;
        while (!stack.isEmpty()) {
            Utilizator currentNode = stack.pop();
            if (!visited.contains(currentNode)) {
                visited.add(currentNode);
                List<Utilizator> neighbors = graph.get(currentNode);
                if (neighbors != null) {
                    neighbors.forEach(neighbor-> {if (!visited.contains(neighbor)) stack.push(neighbor);});
                    currentRoadLength++;
                }
            } else {
                if(currentRoadLength > longestRoadLength) longestRoadLength = currentRoadLength;
                currentRoadLength--;
            }
        }
        if(currentRoadLength > longestRoadLength) longestRoadLength = currentRoadLength;
        return new Pair<>(longestRoadLength, visited);
    }
}