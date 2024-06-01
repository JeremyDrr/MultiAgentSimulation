package fr.jeremy.mas.communication;

import java.util.HashMap;
import java.util.Map;

public class Operation {

    public String code; //Works as an enum - PICK, DROP, USE, MOVE, TRANSFER, NEGOTIATE
    public String toAgent;
    public Map<String, Integer> position;
    public int transferPoints = 0;
    public String direction;
    public Map<String, Integer> distances;  // Used for negotiation

    public Operation(String code, String toAgent, Map<String, Integer> position, int transferPoints, String direction, Map<String, Integer> distances) {
        this.code = code;
        this.toAgent = toAgent;
        this.position = position;
        this.transferPoints = transferPoints;
        this.direction = direction;
        this.distances = distances;
    }

    public Operation(String code){
        this.code = code;
    }

    public Operation(String code, String direction) {
        this.code = code;
        this.direction = direction;
    }

    public Operation(String code, String toAgent, Integer transferPoints) {
        this.code = code;
        this.toAgent = toAgent;
        this.transferPoints = transferPoints;
    }

    public Operation(String code, Map<String, Object> distances) {
        this.code = code;
        this.distances = new HashMap<String, Integer>();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, Integer> getPosition() {
        return position;
    }

    public void setPosition(Map<String, Integer> position) {
        this.position = position;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Map<String, Integer> getDistances() {
        return distances;
    }

    public void setDistances(Map<String, Integer> distances) {
        this.distances = distances;
    }

    public int getTransferPoints() {
        return transferPoints;
    }

    public void setTransferPoints(int transferPoints) {
        this.transferPoints = transferPoints;
    }

    public String getToAgent() {
        return toAgent;
    }

    public void setToAgent(String toAgent) {
        this.toAgent = toAgent;
    }

    @Override
    public String toString() {
        return "Operation: " +
                "code=" + code +
                ", toAgent=" + toAgent +
                ", position=" + position +
                ", transferPoints=" + transferPoints +
                ", direction=" + direction +
                ", distances=" + distances;
    }
}
