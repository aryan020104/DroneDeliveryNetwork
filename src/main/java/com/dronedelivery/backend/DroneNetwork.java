package com.dronedelivery.backend;

import java.util.*;

public class DroneNetwork {
    private Map<String, Node> nodes;
    private List<Edge> edges;
    private Map<String, List<Edge>> adjacencyList;
    
    public DroneNetwork() {
        this.nodes = new HashMap<>();
        this.edges = new ArrayList<>();
        this.adjacencyList = new HashMap<>();
    }
    
    // ===== Add/Get Nodes =====
    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        adjacencyList.put(node.getId(), new ArrayList<>());
    }
    
    public Node getNode(String id) {
        return nodes.get(id);
    }
    
    public Map<String, Node> getNodes() {
        return nodes;
    }
    
    // ===== Add/Get Edges =====
    public void addEdge(Edge edge) {
        edges.add(edge);
        adjacencyList.get(edge.getFrom().getId()).add(edge);
        
        // If bidirectional, add reverse edge
        if (edge.isBidirectional()) {
            Edge reverseEdge = new Edge(
                edge.getTo(),
                edge.getFrom(),
                edge.getEnergy(),
                edge.getCapacity(),
                false,
                edge.isRestricted()
            );
            edges.add(reverseEdge);
            adjacencyList.get(edge.getTo().getId()).add(reverseEdge);
        }
    }
    
    public List<Edge> getEdges() {
        return edges;
    }
    
    public List<Edge> getEdgesFrom(String nodeId) {
        return adjacencyList.getOrDefault(nodeId, new ArrayList<>());
    }
    
    // ===== F1: Check Reachability (BFS) =====
    public boolean isReachable(String startId, String targetId) {
        if (!nodes.containsKey(startId) || !nodes.containsKey(targetId)) {
            return false;
        }
        
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(startId);
        visited.add(startId);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            if (current.equals(targetId)) {
                return true;
            }
            
            // Only traverse non-restricted edges
            for (Edge edge : adjacencyList.get(current)) {
                if (!edge.isRestricted() && !visited.contains(edge.getTo().getId())) {
                    visited.add(edge.getTo().getId());
                    queue.add(edge.getTo().getId());
                }
            }
        }
        
        return false;
    }
    
    public boolean checkAllDeliveryPointsReachable(String hubId) {
        for (Node node : nodes.values()) {
            if (node.getType() == Node.NodeType.DELIVERY) {
                if (!isReachable(hubId, node.getId())) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // ===== F2: Dijkstra's Algorithm =====
    public PathResult findShortestPath(String startId, String endId) {
        if (!nodes.containsKey(startId) || !nodes.containsKey(endId)) {
            return null;
        }
        
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(
            (a, b) -> Integer.compare(
                distances.getOrDefault(a, Integer.MAX_VALUE),
                distances.getOrDefault(b, Integer.MAX_VALUE)
            )
        );
        
        // Initialize
        for (String nodeId : nodes.keySet()) {
            distances.put(nodeId, Integer.MAX_VALUE);
        }
        distances.put(startId, 0);
        pq.add(startId);
        
        while (!pq.isEmpty()) {
            String current = pq.poll();
            int currentDist = distances.get(current);
            
            if (currentDist == Integer.MAX_VALUE) continue;
            if (current.equals(endId)) break;
            
            // Only traverse non-restricted edges
            for (Edge edge : adjacencyList.get(current)) {
                if (edge.isRestricted()) continue; // Skip restricted edges
                
                String neighbor = edge.getTo().getId();
                int newDist = currentDist + edge.getEnergy();
                
                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previousNodes.put(neighbor, current);
                    pq.remove(neighbor);
                    pq.add(neighbor);
                }
            }
        }
        
        // Reconstruct path
        List<String> path = new ArrayList<>();
        String current = endId;
        
        if (distances.get(endId) == Integer.MAX_VALUE) {
            return null; // No path exists
        }
        
        while (current != null) {
            path.add(0, current);
            current = previousNodes.get(current);
        }
        
        return new PathResult(path, distances.get(endId));
    }
    
    // ===== F1: Get All Delivery Points =====
    public List<Node> getDeliveryPoints() {
        List<Node> deliveryPoints = new ArrayList<>();
        for (Node node : nodes.values()) {
            if (node.getType() == Node.NodeType.DELIVERY) {
                deliveryPoints.add(node);
            }
        }
        return deliveryPoints;
    }
    
    // ===== Get Distributors =====
    public List<Node> getDistributors() {
        List<Node> distributors = new ArrayList<>();
        for (Node node : nodes.values()) {
            if (node.getType() == Node.NodeType.DISTRIBUTOR) {
                distributors.add(node);
            }
        }
        return distributors;
    }
    
    // ===== Get Charging Points =====
    public List<Node> getChargingPoints() {
        List<Node> charging = new ArrayList<>();
        for (Node node : nodes.values()) {
            if (node.getType() == Node.NodeType.CHARGING) {
                charging.add(node);
            }
        }
        return charging;
    }
}
