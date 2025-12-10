package com.dronedelivery.visualization;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dronedelivery.backend.DroneNetwork;
import com.dronedelivery.backend.Edge;
import com.dronedelivery.backend.Node;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class GraphVisualizer {
    private Canvas canvas;
    private DroneNetwork network;
    private static final double NODE_RADIUS = 15;
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double offsetX = 10;
    private double offsetY = 10;
    
    public GraphVisualizer(Canvas canvas, DroneNetwork network) {
        this.canvas = canvas;
        this.network = network;
        calculateScale();
    }
    
    private void calculateScale() {
        if (network == null || network.getNodes().isEmpty()) {
            return;
        }
        
        // Find min and max coordinates
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        
        for (Node node : network.getNodes().values()) {
            minX = Math.min(minX, node.getX());
            maxX = Math.max(maxX, node.getX());
            minY = Math.min(minY, node.getY());
            maxY = Math.max(maxY, node.getY());
        }
        
        double canvasWidth = canvas.getWidth() - 2 * offsetX - 2 * NODE_RADIUS;
        double canvasHeight = canvas.getHeight() - 2 * offsetY - 2 * NODE_RADIUS;
        
        double dataWidth = maxX - minX;
        double dataHeight = maxY - minY;
        
        // Prevent division by zero
        if (dataWidth == 0) dataWidth = 1;
        if (dataHeight == 0) dataHeight = 1;
        
        scaleX = canvasWidth / dataWidth;
        scaleY = canvasHeight / dataHeight;
        
        // Use uniform scaling to maintain aspect ratio
        double scale = Math.min(scaleX, scaleY);
        scaleX = scale;
        scaleY = scale;
    }
    
    private double getScreenX(double dataX) {
        // Find min X for centering
        double minX = network.getNodes().values().stream()
            .mapToDouble(Node::getX).min().orElse(0);
        return offsetX + (dataX - minX) * scaleX;
    }
    
    private double getScreenY(double dataY) {
        // Find min Y for centering
        double minY = network.getNodes().values().stream()
            .mapToDouble(Node::getY).min().orElse(0);
        return offsetY + (dataY - minY) * scaleY;
    }
    
    public void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Clear canvas with light gray background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Draw grid
        drawGrid(gc);
        
        // Draw edges first (so they appear behind nodes)
        drawEdges(gc);
        
        // Draw nodes on top
        drawNodes(gc);
    }
    
    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        
        double gridSpacing = 50;
        for (double x = 0; x < canvas.getWidth(); x += gridSpacing) {
            gc.strokeLine(x, 0, x, canvas.getHeight());
        }
        for (double y = 0; y < canvas.getHeight(); y += gridSpacing) {
            gc.strokeLine(0, y, canvas.getWidth(), y);
        }
    }
    
    private void drawEdges(GraphicsContext gc) {
        Set<String> drawnEdges = new HashSet<>();
        
        for (Edge edge : network.getEdges()) {
            // Create unique key for bidirectional edges (avoid drawing twice)
            String edgeKey = edge.getFrom().getId() + "-" + edge.getTo().getId();
            String reverseKey = edge.getTo().getId() + "-" + edge.getFrom().getId();
            
            if (drawnEdges.contains(edgeKey) || drawnEdges.contains(reverseKey)) {
                continue;
            }
            drawnEdges.add(edgeKey);
            
            Node from = edge.getFrom();
            Node to = edge.getTo();
            
            double x1 = getScreenX(from.getX());
            double y1 = getScreenY(from.getY());
            double x2 = getScreenX(to.getX());
            double y2 = getScreenY(to.getY());
            
            // Set color and style based on restriction
            if (edge.isRestricted()) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(2.5);
                drawDashedLine(gc, x1, y1, x2, y2, 5, 5);
            } else {
                gc.setStroke(Color.DARKGRAY);
                gc.setLineWidth(1.5);
                gc.strokeLine(x1, y1, x2, y2);
            }
            
            // Draw arrowhead for directed (non-bidirectional) edges
            if (!edge.isBidirectional()) {
                drawArrowHead(gc, x1, y1, x2, y2, edge.isRestricted());
            }
            
            // Draw edge label (energy and capacity)
            double midX = (x1 + x2) / 2;
            double midY = (y1 + y2) / 2;
            String label = "E:" + edge.getEnergy() + " C:" + edge.getCapacity();
            
            gc.setFill(Color.BLACK);
            gc.setFont(new Font(9));
            gc.fillText(label, midX + 5, midY - 5);
        }
    }
    
    private void drawDashedLine(GraphicsContext gc, double x1, double y1, double x2, double y2,
                                double dashLength, double gapLength) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double unitDx = dx / distance;
        double unitDy = dy / distance;
        
        double currentDist = 0;
        boolean drawDash = true;
        
        while (currentDist < distance) {
            double segmentLength = drawDash ? dashLength : gapLength;
            double nextDist = Math.min(currentDist + segmentLength, distance);
            
            if (drawDash) {
                double sx1 = x1 + unitDx * currentDist;
                double sy1 = y1 + unitDy * currentDist;
                double sx2 = x1 + unitDx * nextDist;
                double sy2 = y1 + unitDy * nextDist;
                gc.strokeLine(sx1, sy1, sx2, sy2);
            }
            
            currentDist = nextDist;
            drawDash = !drawDash;
        }
    }
    
    private void drawArrowHead(GraphicsContext gc, double x1, double y1, double x2, double y2, 
                               boolean restricted) {
        double size = 15;
        double angle = Math.atan2(y2 - y1, x2 - x1);
        
        double x = x2 - size * Math.cos(angle);
        double y = y2 - size * Math.sin(angle);
        
        double x3 = x - size * Math.cos(angle - Math.PI / 6);
        double y3 = y - size * Math.sin(angle - Math.PI / 6);
        double x4 = x - size * Math.cos(angle + Math.PI / 6);
        double y4 = y - size * Math.sin(angle + Math.PI / 6);
        
        Color arrowColor = restricted ? Color.RED : Color.DARKGRAY;
        gc.setFill(arrowColor);
        gc.fillPolygon(
            new double[]{x2, x3, x4},
            new double[]{y2, y3, y4},
            3
        );
    }
    
    private void drawNodes(GraphicsContext gc) {
        for (Node node : network.getNodes().values()) {
            double x = getScreenX(node.getX());
            double y = getScreenY(node.getY());
            
            // Draw node circle with color based on type
            Color color = getNodeColor(node.getType());
            gc.setFill(color);
            gc.fillOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
            
            // Draw border
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeOval(x - NODE_RADIUS, y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
            
            // Draw label
            gc.setFill(Color.WHITE);
            gc.setFont(new Font(10));
            gc.fillText(node.getId(), x, y + 4);
        }
    }
    
    private Color getNodeColor(Node.NodeType type) {
        switch (type) {
            case DISTRIBUTOR:
                return Color.DODGERBLUE;
            case CHARGING:
                return Color.LIMEGREEN;
            case DELIVERY:
                return Color.CRIMSON;
            default:
                return Color.GRAY;
        }
    }
    
    public void drawPath(List<String> path, Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        gc.setStroke(color);
        gc.setLineWidth(4);
        
        for (int i = 0; i < path.size() - 1; i++) {
            Node from = network.getNode(path.get(i));
            Node to = network.getNode(path.get(i + 1));
            
            if (from != null && to != null) {
                double x1 = getScreenX(from.getX());
                double y1 = getScreenY(from.getY());
                double x2 = getScreenX(to.getX());
                double y2 = getScreenY(to.getY());
                gc.strokeLine(x1, y1, x2, y2);
            }
        }
    }
}
