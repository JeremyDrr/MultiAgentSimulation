package fr.jeremy.mas.threads;

import fr.jeremy.mas.communication.Message;
import fr.jeremy.mas.communication.MessageBox;
import fr.jeremy.mas.communication.Operation;
import fr.jeremy.mas.representation.Agent;
import fr.jeremy.mas.representation.Environment;
import fr.jeremy.mas.utils.TileWorldService;

import java.util.*;

public class NegotiationThread extends Thread{

    public String name;
    public Environment environment;
    public MessageBox[] agentsMessageBox;
    public MessageBox negotiationMessageBox;
    public Ticker ticker;
    public TileWorldService tileWorldService;

    public NegotiationThread(MessageBox[] agentsMessageBox, MessageBox negotiationMessageBox, Environment environment, Ticker ticker, TileWorldService tileWorldService, String agentName) {
        this.agentsMessageBox = agentsMessageBox;
        this.negotiationMessageBox = negotiationMessageBox;
        this.name = agentName;
        this.environment = environment;
        this.ticker = ticker;
        this.tileWorldService = tileWorldService;
        tileWorldService.updateConsole("Negotiation: started.");
    }

    @Override
    public void run() {

        while(ticker.running()){
            try {
                negotiationMessageBox.checkNegotiationMessageList(this.name);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            computeNegotiationResult();
        }

        System.out.println(this.name + ": ended");
    }

    /* --- OPERATIONS --- */

    private Message move(int[] newPosition) {
        System.out.println("MOVE: " + this.name + ": position=" + newPosition);
        Message message = new Message(this.name, "NEGOTIATION");
        Operation operation = new Operation("MOVE", String.valueOf(newPosition));
        message.setOperation(operation);
        return message;
    }

    private Message pickTile(Map<String, Integer> position) {
        System.out.println("PICK: " + this.name + ": position=" + position);
        Message message = new Message(this.name, "NEGOTIATION");
        Operation operation = new Operation("PICK", String.valueOf(position));
        message.setOperation(operation);
        return message;
    }

    private Message dropTile(Map<String, Integer> position) {
        System.out.println("DROP: " + this.name + ": position=" + position);
        Message message = new Message(this.name, "NEGOTIATION");
        Operation operation = new Operation("DROP", String.valueOf(position));
        message.setOperation(operation);
        return message;
    }

    private Message useTile(String direction) {
        System.out.println("USE: " + this.name + ": direction=" + direction);
        Message message = new Message(this.name, "NEGOTIATION");
        Operation operation = new Operation("USE", direction);
        message.setOperation(operation);
        return message;
    }

    private Message transferPoints(String toAgent, Integer transferPoints) {
        System.out.println("TRANSFER: " + this.name + ": toAgent=" + toAgent + "; transferPoints=" + transferPoints);
        Message message = new Message(this.name, "NEGOTIATION");
        Operation operation = new Operation("TRANSFER", toAgent, transferPoints);
        message.setOperation(operation);
        return message;
    }

    private void moveRandom(Agent agent) {

        List<String> directions = Arrays.asList("UP", "DOWN", "LEFT", "RIGHT");
        Random random = new Random();
        String randomDirection = directions.get(random.nextInt(directions.size()));
        Message message;

        switch (randomDirection) {
            case "UP":
                message = move(new int[]{agent.getXPosition() - 1, agent.getYPosition()});
                break;
            case "DOWN":
                message = move(new int[]{agent.getXPosition() + 1, agent.getYPosition()});
                break;
            case "LEFT":
                message = move(new int[]{agent.getXPosition(), agent.getYPosition() - 1});
                break;
            case "RIGHT":
                message = move(new int[]{agent.getXPosition(), agent.getYPosition() + 1});
                break;
            default:
                message = move(new int[]{agent.getXPosition(), agent.getYPosition()});
        }

        notifyAgent(agent.getName(), message);
    }

    private void computeNegotiationResult() {
        System.out.println(this.getName() + " moved");

        List<Map<String, Object>> distances = new ArrayList<>();
        for (Message message : negotiationMessageBox.getMessageList()) {
            tileWorldService.updateConsole(this.getName() + ": " + message);
            if ("NEGOTIATION".equalsIgnoreCase(message.getOperation().getCode())) {
                Map<String, Object> distanceMap = new HashMap<>();
                distanceMap.put("owner", message.getSender());
                distanceMap.put("distances", message.getOperation().getDistances());
                distances.add(distanceMap);
            }
        }

        System.out.println(this.getName() + " moved here: distances=" + distances);

        List<List<String>> map = environment.getMap();
        for (Agent agent : environment.getAgents()) {
            decideAgentAction(agent, map, distances);
        }

        // notify messages list processing ended
        negotiationMessageBox.emptyMessageList(this.getName());
    }

    private void decideAgentAction(Agent agent, List<List<String>> map, List<Map<String, Object>> distances) {
        System.out.println("DECIDE: agentName=" + agent.getName() + "| hasTile=" + agent.getTile() + "| map=" + map + "| distances=" + distances);

        if (agent.getTile() == null && map.get(agent.getXPosition()).get(agent.getYPosition()).contains("T")) {

            Message message = pickTile(Map.of("x", agent.getXPosition(), "y", agent.getYPosition()));

            notifyAgent(agent.getName(), message);

        } else if (agent.getTile() != null) {

            String direction = "";

            if (agent.getXPosition() - 1 >= 0 && map.get(agent.getXPosition() - 1).get(agent.getYPosition()).contains("H"))
                direction = "UP";

            if (agent.getXPosition() + 1 < environment.getGridHeight() && map.get(agent.getXPosition() + 1).get(agent.getYPosition()).contains("H"))
                direction = "DOWN";

            if (agent.getYPosition() - 1 >= 0 && map.get(agent.getXPosition()).get(agent.getYPosition() - 1).contains("H"))
                direction = "LEFT";

            if (agent.getYPosition() + 1 < environment.getGridWidth() && map.get(agent.getXPosition()).get(agent.getYPosition() + 1).contains("H"))
                direction = "RIGHT";

            if (!direction.isEmpty()) {
                Message message = useTile(direction);
                notifyAgent(agent.getName(), message);

            } else {
                for (Map<String, Object> dist : distances) {
                    if (dist.get("owner").toString().equalsIgnoreCase(agent.getName())) {
                        int[] position = getDirectionToClosestElement(agent, (List<Map<String, Object>>) dist.get("distances.holes"));
                        if (position != null) {
                            Message message = move(position);
                            notifyAgent(agent.getName(), message);
                        } else {
                            moveRandom(agent);
                        }
                    }
                }
            }
        } else {
            for (Map<String, Object> dist : distances) {
                if (dist.get("owner").toString().equalsIgnoreCase(agent.getName())) {
                    int[] position = getDirectionToClosestElement(agent, (List<Map<String, Object>>) dist.get("distances.tiles"));
                    if (position != null) {
                        Message message = move(position);
                        notifyAgent(agent.getName(), message);
                        return;
                    } else {
                        moveRandom(agent);
                    }
                }
            }
        }
    }

    private int[] getDirectionToClosestElement(Agent agent, List<Map<String, Object>> elements) {
        int size = environment.getGridHeight() * environment.getGridWidth();
        int k = 0;

        if (elements == null || elements.isEmpty()) return null;

        for (int i = 0; i < elements.size(); i++) {
            Map<String, Object> element = elements.get(i);
            if (element.get("distance") != null && ((List<?>) element.get("distance")).size() < size) {
                size = ((List<?>) element.get("distance")).size();
                k = i;
            }
        }

        if (elements.get(k) != null && elements.get(k).get("distance") != null && !((List<?>) elements.get(k).get("distance")).isEmpty()) {
            String direction = ((List<String>) elements.get(k).get("distance")).get(0);
            switch (direction.toLowerCase()) {
                case "north":
                    return new int[]{agent.getXPosition() - 1, agent.getYPosition()};
                case "south":
                    return new int[]{agent.getXPosition() + 1, agent.getYPosition()};
                case "east":
                    return new int[]{agent.getXPosition(), agent.getYPosition() + 1};
                case "west":
                    return new int[]{agent.getXPosition(), agent.getYPosition() - 1};
            }
        }

        return null;
    }



    private void notifyAgent(String agentName, Message message) {
        System.out.println("notifyAgent: message=" + message + ")");
        for(int i = 0; i < agentsMessageBox.length; i++) {
            if(agentsMessageBox[i].getOwner().equalsIgnoreCase(agentName)) {
                agentsMessageBox[i].addMessage(message);
                return;
            }
        }
    }

}
