package com.dronedelivery.backend;

public class Edge {
    private Node from;
    private Node to;
    private int energy;
    private int capacity;
    private boolean bidirectional;
    private boolean restricted;


    @java.lang.Override
    public java.lang.String toString() {
        return "Edge{" +
                "from=" + from +
                ", to=" + to +
                ", energy=" + energy +
                ", capacity=" + capacity +
                ", bidirectional=" + bidirectional +
                ", restricted=" + restricted +
                '}';
    }

    public Edge(Node from, Node to, int energy, int capacity, boolean bidirectional, boolean restricted){
        this.from = from;
        this.to = to;
        this.energy = energy;
        this.capacity = capacity;
        this.bidirectional = bidirectional;
        this.restricted = restricted;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getEnergy() {
        return energy;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public boolean isRestricted() {
        return restricted;
    }
    public void setFrom(Node from) {
        this.from = from;
    }

    public void setTo(Node to) {
        this.to = to;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }
}

