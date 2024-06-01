package fr.jeremy.mas.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import java.util.*;

public class Distance {

    public static List<String> findShortestPath(int[] startCoordinates, int[] endCoordinates, int[][] map) {
        List<int[]> grid = new ArrayList<>();
        for (int[] row : map) {
            grid.add(Arrays.copyOf(row, row.length));
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

        while (!queue.isEmpty()) {
            Map<String, Object> currentLocation = queue.poll();

            Map<String, Object> newLocation = exploreInDirection(currentLocation, endCoordinates, "north", grid);
            if ("goal".equalsIgnoreCase((String) newLocation.get("status"))) {
                return (List<String>) newLocation.get("path");
            } else if ("valid".equalsIgnoreCase((String) newLocation.get("status"))) {
                queue.add(newLocation);
            }

            newLocation = exploreInDirection(currentLocation, endCoordinates, "east", grid);
            if ("goal".equalsIgnoreCase((String) newLocation.get("status"))) {
                return (List<String>) newLocation.get("path");
            } else if ("valid".equalsIgnoreCase((String) newLocation.get("status"))) {
                queue.add(newLocation);
            }

            newLocation = exploreInDirection(currentLocation, endCoordinates, "south", grid);
            if ("goal".equalsIgnoreCase((String) newLocation.get("status"))) {
                return (List<String>) newLocation.get("path");
            } else if ("valid".equalsIgnoreCase((String) newLocation.get("status"))) {
                queue.add(newLocation);
            }

            newLocation = exploreInDirection(currentLocation, endCoordinates, "west", grid);
            if ("goal".equalsIgnoreCase((String) newLocation.get("status"))) {
                return (List<String>) newLocation.get("path");
            } else if ("valid".equalsIgnoreCase((String) newLocation.get("status"))) {
                queue.add(newLocation);
            }
        }

        return Collections.emptyList(); // No valid path found
    }

    private static Map<String, Object> exploreInDirection(Map<String, Object> currentLocation, int[] endCoordinates, String direction, List<int[]> grid) {

        List<String> newPath = new ArrayList<>((List<String>) currentLocation.get("path"));
        newPath.add(direction);

        int dft = (int) currentLocation.get("distanceFromTop");
        int dfl = (int) currentLocation.get("distanceFromLeft");

        switch (direction) {
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

        String status = locationStatus(newLocation, endCoordinates, grid);
        newLocation.put("status", status);

        if ("valid".equalsIgnoreCase(status)) {
            grid.get(dft)[dfl] = 'V'; // Mark as visited
        }

        return newLocation;
    }

    private static String locationStatus(Map<String, Object> location, int[] endCoordinates, List<int[]> grid) {

        int gridSize = grid.size();
        int dft = (int) location.get("distanceFromTop");
        int dfl = (int) location.get("distanceFromLeft");

        if (dfl < 0 || dfl >= gridSize || dft < 0 || dft >= gridSize) {
            return "invalid";
        } else if (dft == endCoordinates[0] && dfl == endCoordinates[1]) {
            return "goal";
        } else if ("HAOV".contains(String.valueOf(grid.get(dft)[dfl]))) {
            return "blocked";
        } else {
            return "valid";
        }
    }
}
