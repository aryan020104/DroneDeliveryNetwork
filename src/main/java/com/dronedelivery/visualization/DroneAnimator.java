package com.dronedelivery.visualization;

import java.util.List;

import com.dronedelivery.backend.DroneNetwork;
import com.dronedelivery.backend.Node;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Duration;

public class DroneAnimator {
    private DroneNetwork network;
    private GraphicsContext gc;
    private List<String> path;
    private int droneEnergyCapacity;
    private static final double DRONE_RADIUS = 8;
    
    public DroneAnimator(GraphicsContext gc, DroneNetwork network) {
        this.gc = gc;
        this.network = network;
        this.droneEnergyCapacity = 500;
    }
    
    public Timeline createDeliveryAnimation(List<String> path, int totalEnergy) {
        this.path = path;
        
        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        
        // Calculate animation duration based on path distance
        double totalDistance = calculatePathDistance(path);
        double totalDuration = Math.max(1000, (totalDistance / 100) * 1000); // in milliseconds
        
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(0), e -> {
            // Animation start
        }));
        
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(totalDuration), e -> {
            System.out.println("✓ Delivery animation completed!");
        }));
        
        return timeline;
    }
    
    private double calculatePathDistance(List<String> path) {
        double distance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            Node from = network.getNode(path.get(i));
            Node to = network.getNode(path.get(i + 1));
            if (from != null && to != null) {
                distance += Math.hypot(to.getX() - from.getX(), to.getY() - from.getY());
            }
        }
        return distance;
    }
}