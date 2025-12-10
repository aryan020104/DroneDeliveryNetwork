package com.dronedelivery.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.dronedelivery.backend.DroneNetwork;
import com.dronedelivery.backend.Edge;

public class MaxFlowAlgorithm {
    
    public static class FlowResult {
        public int maxFlow;
        public String details;
        
        public FlowResult(int maxFlow, String details) {
            this.maxFlow = maxFlow;
            this.details = details;
        }
    }
    
    /**
     * F3: Calculate delivery capacity using Edmonds-Karp (BFS-based max flow)
     * How many drones can simultaneously deliver without exceeding corridor capacities?
     */
    public static FlowResult calculateDeliveryCapacity(DroneNetwork network, String hubId, 
                                                       Set<String> deliveryPointIds) {
        if (!network.getNodes().containsKey(hubId)) {
            return new FlowResult(0, "Hub not found");
        }
        
        // Create residual graph
        Map<String, Map<String, Integer>> residualGraph = createResidualGraph(network);
        
        // Add super sink connected to all delivery points
        String superSink = "SUPER_SINK";
        for (String deliveryId : deliveryPointIds) {
            residualGraph.putIfAbsent(deliveryId, new HashMap<>());
            residualGraph.putIfAbsent(superSink, new HashMap<>());
            residualGraph.get(deliveryId).put(superSink, Integer.MAX_VALUE / 2); // Large capacity
            residualGraph.get(superSink).put(deliveryId, 0);
        }
        
        // Run Edmonds-Karp algorithm
        int maxFlow = 0;
        int iterations = 0;
        
        while (true) {
            Map<String, String> parent = new HashMap<>();
            
            // BFS to find augmenting path
            if (!bfsForFlow(residualGraph, hubId, superSink, parent)) {
                break; // No more augmenting paths
            }
            
            // Find minimum capacity (bottleneck) in path
            int pathFlow = Integer.MAX_VALUE;
            String current = superSink;
            
            while (!current.equals(hubId)) {
                String prev = parent.get(current);
                pathFlow = Math.min(pathFlow, residualGraph.get(prev).get(current));
                current = prev;
            }
            
            // Update residual capacities
            current = superSink;
            while (!current.equals(hubId)) {
                String prev = parent.get(current);
                residualGraph.get(prev).put(current, 
                    residualGraph.get(prev).get(current) - pathFlow);
                residualGraph.get(current).put(prev, 
                    residualGraph.get(current).getOrDefault(prev, 0) + pathFlow);
                current = prev;
            }
            
            maxFlow += pathFlow;
            iterations++;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("Edmonds-Karp Algorithm:\n");
        details.append("Source: ").append(hubId).append("\n");
        details.append("Destinations: ").append(deliveryPointIds.size()).append(" delivery points\n");
        details.append("Augmenting paths found: ").append(iterations).append("\n");
        details.append("Maximum simultaneous drones: ").append(maxFlow);
        
        return new FlowResult(maxFlow, details.toString());
    }
    
    private static Map<String, Map<String, Integer>> createResidualGraph(DroneNetwork network) {
        Map<String, Map<String, Integer>> graph = new HashMap<>();
        
        // Initialize all nodes
        for (String nodeId : network.getNodes().keySet()) {
            graph.putIfAbsent(nodeId, new HashMap<>());
        }
        
        // Add all edges with their capacities
        for (Edge edge : network.getEdges()) {
            if (!edge.isRestricted()) { // Skip restricted edges
                String from = edge.getFrom().getId();
                String to = edge.getTo().getId();
                
                graph.get(from).put(to, edge.getCapacity());
                graph.get(to).putIfAbsent(from, 0);
            }
        }
        
        return graph;
    }
    
    private static boolean bfsForFlow(Map<String, Map<String, Integer>> graph, 
                                      String source, String sink, 
                                      Map<String, String> parent) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(source);
        visited.add(source);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            for (String neighbor : graph.getOrDefault(current, new HashMap<>()).keySet()) {
                if (!visited.contains(neighbor) && 
                    graph.get(current).get(neighbor) > 0) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    
                    if (neighbor.equals(sink)) {
                        return true;
                    }
                    
                    queue.add(neighbor);
                }
            }
        }
        
        return false;
    }
}
