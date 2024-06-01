package fr.jeremy.mas;

import fr.jeremy.mas.representation.Environment;
import fr.jeremy.mas.utils.TileWorldService;

import javax.naming.ConfigurationException;

public class Main {
    public static void main(String[] args) throws ConfigurationException, InterruptedException {

        String inputFile = args.length > 0 ? args[0] : "system.txt";

        TileWorldService tileWorldService = new TileWorldService();
        Environment environment = tileWorldService.getConfiguration(inputFile);

        tileWorldService.initialise(environment);




    }
}
