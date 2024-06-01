package fr.jeremy.mas.representation;

import fr.jeremy.mas.communication.Operation;

import java.util.*;

public class Environment {

    public int numberOfAgents;
    public long tickTime;
    public long totalTime;
    public long remainingTime;
    public int gridWidth;
    public int gridHeight;
    public List<Agent> agents = new ArrayList<Agent>();
    public List<Tile> tiles = new ArrayList<Tile>();
    public List<Obstacle> obstacles = new ArrayList<Obstacle>();
    public List<Hole> holes = new ArrayList<Hole>();
    public Generator generator = null;
    public List<List<String>> map = new ArrayList<>();

    public synchronized boolean executeOperation(String author, Operation operation) {

        return switch (operation.code) {
            case "PICK" -> pick(author, operation);
            case "DROP" -> drop(author, operation);
            case "USE" -> use(author, operation);
            case "MOVE" -> move(author, operation);
            case "TRANSFER" -> transfer(author, operation);
            case "ADD" -> addRandomGroup();
            default -> throw new IllegalArgumentException("Error: Unknown operation \"" + operation + "\"");
        };
    }

    private boolean addRandomGroup() {

        if (isEnoughSpace(2)) {

            String[] colors = new String[this.numberOfAgents];
            int i = 0;
            for (Agent agent : this.agents) {
                colors[i++] = agent.getColor();
            }

            long lifetime = getRandomLifetime();

            Random random = new Random();
            int depth = random.nextInt(5) + 1;
            String color = colors[random.nextInt(colors.length)];

            Map<String, Integer> position = getRandomPosition();
            Hole hole = new Hole(color, depth, position.get("x"), position.get("y"), lifetime);
            this.holes.add(hole);

            position = getRandomPosition();
            Tile tile = new Tile(depth, color, position.get("x"), position.get("y"), lifetime);
            this.tiles.add(tile);

            initialiseMap();

            return true;
        }

        return false;
    }

    private long getRandomLifetime() {
        long generatorStartTime = this.generator.generatorMinLifetime;
        long generatorEndTime = this.generator.generatorMaxLifetime;
        Random r = new Random();
        long nextRandomTime = generatorStartTime + ((long)(r.nextDouble() * (generatorEndTime - generatorStartTime)));

        return nextRandomTime;
    }

    private Map<String, Integer> getRandomPosition() {
        Random random = new Random();

        while (true) {
            int x = random.nextInt(this.gridHeight);
            int y = random.nextInt(this.gridWidth);
            if ("E".equals(this.map.get(x).get(y))) {
                return new HashMap<>() {{
                    put("x", x);
                    put("y", y);
                }};
            }
        }
    }

    private boolean isEnoughSpace(int space) {
        int availableSpace = 0;

        for (List<String> row : map) {
            for (String cell : row) {
                if ("E".equals(cell)) {
                    availableSpace++;
                }
            }
        }

        return availableSpace >= space;
    }

    private boolean move(String author, Operation operation) {
        if(!canMoveTo(operation.position)) return false;

        for(int i = 0; i < numberOfAgents; i++){
            if(author.equalsIgnoreCase(agents.get(i).name)){

                // Update agent old position and new position
                agents.get(i).xPosition = operation.position.get("x");
                agents.get(i).yPosition = operation.position.get("y");
                initialiseMap();
                return true;
            }
        }
        return false;
    }

