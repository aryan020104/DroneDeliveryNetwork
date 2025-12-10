package com.dronedelivery.test;

import java.util.HashSet;
import java.util.Set;

import com.dronedelivery.algorithms.ChargingOptimization;
import com.dronedelivery.algorithms.CommunicationNetwork;
import com.dronedelivery.algorithms.MaxFlowAlgorithm;
import com.dronedelivery.algorithms.ResilienceAlgorithm;
import com.dronedelivery.backend.DroneNetwork;
import com.dronedelivery.backend.Node;
import com.dronedelivery.backend.PathResult;
import com.dronedelivery.io.JsonHandler;

public class AlgorithmTest {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Drone Network Algorithms ===\n");
        
        // Load network
        JsonHandler handler = new JsonHandler();
        DroneNetwork network = handler.loadNetwork("src/main/resources/networks/sample1.json");
        
        if (network == null) {
            System.err.println("Failed to load network!");
            return;
        }
        
        System.out.println("Network loaded: " + network.getNodes().size() + " nodes, " 
                          + network.getEdges().size() + " edges\n");
        
        // Test F1: Reachability
        System.out.println("=== F1: Reachability Test ===");
        if (network.getDistributors().isEmpty()) {
            System.out.println("No distributors found!");
        } else {
            Node hub = network.getDistributors().get(0);
            System.out.println("Testing reachability from: " + hub.getId());
            for (Node delivery : network.getDeliveryPoints()) {
                boolean reachable = network.isReachable(hub.getId(), delivery.getId());
                System.out.println("  " + delivery.getId() + ": " + (reachable ? "REACHABLE" : "NOT REACHABLE"));
            }
        }
        
        // Test F2: Shortest Path
        System.out.println("\n=== F2: Shortest Path Test ===");
        if (network.getNodes().size() >= 2) {
            String[] nodeIds = network.getNodes().keySet().toArray(new String[0]);
            PathResult result = network.findShortestPath(nodeIds[0], nodeIds[1]);
            if (result != null) {
                System.out.println("Path found: " + String.join(" -> ", result.getPath()));
                System.out.println("Energy cost: " + result.getTotalEnergy());
            } else {
                System.out.println("No path found");
            }
        }
        
        // Test F3: Capacity
        System.out.println("\n=== F3: Capacity Check ===");
        Set<String> deliveryIds = new HashSet<>();
        for (Node node : network.getDeliveryPoints()) {
            deliveryIds.add(node.getId());
        }
        
        if (!deliveryIds.isEmpty() && !network.getDistributors().isEmpty()) {
            String hubId = network.getDistributors().get(0).getId();
            System.out.println("Testing capacity from: " + hubId);
            MaxFlowAlgorithm.FlowResult flowResult = 
                MaxFlowAlgorithm.calculateDeliveryCapacity(network, hubId, deliveryIds);
            System.out.println(flowResult.details);
        }
        
        // Test F4: Resilience
        System.out.println("\n=== F4: Network Resilience ===");
        ResilienceAlgorithm.ResilienceResult resilience = 
            ResilienceAlgorithm.analyzeNetworkResilience(network);
        System.out.println(resilience.details);
        
        // Test F5: Charging Optimization
        System.out.println("\n=== F5: Charging Optimization ===");
        ChargingOptimization.OptimizationResult charging = 
            ChargingOptimization.optimizeChargingStations(network, 2);
        System.out.println(charging.details);
        
        // Test F6: Communication Network
        System.out.println("\n=== F6: Communication Network ===");
        CommunicationNetwork.MSTResult mst = 
            CommunicationNetwork.buildCommunicationNetwork(network);
        System.out.println(mst.details);
    }
}
