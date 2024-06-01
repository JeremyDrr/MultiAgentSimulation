package fr.jeremy.mas.threads;

import fr.jeremy.mas.representation.Environment;
import fr.jeremy.mas.representation.Hole;
import fr.jeremy.mas.representation.Tile;
import fr.jeremy.mas.communication.Message;
import fr.jeremy.mas.communication.MessageBox;
import fr.jeremy.mas.utils.Distance;
import fr.jeremy.mas.utils.TileWorldService;
import fr.jeremy.mas.representation.Agent;
import fr.jeremy.mas.communication.Operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        tileWorldService.updateConsole("${agentName}: started");
    }


    @Override
    public void run() {
        while(ticker.running()){

            ticker.action(this.name);
            tileWorldService.updateConsole(this.name + ": Action");

            Map<String, List<Map<String, Object>>> distances = computeDistances(getPosition());

            Message negotiationMessage = negotiate(distances);
            notifyPrincipal(negotiationMessage);
            negotiationMessageBox.checkNegotiationMessageList(this.name);
            negotiationMessageBox.isMessageListProcessed();

            processMessageList();

            environmentMessageBox.checkMessageList(this.name);
            environmentMessageBox.isMessageListProcessed();

            processMessageList();
        }

        System.out.println(this.name + ": ended");
    }

    private Map<String, Integer> getPosition() {
        for (Agent agent : environment.agents) {
            if (this.name.equalsIgnoreCase(agent.name)) {
                Map<String, Integer> position = new HashMap<>();
                position.put("x", agent.xPosition);
                position.put("y", agent.yPosition);
                return position;
            }
        }
        return null;
    }

    private Map<String, List<Map<String, Object>>> computeDistances(Map<String, Integer> position) {

        List<Map<String, Object>> distanceToHoles = new ArrayList<>();
        for (Hole hole : environment.holes) {
            int distance = Distance.findShortestPath(new int[]{position.get("x"), position.get("y")}, new int[]{hole.xPosition, hole.yPosition}, environment.map);
            Map<String, Object> holeDistance = new HashMap<>();
            holeDistance.put("x", hole.xPosition);
            holeDistance.put("y", hole.yPosition);
            holeDistance.put("distance", distance);
            distanceToHoles.add(holeDistance);
        }

        List<Map<String, Object>> distanceToTiles = new ArrayList<>();
        for (Tile tile : environment.tiles) {
            int distance = Distance.findShortestPath(new int[]{position.get("x"), position.get("y")}, new int[]{tile.xPosition, tile.yPosition}, environment.map);
            Map<String, Object> tileDistance = new HashMap<>();
            tileDistance.put("x", tile.xPosition);
            tileDistance.put("y", tile.yPosition);
            tileDistance.put("distance", distance);
            distanceToTiles.add(tileDistance);
        }

        Map<String, List<Map<String, Object>>> distances = new HashMap<>();
        distances.put("holes", distanceToHoles);
        distances.put("tiles", distanceToTiles);

        return distances;
    }

    private void notifyPrincipal(Message message) {
        negotiationMessageBox.addMessage(message);
    }

    /**
     * Send message to the environment.
     * @param message - message to send.
     */
    private void notifyEnvironment(Message message) {
        environmentMessageBox.addMessage(message);
        return;
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
        System.out.println("move(): " + this.name + ": position=" + newPosition);
        Message message = new Message(this.name, "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation("MOVE", newPosition);
        message.setOperation(operation);
        return message;
    }

    private Message pickTile(Map<String, Integer> position) {
        System.out.println("pickTile(): " + this.name + ": position=" + position);
        Message message = new Message(this.name, "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation("PICK", position);
        message.setOperation(operation);
        return message;
    }

    private Message dropTile(Map<String, Integer> position) {
        System.out.println("dropTile(): " + this.name + ": position=" + position);
        Message message = new Message(this.name, "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation("DROP", position);
        message.setOperation(operation);
        return message;
    }

    private Message useTile(String direction) {
        System.out.println("useTile(): " + this.name + ": direction=" + direction);
        Message message = new Message(this.name, "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation("USE", direction);
        message.setOperation(operation);
        return message;
    }

    private Message transferPoints(String toAgent, Integer transferPoints) {
        System.out.println("transferPoints(): " + this.name + ": toAgent=" + toAgent + "; transferPoints=" + transferPoints);
        Message message = new Message(this.name, "OPERATION_SUCCESS_CODE");
        Operation operation = new Operation("TRANSFER", toAgent, transferPoints);
        message.setOperation(operation);
        return message;
    }

    private Message negotiate(Map<String, Object> distances) {
        System.out.println("negotiate(): " + this.name + ": distances=" + distances);
        Message message = new Message(this.name, "NEGOTIATION_RESULT");
        Operation operation = new Operation("NEGOTIATION", distances);
        message.setOperation(operation);
        return message;
    }

}
