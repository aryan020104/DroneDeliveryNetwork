package com.dronedelivery.test;

import com.dronedelivery.backend.DroneNetwork;
import com.dronedelivery.backend.Node;
import com.dronedelivery.io.JsonHandler;
import com.dronedelivery.visualization.GraphVisualizer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class VisualizationTest extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Drone Network Visualization Test");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);
        
        // Create canvas
        Canvas canvas = new Canvas(950, 650);
        
        // Load network
        JsonHandler handler = new JsonHandler();
        DroneNetwork network = handler.loadNetwork("src/main/resources/networks/sample1.json");
        
        if (network == null) {
            System.err.println("Failed to load network!");
            return;
        }
        
        System.out.println("\n=== Network Loaded ===");
        System.out.println("Nodes: " + network.getNodes().size());
        System.out.println("Edges: " + network.getEdges().size());
        System.out.println("\nNodes Details:");
        for (Node node : network.getNodes().values()) {
            System.out.println("  " + node);
        }
        
        // Create visualizer and draw
        GraphVisualizer visualizer = new GraphVisualizer(canvas, network);
        System.out.println("\nDrawing network...");
        visualizer.draw();
        System.out.println("✓ Network drawn on canvas");
        
        // Layout
        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
