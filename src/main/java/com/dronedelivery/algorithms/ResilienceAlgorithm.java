package com.dronedelivery.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dronedelivery.backend.DroneNetwork;
import com.dronedelivery.backend.Edge;

public class ResilienceAlgorithm {
    
    public static class ResilienceResult {
        public List<String> articulationPoints;
        public List<String> bridges;
        public String details;
        
        public ResilienceResult(List<String> artPoints, List<String> bridgeList, String details) {
            this.articulationPoints = artPoints;
            this.bridges = bridgeList;
            this.details = details;
        }
    }
    
    /**
     * F4: Find articulation points (critical nodes)
     * Nodes whose removal disconnects the network
     */
    public static List<String> findArticulationPoints(DroneNetwork network) {
        Map<String, Integer> disc = new HashMap<>();
        Map<String, Integer> low = new HashMap<>();
        Map<String, Boolean> visited = new HashMap<>();
        List<String> articulationPoints = new ArrayList<>();
        int[] time = {0};
        
        // Initialize all nodes
        for (String nodeId : network.getNodes().keySet()) {
            visited.put(nodeId, false);
            disc.put(nodeId, -1);
            low.put(nodeId, -1);
        }
        
        // DFS from each unvisited node
        for (String nodeId : network.getNodes().keySet()) {
            if (!visited.get(nodeId)) {
                dfsFindArticulation(nodeId, -1, network, disc, low, visited, 
                                  articulationPoints, time);
            }
        }
        
        return articulationPoints;
    }
    
    private static void dfsFindArticulation(String u, int parent, DroneNetwork network,
                                           Map<String, Integer> disc,
                                           Map<String, Integer> low,
                                           Map<String, Boolean> visited,
                                           List<String> articulationPoints,
                                           int[] time) {
        int children = 0;
        visited.put(u, true);
        disc.put(u, time[0]);
        low.put(u, time[0]++);
        
        for (Edge edge : network.getEdgesFrom(u)) {
            if (edge.isRestricted()) continue; // Skip restricted edges
            
            String v = edge.getTo().getId();
            
            if (!visited.getOrDefault(v, false)) {
                children++;
                dfsFindArticulation(v, u.hashCode(), network, disc, low, visited, 
                                  articulationPoints, time);
                
                // Check if subtree of v has connection back to ancestor of u
                Integer lowV = low.getOrDefault(v, Integer.MAX_VALUE);
                low.put(u, Math.min(low.get(u), lowV));
                
                // Case 1: u is root and has 2+ children
                if (parent == -1 && children > 1) {
                    if (!articulationPoints.contains(u)) {
                        articulationPoints.add(u);
                    }
                }
                
                // Case 2: u is non-root and low[v] >= disc[u]
                if (parent != -1 && lowV >= disc.getOrDefault(u, Integer.MAX_VALUE)) {
                    if (!articulationPoints.contains(u)) {
                        articulationPoints.add(u);
                    }
                }
            } else if (v.hashCode() != parent) {
                Integer discV = disc.getOrDefault(v, Integer.MAX_VALUE);
                low.put(u, Math.min(low.get(u), discV));
            }
        }
    }
    
    /**
     * F4: Find bridges (critical edges)
     * Edges whose removal disconnects the network
     */
    public static List<String> findBridges(DroneNetwork network) {
        Map<String, Integer> disc = new HashMap<>();
        Map<String, Integer> low = new HashMap<>();
        Map<String, Boolean> visited = new HashMap<>();
        List<String> bridges = new ArrayList<>();
        int[] time = {0};
        
        // Initialize all nodes
        for (String nodeId : network.getNodes().keySet()) {
            visited.put(nodeId, false);
            disc.put(nodeId, -1);
            low.put(nodeId, -1);
        }
        
        // DFS from each unvisited node
        for (String nodeId : network.getNodes().keySet()) {
            if (!visited.get(nodeId)) {
                dfsFindBridges(nodeId, -1, network, disc, low, visited, bridges, time);
            }
        }
        
        return bridges;
    }
    
    private static void dfsFindBridges(String u, int parent, DroneNetwork network,
                                       Map<String, Integer> disc,
                                       Map<String, Integer> low,
                                       Map<String, Boolean> visited,
                                       List<String> bridges,
                                       int[] time) {
        visited.put(u, true);
        disc.put(u, time[0]);
        low.put(u, time[0]++);
        
        for (Edge edge : network.getEdgesFrom(u)) {
            if (edge.isRestricted()) continue;
            
            String v = edge.getTo().getId();
            
            if (!visited.getOrDefault(v, false)) {
                dfsFindBridges(v, u.hashCode(), network, disc, low, visited, bridges, time);
                
                Integer lowV = low.getOrDefault(v, Integer.MAX_VALUE);
                low.put(u, Math.min(low.get(u), lowV));
                
                // Bridge found: low[v] > disc[u]
                if (lowV > disc.getOrDefault(u, Integer.MAX_VALUE)) {
                    bridges.add(u + " -> " + v);
                }
            } else if (v.hashCode() != parent) {
                Integer discV = disc.getOrDefault(v, Integer.MAX_VALUE);
                low.put(u, Math.min(low.get(u), discV));
            }
        }
    }
    
    /**
     * Complete F4 analysis
     */
    public static ResilienceResult analyzeNetworkResilience(DroneNetwork network) {
        List<String> artPoints = findArticulationPoints(network);
        List<String> bridgeList = findBridges(network);
        
        StringBuilder details = new StringBuilder();
        details.append("NETWORK RESILIENCE ANALYSIS\n");
        details.append("============================\n\n");
        
        details.append("CRITICAL NODES (Articulation Points):\n");
        if (artPoints.isEmpty()) {
            details.append("  ✓ None - Network is robust\n");
        } else {
            details.append("  ✗ Found ").append(artPoints.size()).append(" critical nodes:\n");
            for (String node : artPoints) {
                details.append("    - ").append(node).append("\n");
            }
        }
        
        details.append("\nCRITICAL EDGES (Bridges):\n");
        if (bridgeList.isEmpty()) {
            details.append("  ✓ None - Network is robust\n");
        } else {
            details.append("  ✗ Found ").append(bridgeList.size()).append(" critical edges:\n");
            for (String bridge : bridgeList) {
                details.append("    - ").append(bridge).append("\n");
            }
        }
        
        details.append("\nRECOMMENDATIONS:\n");
        if (!artPoints.isEmpty()) {
            details.append("  1. Add backup routes around critical nodes\n");
        }
        if (!bridgeList.isEmpty()) {
            details.append("  2. Add redundant corridors for critical edges\n");
        }
        if (artPoints.isEmpty() && bridgeList.isEmpty()) {
            details.append("  ✓ Network has good redundancy\n");
        }
        
        return new ResilienceResult(artPoints, bridgeList, details.toString());
    }
}
