package com.dronedelivery.backend;

import java.util.List;

public class PathResult {
    private List<String> path;
    private int totalEnergy;
    
    public PathResult(List<String> path, int totalEnergy) {
        this.path = path;
        this.totalEnergy = totalEnergy;
    }
    
    public List<String> getPath() {
        return path;
    }
    
    public int getTotalEnergy() {
        return totalEnergy;
    }
    
    @Override
    public String toString() {
        return "Path: " + String.join(" → ", path) + "\n" +
               "Total Energy: " + totalEnergy;
    }
}
