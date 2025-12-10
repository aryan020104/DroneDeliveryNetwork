package com.dronedelivery.test;

import com.dronedelivery.backend.Node;
import com.dronedelivery.backend.Edge;

public class Main {
    public static void main(String[] args) {
        Node distributor = new Node("HUB1", Node.NodeType.DISTRIBUTOR, 55, 65);
        Node charging = new Node("HUB2", Node.NodeType.CHARGING, 45, 75);
        Node delivery = new Node("HUB3", Node.NodeType.DELIVERY, 35, 85);


        Edge edge = new Edge(distributor, delivery, 100, 5, true, false);
        System.out.println(distributor);
        System.out.println(delivery);
        System.out.println(edge);
    }

}