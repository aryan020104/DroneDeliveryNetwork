package com.dronedelivery.algorithms;

import com.dronedelivery.backend.*;
import java.util.*;

public class ChargingOptimization {
    
    public static class OptimizationResult {
        public List<String> optimalStations;
        public double averageDistance;
        public String details;
        
        public OptimizationResult(List<String> stations, double avgDist, String details) {
            this.optimalStations = stations;
            this.averageDistance = avgDist;
            this.details = details;
        }
    }
    
    /**
     * F5: Place k charging stations optimally
     * Minimize average distance from all nodes to nearest charging station
     */
    public static OptimizationResult optimizeChargingStations(DroneNetwork network, int k) {
        List<String> optimalStations = new ArrayList<>();
        Set<String> nonChargingNodes = new HashSet<>();
        
        // Find all non-charging nodes
        for (Node node : network.getNodes().values()) {
            if (node.getType() != Node.NodeType.CHARGING) {
                nonChargingNodes.add(node.getId());
            }
        }
        
        // Existing charging stations
        Set<String> existingCharging = new HashSet<>();
        for (Node node : network.getNodes().values()) {
            if (node.getType() == Node.NodeType.CHARGING) {
                existingCharging.add(node.getId());
            }
        }
        
        // Greedy: select k nodes that minimize average distance
        for (int i = 0; i < k && !nonChargingNodes.isEmpty(); i++) {
            String best = null;
            double bestScore = Double.MAX_VALUE;
            
            for (String candidate : nonChargingNodes) {
                Set<String> allStations = new HashSet<>(existingCharging);
                allStations.addAll(optimalStations);
                allStations.add(candidate);
                
                double avgDist = calculateAverageDistanceToCharging(network, allStations);
                
                if (avgDist < bestScore) {
                    bestScore = avgDist;
                    best = candidate;
                }
            }
            
            if (best != null) {
                optimalStations.add(best);
                nonChargingNodes.remove(best);
            }
        }
        
        Set<String> finalStations = new HashSet<>(existingCharging);
        finalStations.addAll(optimalStations);
        double finalAvgDist = calculateAverageDistanceToCharging(network, finalStations);
        
        StringBuilder details = new StringBuilder();
        details.append("CHARGING STATION OPTIMIZATION\n");
        details.append("=============================\n\n");
        details.append("Stations to add: ").append(k).append("\n");
        details.append("Recommended locations:\n");
        for (String station : optimalStations) {
            details.append("  + ").append(station).append(" (").append(network.getNode(station).getType()).append(")\n");
        }
        details.append("\nExisting charging stations: ").append(existingCharging.size()).append("\n");
        details.append("Total after optimization: ").append(finalStations.size()).append("\n");
        details.append("Average distance to nearest charging station: ").append(
            String.format("%.2f", finalAvgDist)).append(" units\n");
        
        return new OptimizationResult(optimalStations, finalAvgDist, details.toString());
    }
    
    private static double calculateAverageDistanceToCharging(DroneNetwork network, 
                                                             Set<String> chargingStations) {
        double totalDist = 0;
        int count = 0;
        
        for (Node node : network.getNodes().values()) {
            if (!chargingStations.contains(node.getId())) {
                int minDist = Integer.MAX_VALUE;
                
                for (String chargingId : chargingStations) {
                    PathResult path = network.findShortestPath(node.getId(), chargingId);
                    if (path != null) {
                        minDist = Math.min(minDist, path.getTotalEnergy());
                    }
                }
                
                if (minDist != Integer.MAX_VALUE) {
                    totalDist += minDist;
                    count++;
                }
            }
        }
        
        return count > 0 ? totalDist / count : 0;
    }
}