    private boolean pick(String author, Operation operation) {

        if (!map.get(operation.position.get("x")).contains("T")) {
            return false;
        }

        for (Agent agent : agents) {
            if (author.equalsIgnoreCase(agent.getName())) {
                for (int i = 0; i < tiles.size(); i++) {
                    Tile tile = tiles.get(i);
                    if (tile.getXPosition() == operation.position.get("x") && tile.getYPosition() == operation.position.get("y")) {
                        if (tile.getNumberOfTiles() > 1) {
                            agent.setTile(new Tile(tile.getColor()));
                            tile.setNumberOfTiles(tile.getNumberOfTiles() - 1);
                        } else {
                            agent.setTile(tile);
                            tiles.remove(i);
                        }
                        initialiseMap();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean drop(String author, Operation operation) {

        if (map.get(operation.position.get("x")).equals("H") || map.get(operation.position.get("x")).equals("O")) {
            return false;
        }

        for (Agent agent : agents) {
            if (author.equalsIgnoreCase(agent.getName())) {

                if (agent.getTile() != null) {
                    Tile tile = agent.getTile();
                    tile.setXPosition(agent.getXPosition());
                    tile.setYPosition(agent.getYPosition());
                    tiles.add(tile);

                    agent.setTile(null);

                    initialiseMap();

                    return true;
                }
            }
        }

        return false;
    }

    private boolean use(String author, Operation operation) {

        for (Agent agent : agents) {
            if (author.equalsIgnoreCase(agent.getName())) {

                int newX = agent.getXPosition();
                int newY = agent.getYPosition();

                switch (operation.direction) {
                    case "LEFT":
                        newY -= 1;
                        break;
                    case "RIGHT":
                        newY += 1;
                        break;
                    case "UP":
                        newX -= 1;
                        break;
                    case "DOWN":
                        newX += 1;
                        break;
                    default:
                        return false;
                }

                // Check if there's a hole in the specified direction
                if (!map.get(newX).get(newY).equals("H")) {
                    return false;
                }

                // Find and modify the hole
                for (int i = 0; i < holes.size(); i++) {
                    Hole hole = holes.get(i);
                    if (hole.getXPosition() == newX && hole.getYPosition() == newY) {
                        holes.remove(i);
                        hole.setDepth(hole.getDepth() - 1);
                        if (hole.getDepth() > 0) {
                            if (agent.getTile().getColor().equalsIgnoreCase(agent.getColor())) {
                                agent.setPoints(agent.getPoints() + 10);
                            }
                            holes.add(hole);
                        } else {
                            if (agent.getTile().getColor().equalsIgnoreCase(agent.getColor())) {
                                agent.setPoints(agent.getPoints() + 40);
                            }
                        }
                        agent.setTile(null);
                        initialiseMap();
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private Boolean transfer(String author, Operation operation) {

        // remove points from agent transferring
        for(int i = 0; i < numberOfAgents; i++) {
            if(author.equalsIgnoreCase(agents.get(i).name)) {
                if(agents.get(i).points - operation.transferPoints < 0)
                    return false;
                agents.get(i).points -= operation.transferPoints;
                break;
            }
        }

        // add points to agent transferring to
        for(int i = 0; i < numberOfAgents; i++) {
            if(operation.toAgent.equalsIgnoreCase(agents.get(i).name)) {
                agents.get(i).points += operation.transferPoints;
                return true;
            }
        }

        return false;
    }

    private boolean canMoveTo(Map<String, Integer> position) {

        int x = position.getOrDefault("x", 0);  // Handle potential missing key
        int y = position.getOrDefault("y", 0);  // Handle potential missing key

        // Check for valid grid coordinates and allowed characters
        return (x >= 0 && x <= gridHeight &&
                y >= 0 && y <= gridWidth &&
                !map.get(x).contains("H") &&
                !map.get(x).contains("O"));
    }

    public void initialiseMap() {

        map = new ArrayList<>();

        // Create empty grid
        for (int i = 0; i < gridHeight; i++) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < gridWidth; j++) {
                row.add("E");
            }
            map.add(row);
        }

        // Mark agents
        for (Agent agent : agents) {
            map.get(agent.xPosition).set(agent.yPosition, "A");

        }

        // Mark tiles
        for (Tile tile : tiles) {
            for (int j = 0; j < tile.numberOfTiles; j++) {
                String currentCell = map.get(tile.xPosition).get(tile.yPosition);
                map.get(tile.xPosition).set(tile.yPosition, currentCell + "T");
            }
        }

        // Mark holes
        for (Hole hole : holes) {
            map.get(hole.xPosition).set(hole.yPosition, "H");
        }

        // Mark obstacles
        for (Obstacle obstacle : obstacles) {
            map.get(obstacle.xPosition).set(obstacle.yPosition, "O");
        }
    }

    public boolean isEmpty() {
        for (List<String> row : map) {
            for (String cellValue : row) {
                if (cellValue.contains("H") || cellValue.contains("T")) {
                    return false;
                }
            }
        }

        return true;
    }

    public String getScore() {
        StringBuilder scoreBuilder = new StringBuilder();

        for (Agent agent : agents) {
            scoreBuilder.append(agent.getName()).append(": ").append(agent.getPoints()).append("\n");
        }

        return scoreBuilder.toString();
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public List<List<String>> getMap() {
        return map;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public List<Hole> getHoles() {
        return holes;
    }

    public Generator getGenerator() {
        return generator;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public void setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }
}
