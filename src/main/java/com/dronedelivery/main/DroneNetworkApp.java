package com.dronedelivery.main;

import java.util.HashSet;
import java.util.Set;

import com.dronedelivery.algorithms.ChargingOptimization;
import com.dronedelivery.algorithms.CommunicationNetwork;
import com.dronedelivery.algorithms.MaxFlowAlgorithm;
import com.dronedelivery.algorithms.ResilienceAlgorithm;
import com.dronedelivery.backend.DroneNetwork;
import com.dronedelivery.backend.Edge;
import com.dronedelivery.backend.Node;
import com.dronedelivery.backend.PathResult;
import com.dronedelivery.io.JsonHandler;
import com.dronedelivery.visualization.GraphVisualizer;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DroneNetworkApp extends Application {
    
    private DroneNetwork network;
    private Canvas canvas;
    private GraphVisualizer visualizer;
    private TextArea outputArea;
    private ComboBox<String> hubCombo;
    private Label networkStatusLabel;
    private ComboBox<String> fromCombo;
    private ComboBox<String> toCombo;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Drone Network Planner - Nova Schilda");
        primaryStage.setWidth(1400);
        primaryStage.setHeight(900);
        
        // Create main layout
        BorderPane root = new BorderPane();
        
        // Create canvas for graph visualization
        canvas = new Canvas(900, 600);
        
        // Create output area (MUST be created before control panel)
        outputArea = new TextArea();
        outputArea.setPrefHeight(150);
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setStyle("-fx-font-family: monospace; -fx-font-size: 10;");
        
        // Create control panel
        VBox controlPanel = createControlPanel();
        
        // Layout
        VBox centerLayout = new VBox(10, canvas, new Label("Output:"), new ScrollPane(outputArea));
        centerLayout.setPadding(new Insets(10));
        
        root.setCenter(centerLayout);
        root.setRight(controlPanel);
        BorderPane.setMargin(controlPanel, new Insets(10));
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Load default network
        loadDefaultNetwork();
    }
    
    private VBox createControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(350);
        panel.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");
        panel.setFillWidth(true);
        
        // Title
        Label titleLabel = new Label("Drone Network Planner");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        
        // Network Status
        networkStatusLabel = new Label("Network: Not Loaded");
        networkStatusLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #0066cc;");
        
        // Separator
        Separator sep1 = new Separator();
        
        // File Operations
        VBox fileBox = createFileBox();
        
        // Separator
        Separator sep2 = new Separator();
        
        // Algorithm Selection
        VBox algorithmBox = createAlgorithmBox();
        
        panel.getChildren().addAll(
            titleLabel,
            networkStatusLabel,
            sep1,
            fileBox,
            sep2,
            algorithmBox
        );
        
        return panel;
    }
    
    private VBox createFileBox() {
        VBox box = new VBox(8);
        Label fileLabel = new Label("File Operations");
        fileLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        
        Button loadSample1 = new Button("Load Sample 1");
        loadSample1.setPrefWidth(Double.MAX_VALUE);
        loadSample1.setStyle("-fx-padding: 8; -fx-font-size: 11;");
        loadSample1.setOnAction(e -> loadNetwork("src/main/resources/networks/sample1.json"));
        
        Button loadSample2 = new Button("Load Sample 2");
        loadSample2.setPrefWidth(Double.MAX_VALUE);
        loadSample2.setStyle("-fx-padding: 8; -fx-font-size: 11;");
        loadSample2.setOnAction(e -> loadNetwork("src/main/resources/networks/sample2.json"));
        
        box.getChildren().addAll(fileLabel, loadSample1, loadSample2);
        return box;
    }
    
    private VBox createAlgorithmBox() {
        VBox box = new VBox(8);
        Label algoLabel = new Label("Algorithms");
        algoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        
        // F1: Reachability
        VBox reachabilityBox = createReachabilityBox();
        
        // F2: Shortest Path
        VBox pathBox = createPathBox();
        
        // F3: Capacity
        Button capacityBtn = new Button("F3: Calculate Capacity");
        capacityBtn.setPrefWidth(Double.MAX_VALUE);
        capacityBtn.setStyle("-fx-padding: 8; -fx-font-size: 11;");
        capacityBtn.setOnAction(e -> handleCapacityCheck());
        
        // F4: Resilience
        Button resilienceBtn = new Button("F4: Check Resilience");
        resilienceBtn.setPrefWidth(Double.MAX_VALUE);
        resilienceBtn.setStyle("-fx-padding: 8; -fx-font-size: 11;");
        resilienceBtn.setOnAction(e -> handleResilience());
        
        // F5: Charging Placement
        Button chargingBtn = new Button("F5: Optimize Charging");
        chargingBtn.setPrefWidth(Double.MAX_VALUE);
        chargingBtn.setStyle("-fx-padding: 8; -fx-font-size: 11;");
        chargingBtn.setOnAction(e -> handleChargingOptimization());
        
        // F6: Communication Network
        Button commBtn = new Button("F6: Communication Network");
        commBtn.setPrefWidth(Double.MAX_VALUE);
        commBtn.setStyle("-fx-padding: 8; -fx-font-size: 11;");
        commBtn.setOnAction(e -> handleCommunicationNetwork());
        
        box.getChildren().addAll(
            algoLabel,
            reachabilityBox,
            new Separator(),
            pathBox,
            new Separator(),
            capacityBtn,
            resilienceBtn,
            chargingBtn,
            commBtn
        );
        
        return box;
    }
    
    private VBox createReachabilityBox() {
        VBox box = new VBox(5);
        
        Label label = new Label("F1: Check Reachability");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");
        
        hubCombo = new ComboBox<>();
        hubCombo.setPromptText("Select Distribution Hub");
        hubCombo.setPrefWidth(Double.MAX_VALUE);
        hubCombo.setStyle("-fx-font-size: 10;");
        
        hubCombo.setOnShowing(e -> {
            hubCombo.getItems().clear();
            if (network != null) {
                network.getDistributors().forEach(n -> hubCombo.getItems().add(n.getId()));
            }
        });
        
        Button checkBtn = new Button("Check Reachability");
        checkBtn.setPrefWidth(Double.MAX_VALUE);
        checkBtn.setStyle("-fx-padding: 6; -fx-font-size: 10;");
        checkBtn.setOnAction(e -> handleReachability());
        
        box.getChildren().addAll(label, hubCombo, checkBtn);
        return box;
    }
    
    private VBox createPathBox() {
        VBox box = new VBox(5);
        
        Label label = new Label("F2: Find Optimal Route");
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");
        
        HBox fromBox = new HBox(5);
        Label fromLabel = new Label("From:");
        fromLabel.setPrefWidth(45);
        fromLabel.setStyle("-fx-font-size: 10;");
        fromCombo = new ComboBox<>();
        fromCombo.setPromptText("Source");
        fromCombo.setPrefWidth(Double.MAX_VALUE);
        fromCombo.setStyle("-fx-font-size: 10;");
        fromBox.getChildren().addAll(fromLabel, fromCombo);
        
        HBox toBox = new HBox(5);
        Label toLabel = new Label("To:");
        toLabel.setPrefWidth(45);
        toLabel.setStyle("-fx-font-size: 10;");
        toCombo = new ComboBox<>();
        toCombo.setPromptText("Destination");
        toCombo.setPrefWidth(Double.MAX_VALUE);
        toCombo.setStyle("-fx-font-size: 10;");
        toBox.getChildren().addAll(toLabel, toCombo);
        
        fromCombo.setOnShowing(e -> {
            fromCombo.getItems().clear();
            if (network != null) {
                network.getNodes().values().forEach(n -> fromCombo.getItems().add(n.getId()));
            }
        });
        
        toCombo.setOnShowing(e -> {
            toCombo.getItems().clear();
            if (network != null) {
                network.getNodes().values().forEach(n -> toCombo.getItems().add(n.getId()));
            }
        });
        
        Button findPathBtn = new Button("Find Path");
        findPathBtn.setPrefWidth(Double.MAX_VALUE);
        findPathBtn.setStyle("-fx-padding: 6; -fx-font-size: 10;");
        findPathBtn.setOnAction(e -> {
            if (fromCombo.getValue() != null && toCombo.getValue() != null) {
                handleFindPath(fromCombo.getValue(), toCombo.getValue());
            } else {
                appendOutput("✗ Please select both source and destination");
            }
        });
        
        box.getChildren().addAll(label, fromBox, toBox, findPathBtn);
        return box;
    }
    
    private void loadDefaultNetwork() {
        loadNetwork("src/main/resources/networks/sample1.json");
    }
    
    private void loadNetwork(String filePath) {
        JsonHandler handler = new JsonHandler();
        network = handler.loadNetwork(filePath);
        
        if (network != null) {
            visualizer = new GraphVisualizer(canvas, network);
            visualizer.draw();
            updateNetworkStatus();
            appendOutput("✓ Network loaded: " + filePath);
            appendOutput("  Nodes: " + network.getNodes().size() + 
                        " | Edges: " + network.getEdges().size());
        } else {
            appendOutput("✗ Failed to load network");
        }
    }
    
    private void updateNetworkStatus() {
        int nodeCount = network.getNodes().size();
        int edgeCount = network.getEdges().size();
        int restrictedCount = (int) network.getEdges().stream()
            .filter(Edge::isRestricted).count();
        
        networkStatusLabel.setText(String.format(
            "Nodes: %d | Edges: %d | Restricted: %d",
            nodeCount, edgeCount, restrictedCount
        ));
    }
    
    private void handleReachability() {
        if (network == null) {
            appendOutput("✗ Load a network first");
            return;
        }
        
        String hubId = hubCombo.getValue();
        if (hubId == null) {
            appendOutput("✗ Select a distribution hub");
            return;
        }
        
        try {
            appendOutput("\n════════════════════════════════════════");
            appendOutput("F1: Checking Reachability from " + hubId);
            appendOutput("════════════════════════════════════════");
            
            boolean allReachable = true;
            int reachableCount = 0;
            
            for (Node delivery : network.getDeliveryPoints()) {
                boolean reachable = network.isReachable(hubId, delivery.getId());
                String status = reachable ? "✓" : "✗";
                appendOutput(status + " " + delivery.getId() + ": " + (reachable ? "REACHABLE" : "NOT REACHABLE"));
                if (reachable) reachableCount++;
                else allReachable = false;
            }
            
            appendOutput("\nResult: " + reachableCount + "/" + network.getDeliveryPoints().size() + 
                        " delivery points reachable");
            if (allReachable) {
                appendOutput("✓ All delivery points are reachable from " + hubId);
            } else {
                appendOutput("✗ Some delivery points are NOT reachable");
            }
        } catch (Exception e) {
            appendOutput("✗ Error in reachability check: " + e.getMessage());
        }
    }
    
    private void handleFindPath(String from, String to) {
        if (network == null) {
            appendOutput("✗ Load a network first");
            return;
        }
        
        appendOutput("\n════════════════════════════════════════");
        appendOutput("F2: Finding Optimal Route");
        appendOutput("════════════════════════════════════════");
        appendOutput("From: " + from + " | To: " + to);
        
        PathResult result = network.findShortestPath(from, to);
        
        if (result != null) {
            appendOutput("\nPath Found:");
            String pathStr = String.join(" → ", result.getPath());
            appendOutput(pathStr);
            appendOutput("Total Energy Cost: " + result.getTotalEnergy() + " units");
            
            // Clear previous drawing and redraw network
            visualizer.draw();
            
            // Highlight path on canvas
            visualizer.drawPath(result.getPath(), Color.LIMEGREEN);
        } else {
            appendOutput("\n✗ No path found from " + from + " to " + to);
            appendOutput("  (Destination may be unreachable due to restricted edges)");
            visualizer.draw();
        }
    }
    
    private void handleCapacityCheck() {
        if (network == null) {
            appendOutput("✗ Load a network first");
            return;
        }
        
        try {
            appendOutput("\n════════════════════════════════════════");
            appendOutput("F3: Calculate Delivery Capacity");
            appendOutput("════════════════════════════════════════");
            
            // Get all distributors and delivery points
            Set<String> hubIds = new HashSet<>();
            for (Node hub : network.getDistributors()) {
                hubIds.add(hub.getId());
            }
            
            Set<String> deliveryIds = new HashSet<>();
            for (Node delivery : network.getDeliveryPoints()) {
                deliveryIds.add(delivery.getId());
            }
            
            if (hubIds.isEmpty() || deliveryIds.isEmpty()) {
                appendOutput("✗ Network must have distributors and delivery points");
                return;
            }
            
            // Calculate capacity for each hub
            for (String hubId : hubIds) {
                MaxFlowAlgorithm.FlowResult result = 
                    MaxFlowAlgorithm.calculateDeliveryCapacity(network, hubId, deliveryIds);
                
                appendOutput("\nHub: " + hubId);
                appendOutput(result.details);
                appendOutput("");
            }
        } catch (Exception e) {
            appendOutput("✗ Error in capacity check: " + e.getMessage());
        }
    }
    
    private void handleResilience() {
        if (network == null) {
            appendOutput("✗ Load a network first");
            return;
        }
        
        try {
            appendOutput("\n════════════════════════════════════════");
            appendOutput("F4: Network Resilience Analysis");
            appendOutput("════════════════════════════════════════\n");
            
            ResilienceAlgorithm.ResilienceResult result = 
                ResilienceAlgorithm.analyzeNetworkResilience(network);
            
            appendOutput(result.details);
        } catch (Exception e) {
            appendOutput("✗ Error in resilience analysis: " + e.getMessage());
        }
    }
    
    private void handleChargingOptimization() {
        if (network == null) {
            appendOutput("✗ Load a network first");
            return;
        }
        
        try {
            appendOutput("\n════════════════════════════════════════");
            appendOutput("F5: Charging Station Optimization");
            appendOutput("════════════════════════════════════════\n");
            
            int k = 2; // Number of new stations to add
            ChargingOptimization.OptimizationResult result = 
                ChargingOptimization.optimizeChargingStations(network, k);
            
            appendOutput(result.details);
        } catch (Exception e) {
            appendOutput("✗ Error in charging optimization: " + e.getMessage());
        }
    }
    
    private void handleCommunicationNetwork() {
        if (network == null) {
            appendOutput("✗ Load a network first");
            return;
        }
        
        try {
            appendOutput("\n════════════════════════════════════════");
            appendOutput("F6: Communication Network (MST)");
            appendOutput("════════════════════════════════════════\n");
            
            CommunicationNetwork.MSTResult result = 
                CommunicationNetwork.buildCommunicationNetwork(network);
            
            appendOutput(result.details);
        } catch (Exception e) {
            appendOutput("✗ Error in communication network: " + e.getMessage());
        }
    }
    
    private void appendOutput(String text) {
        outputArea.appendText(text + "\n");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
