package com.dronedelivery.algorithms;

import java.util.HashMap;
import java.util.Map;

public class UnionFind {
    private Map<String, String> parent;
    private Map<String, Integer> rank;
    
    public UnionFind(java.util.Collection<String> elements) {
        this.parent = new HashMap<>();
        this.rank = new HashMap<>();
        for (String elem : elements) {
            parent.put(elem, elem);
            rank.put(elem, 0);
        }
    }
    
    public String find(String x) {
        if (!parent.get(x).equals(x)) {
            parent.put(x, find(parent.get(x))); // Path compression
        }
        return parent.get(x);
    }
    
    public boolean union(String x, String y) {
        String rootX = find(x);
        String rootY = find(y);
        
        if (rootX.equals(rootY)) return false;
        
        // Union by rank
        if (rank.get(rootX) < rank.get(rootY)) {
            parent.put(rootX, rootY);
        } else if (rank.get(rootX) > rank.get(rootY)) {
            parent.put(rootY, rootX);
        } else {
            parent.put(rootY, rootX);
            rank.put(rootX, rank.get(rootX) + 1);
        }
        return true;
    }
    
    public boolean isConnected(String x, String y) {
        return find(x).equals(find(y));
    }
}
