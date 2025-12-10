package com.dronedelivery.io;

import com.dronedelivery.backend.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

public class JsonHandler {
    private JSONParser parser = new JSONParser();
    
    public DroneNetwork loadNetwork(String filePath) {
        try {
            DroneNetwork network = new DroneNetwork();
            
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filePath));
            
            // Parse nodes
            JSONArray nodesArray = (JSONArray) jsonObject.get("nodes");
            for (Object nodeObj : nodesArray) {
                JSONObject nodeJson = (JSONObject) nodeObj;
                String id = (String) nodeJson.get("id");
                String typeStr = (String) nodeJson.get("type");
                double x = ((Number) nodeJson.get("x")).doubleValue();
                double y = ((Number) nodeJson.get("y")).doubleValue();
                
                Node.NodeType type = Node.NodeType.valueOf(typeStr);
                Node node = new Node(id, type, x, y);
                network.addNode(node);
            }
            
            // Parse edges
            JSONArray edgesArray = (JSONArray) jsonObject.get("edges");
            for (Object edgeObj : edgesArray) {
                JSONObject edgeJson = (JSONObject) edgeObj;
                String fromId = (String) edgeJson.get("from");
                String toId = (String) edgeJson.get("to");
                int energy = ((Number) edgeJson.get("energy")).intValue();
                int capacity = ((Number) edgeJson.get("capacity")).intValue();
                boolean bidirectional = (Boolean) edgeJson.get("bidirectional");
                boolean restricted = (Boolean) edgeJson.getOrDefault("restricted", false);
                
                Node from = network.getNode(fromId);
                Node to = network.getNode(toId);
                
                if (from != null && to != null) {
                    Edge edge = new Edge(from, to, energy, capacity, bidirectional, restricted);
                    network.addEdge(edge);
                }
            }
            
            System.out.println("✓ Network loaded successfully from " + filePath);
            return network;
            
        } catch (Exception e) {
            System.err.println("✗ Error loading JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
