package fr.jeremy.mas.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import java.util.*;

public class Distance {

    public static List<String> findShortestPath(int[] startCoordinates, int[] endCoordinates, List<List<String>> map) {

        List<List<String>> grid = new ArrayList<>();
        for (List<String> row : map) {
            List<String> newRow = new ArrayList<>(row);
            grid.add(newRow);
        }

        int distanceFromTop = startCoordinates[0];
        int distanceFromLeft = startCoordinates[1];

        Queue<Map<String, Object>> queue = new LinkedList<>();

        Map<String, Object> location = new HashMap<>();
        location.put("distanceFromTop", distanceFromTop);
        location.put("distanceFromLeft", distanceFromLeft);
        location.put("path", new ArrayList<String>());
        location.put("status", "Start");

        queue.add(location);

        // Loop through the grid searching for the goal
        while (!queue.isEmpty()) {
            // Take the first location off the queue
            Map<String, Object> currentLocation = queue.poll();

            // Explore in each direction
            String[] directions = {"north", "east", "south", "west"};
            for (String direction : directions) {
                Map<String, Object> newLocation = exploreInDirection(currentLocation, endCoordinates, direction, grid);
                if ("goal".equalsIgnoreCase((String) newLocation.get("status"))) {
                    return (List<String>) newLocation.get("path");
                } else if ("valid".equalsIgnoreCase((String) newLocation.get("status"))) {
                    queue.add(newLocation);
                }
            }
        }

        // No valid path found
        return Collections.emptyList();
    }

    private static Map<String, Object> exploreInDirection(Map<String, Object> currentLocation, int[] endCoordinates, String direction, List<List<String>> grid) {

        List<String> newPath = new ArrayList<>((List<String>) currentLocation.get("path"));
        newPath.add(direction);

        int dft = (int) currentLocation.get("distanceFromTop");
        int dfl = (int) currentLocation.get("distanceFromLeft");

        switch (direction.toLowerCase()) {
            case "north":
                dft -= 1;
                break;
            case "east":
                dfl += 1;
                break;
            case "south":
                dft += 1;
                break;
            case "west":
                dfl -= 1;
                break;
        }

        Map<String, Object> newLocation = new HashMap<>();
        newLocation.put("distanceFromTop", dft);
        newLocation.put("distanceFromLeft", dfl);
        newLocation.put("path", newPath);
        newLocation.put("status", "Unknown");

        newLocation.put("status", locationStatus(newLocation, endCoordinates, grid));

        // If this new location is valid, mark it as 'V' (Visited)
        if ("valid".equalsIgnoreCase((String) newLocation.get("status"))) {
            grid.get(dft).set(dfl, "V");
        }

        return newLocation;
    }

    private static String locationStatus(Map<String, Object> location, int[] endCoordinates, List<List<String>> grid) {

        int gridSize = grid.size();
        int dft = (int) location.get("distanceFromTop");
        int dfl = (int) location.get("distanceFromLeft");

        if (dfl < 0 || dfl >= gridSize || dft < 0 || dft >= gridSize) {
            // location is not on the grid--return false
            return "invalid";
        } else if (dft == endCoordinates[0] && dfl == endCoordinates[1]) {
            return "goal";
        } else if ("HAOV".contains(grid.get(dft).get(dfl))) {
            // location is either an obstacle or has been visited
            return "blocked";
        } else {
            return "valid";
        }
    }


}
