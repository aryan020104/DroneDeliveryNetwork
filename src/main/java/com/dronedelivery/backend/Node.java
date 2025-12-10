package com.dronedelivery.backend;

public class Node {
    public enum NodeType {
        DISTRIBUTOR,
        CHARGING,
        DELIVERY
    }

    private String id;
    private NodeType type;
    private double x;
    private double y;

    @java.lang.Override
    public java.lang.String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    public Node(String id, NodeType type, double x, double y) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return this.id;
    }

    public NodeType getType() {
        return this.type;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

}