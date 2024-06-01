package fr.jeremy.mas.representation;

public class Tile {

    public long lifetime;
    public int numberOfTiles;
    public String color;
    public int xPosition;
    public int yPosition;

    public Tile(){

    }

    public Tile(String color){
        this.color = color;
    }

    public Tile(int numberOfTiles, String color, int xPosition, int yPosition, long lifetime){
        this.numberOfTiles = numberOfTiles;
        this.color = color;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.lifetime = lifetime;
    }

    public int getXPosition() {
        return xPosition;
    }

    public void setXPosition(int xPosition) {
        this.xPosition = xPosition;
    }

    public int getYPosition() {
        return yPosition;
    }

    public void setYPosition(int yPosition) {
        this.yPosition = yPosition;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getNumberOfTiles() {
        return numberOfTiles;
    }

    public void setNumberOfTiles(int numberOfTiles) {
        this.numberOfTiles = numberOfTiles;
    }
}
