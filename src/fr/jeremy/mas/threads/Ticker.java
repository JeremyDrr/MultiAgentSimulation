package fr.jeremy.mas.threads;

import fr.jeremy.mas.representation.Environment;
import fr.jeremy.mas.utils.TileWorldService;

public class Ticker {

    private Environment environment;
    private TileWorldService tileWorldService;

    private volatile boolean ticked = false;
    private boolean running = true;
    private int agentsCounter = 0;

    public Ticker(Environment environment, TileWorldService tileWorldService){
        this.environment = environment;
        this.tileWorldService = tileWorldService;
    }

    public void end(){
        running = false;
        tileWorldService.updateConsole("Ended");
        tileWorldService.endGame(environment.getScore());
    }

    public boolean running(){
        return running;
    }

    public synchronized void tick(String threadName){
        ticked = true;
        agentsCounter = 0;
        notifyAll();

        System.out.println(threadName + ": Tick");
    }

    public synchronized void action(String threadName) throws InterruptedException {
        if(!ticked || (agentsCounter < environment.getAgents().size())){
            System.out.println(threadName + ": Wait for tick.");
            wait();
        }

        agentsCounter++;
        if(agentsCounter == environment.getAgents().size()){
            ticked = false;
        }
        System.out.println(threadName + ": Action");
    }
}
