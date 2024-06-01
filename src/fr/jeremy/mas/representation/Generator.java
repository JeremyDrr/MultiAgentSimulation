package fr.jeremy.mas.representation;

public class Generator {

    public int generatorStartTime;
    public int generatorEndTime;
    public int generatorMinLifetime;
    public int generatorMaxLifetime;

    public int getGeneratorStartTime() {
        return generatorStartTime;
    }

    public void setGeneratorStartTime(int generatorStartTime) {
        this.generatorStartTime = generatorStartTime;
    }

    public int getGeneratorEndTime() {
        return generatorEndTime;
    }

    public void setGeneratorEndTime(int generatorEndTime) {
        this.generatorEndTime = generatorEndTime;
    }

    public int getGeneratorMinLifetime() {
        return generatorMinLifetime;
    }

    public void setGeneratorMinLifetime(int generatorMinLifetime) {
        this.generatorMinLifetime = generatorMinLifetime;
    }

    public int getGeneratorMaxLifetime() {
        return generatorMaxLifetime;
    }

    public void setGeneratorMaxLifetime(int generatorMaxLifetime) {
        this.generatorMaxLifetime = generatorMaxLifetime;
    }
}
