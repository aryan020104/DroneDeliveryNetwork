package com.dronedelivery.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dronedelivery.backend.DroneNetwork;
import com.dronedelivery.backend.Edge;

public class CommunicationNetwork {
    
    public static class MSTResult {
        public List<String> mstEdges;
        public int totalCost;
        public String details;
        
        public MSTResult(List<String> edges, int cost, String details) {
            this.mstEdges = edges;
            this.totalCost = cost;
            this.details = details;
        }
    }
    
    /**
     * F6: Build minimum spanning tree for communication network
     * Connect all stations with minimum setup cost
     * Uses Kruskal's algorithm with Union-Find
     */
    public static MSTResult buildCommunicationNetwork(DroneNetwork network) {
        // Create list of edges with their costs (using energy as cost)
        List<EdgeWithCost> edgeList = new ArrayList<>();
        
        for (Edge edge : network.getEdges()) {
            if (!edge.isRestricted()) {
                edgeList.add(new EdgeWithCost(
                    edge.getFrom().getId(),
                    edge.getTo().getId(),
                    edge.getEnergy()
                ));
            }
        }
        
        // Sort edges by cost
        Collections.sort(edgeList, (a, b) -> Integer.compare(a.cost, b.cost));
        
        // Kruskal's algorithm with Union-Find
        UnionFind uf = new UnionFind(network.getNodes().keySet());
        List<String> mstEdges = new ArrayList<>();
        int totalCost = 0;
        int edgesAdded = 0;
        int requiredEdges = network.getNodes().size() - 1;
        
        for (EdgeWithCost edge : edgeList) {
            if (edgesAdded >= requiredEdges) break;
            
            if (uf.union(edge.from, edge.to)) {
                mstEdges.add(edge.from + " <-> " + edge.to + " (cost: " + edge.cost + ")");
                totalCost += edge.cost;
                edgesAdded++;
            }
        }
        
        StringBuilder details = new StringBuilder();
        details.append("COMMUNICATION NETWORK (MINIMUM SPANNING TREE)\n");
        details.append("=============================================\n\n");
        details.append("Using Kruskal's Algorithm:\n\n");
        details.append("Network Links:\n");
        for (String link : mstEdges) {
            details.append("  ✓ ").append(link).append("\n");
        }
        details.append("\nTotal Links: ").append(mstEdges.size()).append("\n");
        details.append("Total Setup Cost: ").append(totalCost).append(" units\n");
        details.append("Nodes Connected: ").append(edgesAdded + 1).append("/").append(network.getNodes().size()).append("\n");
        
        if (edgesAdded + 1 == network.getNodes().size()) {
            details.append("\n✓ All stations connected!\n");
        } else {
            details.append("\n✗ Warning: Not all stations connected. Network may be disconnected.\n");
        }
        
        return new MSTResult(mstEdges, totalCost, details.toString());
    }
    
    static class EdgeWithCost {
        String from;
        String to;
        int cost;
        
        EdgeWithCost(String from, String to, int cost) {
            this.from = from;
            this.to = to;
            this.cost = cost;
        }
    }
}
