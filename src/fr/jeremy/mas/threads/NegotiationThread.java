package fr.jeremy.mas.threads;

import fr.jeremy.mas.communication.Message;
import fr.jeremy.mas.communication.MessageBox;
import fr.jeremy.mas.communication.Operation;
import fr.jeremy.mas.representation.Agent;
import fr.jeremy.mas.representation.Environment;
import fr.jeremy.mas.utils.TileWorldService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
        tileWorldService.updateConsole("${agentName}: started.");
    }

    @Override
    public void run() {

        while(ticker.running()){
            try {
                negotiationMessageBox.checkNegotiationMessageList(this.name);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            computeNegotiationResult();
        }

        System.out.println(this.name + ": ended");
    }

    /* --- OPERATIONS --- */

    private Message move(Map<String, Integer> newPosition) {
        System.out.println("move(): " + this.name + ": position=" + newPosition);
        Message message = new Message(this.name, "NEGOTIATION");
        Operation operation = new Operation("MOVE", newPosition);
        message.setOperation(operation);
        return message;
    }

    private Message pickTile(Map<String, Integer> position) {
        System.out.println("pickTile(): " + this.name + ": position=" + position);
        Message message = new Message(this.name, "NEGOTIATION");
        Operation operation = new Operation("PICK", position);
        message.setOperation(operation);
        return message;
    }

    private Message dropTile(Map<String, Integer> position) {
        System.out.println("dropTile(): " + this.name + ": position=" + position);
        Message message = new Message(this.name, "NEGOTIATION");
        Operation operation = new Operation("DROP", position);
        message.setOperation(operation);
        return message;
    }

    private Message useTile(String direction) {
        System.out.println("useTile(): " + this.name + ": direction=" + direction);
        Message message = new Message(this.name, "NEGOTIATION");
        Operation operation = new Operation("USE", direction);
        message.setOperation(operation);
        return message;
    }

    private Message transferPoints(String toAgent, Integer transferPoints) {
        System.out.println("transferPoints(): " + this.name + ": toAgent=" + toAgent + "; transferPoints=" + transferPoints);
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

    private void notifyAgent(String agentName, Message message) {
        System.out.println("notifyAgent: message=${message}");
        for(int i = 0; i < agentsMessageBox.length; i++) {
            if(agentsMessageBox[i].getOwner().equalsIgnoreCase(agentName)) {
                agentsMessageBox[i].addMessage(message);
                return;
            }
        }
    }

}
