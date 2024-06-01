package fr.jeremy.mas.threads;

import fr.jeremy.mas.communication.Operation;
import fr.jeremy.mas.communication.OperationType;
import fr.jeremy.mas.representation.Environment;
import fr.jeremy.mas.utils.TileWorldService;

import java.util.Random;

public class GeneratorThread extends Thread{

    public String name;
    public Environment environment;
    public Ticker ticker;
    public TileWorldService tileWorldService;
    public long totalTime = 0;

    public GeneratorThread(Environment environment, Ticker ticker, TileWorldService tileWorldService) {
        this.name = "generator";
        this.environment = environment;
        this.ticker = ticker;
        this.tileWorldService = tileWorldService;
        tileWorldService.updateConsole("Generator: started");
    }

    @Override
    public void run() {

        String[] colors = new String[environment.numberOfAgents];
        for(int i = 0; i < environment.agents.size(); i++) {
            colors[i] = environment.agents.get(i).color;
        }

        while (ticker.running()){
            long nextRandomTime = getNextRandomTime();
            tileWorldService.updateConsole(this.name + ": Next group random generation in " + nextRandomTime);

            try{
                Thread.sleep(nextRandomTime);
            }catch(InterruptedException e){
                e.printStackTrace();
            }

            environment.executeOperation("generator", new Operation("ADD"));
            tileWorldService.updateTileWorld(environment);

            totalTime += nextRandomTime;
            System.out.println("Generator: " + totalTime);

            if(totalTime > environment.totalTime){
                break;
            }
        }

        System.out.println(this.name + ": ended");
    }

    private long getNextRandomTime() {

        long generatorStartTime = environment.generator.generatorStartTime;
        long generatorEndTime = environment.generator.generatorEndTime;
        Random r = new Random();
        long nextRandomTime = generatorStartTime + ((long)(r.nextDouble() * (generatorEndTime - generatorStartTime)));

        return nextRandomTime;
    }
}
