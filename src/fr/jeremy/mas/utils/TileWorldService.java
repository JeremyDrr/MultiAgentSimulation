package fr.jeremy.mas.utils;

import fr.jeremy.mas.communication.MessageBox;
import fr.jeremy.mas.representation.*;
import fr.jeremy.mas.threads.*;

import javax.naming.ConfigurationException;
import java.util.*;

public class TileWorldService {

    public Environment getConfiguration(String configuration) throws ConfigurationException {

        try{

            boolean dynamic = false;
            Environment environment = new Environment();
            List<String> configurationVariables = new ArrayList<String>();

            Scanner scanner = new Scanner(configuration);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                configurationVariables.addAll(Arrays.asList(line.split("\\s+")));
            }

            scanner.close();

            // Environment
            environment.numberOfAgents = Integer.parseInt(configurationVariables.get(0));
            environment.tickTime = Long.parseLong(configurationVariables.get(1));
            environment.totalTime = Long.parseLong(configurationVariables.get(2));
            environment.remainingTime = environment.totalTime;
            environment.gridWidth = Integer.parseInt(configurationVariables.get(3));
            environment.gridHeight = Integer.parseInt(configurationVariables.get(4));

            int k = 5;

            // Agent color and name
            for(int i = 0; i < environment.numberOfAgents; i++){
                Agent agent = new Agent(configurationVariables.get(k), configurationVariables.get(k));
                environment.agents.add(agent);
                k++;
            }

            // Agent position
            int agentIndex = 0;
            for(int i = 0; i < environment.numberOfAgents; i++){
                environment.agents.get(agentIndex).xPosition = Integer.parseInt(configurationVariables.get(k));
                environment.agents.get(agentIndex).xPosition = Integer.parseInt(configurationVariables.get(k+1));
                k += 2;
                agentIndex++;
            }

            // Obstacles
            if (!"obstacles".equalsIgnoreCase(configurationVariables.get(k))) {
                throw new ConfigurationException("Expected 'obstacles' keyword at position " + k);
            } else {
                k++;
                while (!"tiles".equalsIgnoreCase(configurationVariables.get(k)) && !"generator".equalsIgnoreCase(configurationVariables.get(k))) {
                    int xPosition = Integer.parseInt(configurationVariables.get(k));
                    int yPosition = Integer.parseInt(configurationVariables.get(k + 1));
                    Obstacle obstacle = new Obstacle(xPosition, yPosition);
                    environment.getObstacles().add(obstacle);
                    k += 2;
                }
            }

            // generator - bonus
            if ("generator".equalsIgnoreCase(configurationVariables.get(k))) {
                dynamic = true;
                Generator generator = new Generator();
                generator.generatorStartTime = Integer.parseInt(configurationVariables.get(k + 1));
                generator.generatorEndTime = Integer.parseInt(configurationVariables.get(k + 2));
                generator.generatorMinLifetime = Integer.parseInt(configurationVariables.get(k + 3));
                generator.generatorMaxLifetime = Integer.parseInt(configurationVariables.get(k + 4));
                environment.generator = generator;
                k += 4;
            } else if ("tiles".equalsIgnoreCase(configurationVariables.get(k))) {
                k++;
                while (!"holes".equalsIgnoreCase(configurationVariables.get(k))) {
                    Tile tile = new Tile();
                    tile.numberOfTiles = Integer.parseInt(configurationVariables.get(k));
                    tile.color = configurationVariables.get(k + 1);
                    tile.xPosition = Integer.parseInt(configurationVariables.get(k + 2));
                    tile.yPosition = Integer.parseInt(configurationVariables.get(k + 3));
                    environment.tiles.add(tile);
                    k += 4;
                }
            } else {
                throw new ConfigurationException("Missing 'tiles' or 'generator' keyword");
            }

            if (!dynamic) {
                // holes
                if (!"holes".equalsIgnoreCase(configurationVariables.get(k))) {
                    throw new ConfigurationException("Missing 'holes' keyword");
                } else {
                    k++;
                    for (int i = k; i < configurationVariables.size(); i++) {
                        Hole hole = new Hole();
                        hole.depth = Integer.parseInt(configurationVariables.get(i));
                        hole.color = configurationVariables.get(i + 1);
                        hole.xPosition = Integer.parseInt(configurationVariables.get(i + 2));
                        hole.yPosition = Integer.parseInt(configurationVariables.get(i + 3));
                        environment.holes.add(hole);
                        i += 3;
                    }
                }
            }

            environment.initialiseMap();

            return environment;

        }catch (Exception e){
            e.printStackTrace();
            throw new ConfigurationException(e.getMessage());
        }
    }

    public void initialise(Environment environment) throws InterruptedException {

        Thread.sleep(5000);
        Ticker ticker = new Ticker(environment, this);

        // Message Boxes
        MessageBox[] agentsMessageBox = new MessageBox[environment.numberOfAgents];
        for(int i = 0; i < agentsMessageBox.length; i++){
            agentsMessageBox[i] = new MessageBox(environment.agents.get(i).name, environment.numberOfAgents, this);
        }
        MessageBox environmentMessageBox = new MessageBox("environment", environment.numberOfAgents, this);
        MessageBox negotiationMessageBox = new MessageBox("principal", environment.numberOfAgents, this);

        // Create agents and start them
        List<AgentThread> agentThreads = new ArrayList<>();
        for(int i = 0; i < environment.agents.size(); i++){
            AgentThread agent = new AgentThread(agentsMessageBox, environmentMessageBox, negotiationMessageBox, environment, ticker, this, environment.agents.get(i).name);
            agentThreads.add(agent);
            agent.start();
        }

        // Random generation of tiles
        if(environment.generator != null){
            GeneratorThread generatorThread = new GeneratorThread(environment, ticker, this);
            generatorThread.start();
        }

        // Start negotiation thread
        NegotiationThread negotiationThread = new NegotiationThread(agentsMessageBox, negotiationMessageBox, environment, ticker, this, "principal");
        negotiationThread.start();

        // Start environment thread
        EnvironmentThread environmentThread = new EnvironmentThread(agentsMessageBox, environmentMessageBox, environment, ticker, this);
        environmentThread.start();

        // Events to update UI
        updateTileWorld(environment);
        updateConsole("The simulation has started");
    }

    public void updateConsole(String message) {

        long currentTimeMillis = System.currentTimeMillis();
        String logMessage = currentTimeMillis + " -> " + message;
        System.out.println(logMessage);

        Map<String, Object> data = new HashMap<>();
        data.put("message", logMessage);

    }

    public void updateTileWorld(Environment environment) {

        for(Tile tile : environment.getTiles()){
            System.out.println("Updating Tile: " + tile);
        }

        for(Hole hole : environment.getHoles()){
            System.out.println("Updating Hole: " + hole);
        }

        for(Agent agent : environment.getAgents()){
            System.out.println("Updating Agent: " + agent);
        }

        for (Obstacle obstacle : environment.getObstacles()){
            System.out.println("Updating Obstacle: " + obstacle);
        }

        if(environment.generator != null){
            Generator generator = environment.getGenerator();
            System.out.println("Updating generator: " + generator);
        }

        System.out.println("Environment updated. Current state: ");
        System.out.println("Agents: " + environment.getAgents());
        System.out.println("Tiles: " + environment.getTiles());
        System.out.println("Holes: " + environment.getHoles());
        System.out.println("Obstacles: " + environment.getObstacles());
    }

    public void endGame(String message) {
        long currentTimeMillis = System.currentTimeMillis();
        System.out.println(currentTimeMillis + " -> " + message);

        // Replace 'event(key: "endGame", for: 'browser', data: data)' with your event sending method call
        // This might involve passing the event key ("endGame"), target ('browser'), and the message data.

        // Assuming you don't need further processing after sending the event, terminate the program execution.
        System.exit(0);
    }

}
