package fr.jeremy.mas.threads;

import fr.jeremy.mas.representation.Environment;
import fr.jeremy.mas.communication.Message;
import fr.jeremy.mas.communication.MessageBox;
import fr.jeremy.mas.utils.TileWorldService;

import java.util.List;

public class EnvironmentThread extends Thread {

    public String name;
    MessageBox[] agentsMessageBox;
    MessageBox environmentMessageBox;
    Environment environment;
    Ticker ticker;
    TileWorldService tileWorldService;

    public EnvironmentThread(MessageBox[] agentsMessageBox, MessageBox environmentMessageBox, Environment environment, Ticker ticker, TileWorldService tileWorldService) {
        this.agentsMessageBox = agentsMessageBox;
        this.environmentMessageBox = environmentMessageBox;
        this.name = "environment";
        this.environment = environment;
        this.ticker = ticker;
        this.tileWorldService = new TileWorldService();
        tileWorldService.updateConsole("Environment: Started");
    }

    @Override
    public void run() {

        while (environment.remainingTime > 0){

            if(environment.generator == null && environment.isEmpty()) break;

            long startTime = System.currentTimeMillis() % 1000;
            ticker.tick(this.name);
            environmentMessageBox.checkMessageList(this.name);
            processMessageList();
            tileWorldService.updateTileWorld(environment);
            long endTime = System.currentTimeMillis() % 1000 - startTime;

            if(environment.tickTime - endTime > 0){
                try {
                    Thread.sleep(environment.tickTime - endTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            environment.remainingTime -= environment.tickTime;

        }

        System.out.println(this.name + ": ended");
        ticker.end();
    }

    private void processMessageList() {
        for (Message message : environmentMessageBox.getMessageList()) {
            tileWorldService.updateConsole("environment: " + message.toString());
            processMessage(message);
        }

        environmentMessageBox.emptyMessageList(this.name);
    }

    private void processMessage(Message message) {

        try {

            Message confirmation = new Message("environment", message.operation.code);
            if(environment.executeOperation(message.sender, message.operation)) {
                confirmation.successCode = "SUCCESS";
            } else {
                confirmation.successCode = "ERROR";
            }

            for(int i = 0; i < environment.numberOfAgents; i++) {
                if(message.sender.equalsIgnoreCase(agentsMessageBox[i].getOwner())) {
                    agentsMessageBox[i].addStatusMessage(confirmation);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
