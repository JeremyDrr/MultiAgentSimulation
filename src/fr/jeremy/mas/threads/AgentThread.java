package fr.jeremy.mas.threads;

import fr.jeremy.mas.representation.Agent;
import fr.jeremy.mas.representation.Environment;
import fr.jeremy.mas.representation.Hole;
import fr.jeremy.mas.representation.Tile;
import fr.jeremy.mas.communication.Message;
import fr.jeremy.mas.communication.MessageBox;
import fr.jeremy.mas.utils.Distance;
import fr.jeremy.mas.utils.TileWorldService;
import fr.jeremy.mas.communication.Operation;

import java.util.*;

public class AgentThread extends Thread {

    public String name;
    public Environment environment;
    public MessageBox[] agentsMessageBox;
    public MessageBox environmentMessageBox;
    public MessageBox negotiationMessageBox;
    public Ticker ticker;
    public TileWorldService tileWorldService;

    public AgentThread(MessageBox[] agentsMessageBox, MessageBox environmentMessageBox, MessageBox negotiationMessageBox, Environment environment, Ticker ticker, TileWorldService tileWorldService, String agentName) {
        this.agentsMessageBox = agentsMessageBox;
        this.environmentMessageBox = environmentMessageBox;
        this.negotiationMessageBox = negotiationMessageBox;
        this.name = agentName;
        this.environment = environment;
        this.ticker = ticker;
        this.tileWorldService = tileWorldService;
        tileWorldService.updateConsole(this.name + ": started");
    }


    @Override
    public void run() {
        while(ticker.running()){

            ticker.action(this.name);

            Map<String, Integer> position = getPosition();
            Map<String, List<Map<String, Object>>> distances = computeDistances(position);

            Message negotiationMessage = negotiate(distances);
            notifyPrincipal(negotiationMessage);

            negotiationMessageBox.checkNegotiationMessageList(this.name);
            negotiationMessageBox.isMessageListProcessed();
            processMessageList();

            environmentMessageBox.checkMessageList(this.name);
            environmentMessageBox.isMessageListProcessed();
            processMessageList();

            displayGrid();

        }

        System.out.println(this.name + ": ended");
    }

    //TODO: Might be the reason of crash
    private synchronized Map<String, Integer> getPosition() {
        String threadName = this.getName();
        System.out.println("Thread name: " + threadName);

        for (Agent agent : environment.getAgents()) {
            String agentName = agent.getName();
            System.out.println("Agent name: " + agentName);

            if (threadName.equalsIgnoreCase(agentName)) {
                Map<String, Integer> position = new HashMap<>();
                position.put("x", agent.getXPosition());
                position.put("y", agent.getYPosition());

                System.out.println("Success! Agent position found for thread: " + threadName);
                return position;
            }
        }

        System.out.println("Error: Agent position not found for thread: " + threadName);
        return null;
    }


    private void notifyPrincipal(Message message) {
        negotiationMessageBox.addMessage(message);
    }

    private void notifyEnvironment(Message message) {
        environmentMessageBox.addMessage(message);
    }

    private synchronized void processMessageList() {

        for (int i = 0; i < environment.numberOfAgents; i++) {
            if (this.name.equalsIgnoreCase(agentsMessageBox[i].getOwner())) {
                for (Message message : agentsMessageBox[i].messageList) {

                    tileWorldService.updateConsole(this.name + ": " + message);

                    switch (message.getOperation().getCode()) {
                        case "MOVE":
                            Message moveMessage = move(message.getOperation().getPosition());
                            notifyEnvironment(moveMessage);
                            break;
                        case "PICK":
                            Message pickMessage = pickTile(message.getOperation().getPosition());
                            notifyEnvironment(pickMessage);
                            break;
                        case "DROP":
                            Message dropMessage = dropTile(message.getOperation().getPosition());
                            notifyEnvironment(dropMessage);
                            break;
                        case "USE":
                            Message useMessage = useTile(message.getOperation().getDirection());
                            notifyEnvironment(useMessage);
                            break;
                        case "TRANSFER":
                            Message transferMessage = transferPoints(message.getOperation().getToAgent(), message.getOperation().getTransferPoints());
                            notifyEnvironment(transferMessage);
                            break;
                    }
                }
                agentsMessageBox[i].messageList.clear();
                break;
            }
        }
    }


    /* --- OPERATIONS --- */

    private Message move(Map<String, Integer> newPosition) {
        System.out.println("MOVE: " + this.name + ": position=" + newPosition);
        Message message = new Message(this.name, "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation("MOVE", String.valueOf(newPosition));
        message.setOperation(operation);
        return message;
    }

    private Message pickTile(Map<String, Integer> position) {
        System.out.println("PICK: " + this.name + ": position=" + position);
        Message message = new Message(this.name, "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation("PICK", String.valueOf(position));
        message.setOperation(operation);
        return message;
    }

    private Message dropTile(Map<String, Integer> position) {
        System.out.println("DROP: " + this.name + ": position=" + position);
        Message message = new Message(this.name, "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation("DROP", String.valueOf(position));
        message.setOperation(operation);
        return message;
    }

    private Message useTile(String direction) {
        System.out.println("USE: " + this.name + ": direction=" + direction);
        Message message = new Message(this.name, "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation("USE", direction);
        message.setOperation(operation);
        return message;
    }

    private Message transferPoints(String toAgent, Integer transferPoints) {
        System.out.println("TRANSFER: " + this.name + ": toAgent=" + toAgent + "; transferPoints=" + transferPoints);
        Message message = new Message(this.name, "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation("TRANSFER", toAgent, transferPoints);
        message.setOperation(operation);
        return message;
    }

    private Message negotiate(Map<String, List<Map<String, Object>>> distances) {
        System.out.println("NEGOTIATE: " + this.name + ": distances=" + distances);
        Message message = new Message(this.name, "NEGOTIATION_RESULT");
        Operation operation = new Operation("NEGOTIATION", String.valueOf(distances));
        message.setOperation(operation);
        return message;
    }

    private Map<String, List<Map<String, Object>>> computeDistances(Map<String, Integer> position) {

        List<Map<String, Object>> distanceToHoles = new ArrayList<>();
        for (Hole hole : environment.getHoles()) {

            List<String> distance = Distance.findShortestPath(
                    new int[]{position.get("x"), position.get("y")},
                    new int[]{hole.getXPosition(), hole.getYPosition()},
                    environment.getMap());

            Map<String, Object> distanceInfo = new HashMap<>();
            distanceInfo.put("x", hole.getXPosition());
            distanceInfo.put("y", hole.getYPosition());
            distanceInfo.put("distance", distance);
            distanceToHoles.add(distanceInfo);
        }

        List<Map<String, Object>> distanceToTiles = new ArrayList<>();
        for (Tile tile : environment.getTiles()) {

            List<String> distance = Distance.findShortestPath(
                    new int[]{position.get("x"), position.get("y")},
                    new int[]{tile.getXPosition(), tile.getYPosition()},
                    environment.getMap());

            Map<String, Object> distanceInfo = new HashMap<>();
            distanceInfo.put("x", tile.getXPosition());
            distanceInfo.put("y", tile.getYPosition());
            distanceInfo.put("distance", distance);
            distanceToTiles.add(distanceInfo);
        }

        Map<String, List<Map<String, Object>>> distances = new HashMap<>();
        distances.put("holes", distanceToHoles);
        distances.put("tiles", distanceToTiles);

        return distances;
    }

    private void displayGrid(){

        List<List<String>> grid = environment.getMap();
        for (int i = 0; i < grid.size(); i++) {
            System.out.println(environment.map.get(i));
        }

    }

}
