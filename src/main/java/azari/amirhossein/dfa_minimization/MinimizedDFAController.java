package azari.amirhossein.dfa_minimization;

import azari.amirhossein.dfa_minimization.models.State;
import azari.amirhossein.dfa_minimization.models.Transition;
import azari.amirhossein.dfa_minimization.utils.FXUtils;
import azari.amirhossein.dfa_minimization.utils.ParticleSystem;
import azari.amirhossein.dfa_minimization.utils.StateChangeListener;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class MinimizedDFAController implements StateChangeListener {

    private String startState;
    private Set<String> finalStates = new HashSet<>();
    HashMap<String, HashMap<String, String>> minDFAGraph = new HashMap<>();

    private Map<String, State> stateMap = new HashMap<>();
    private List<Transition> transitions = new ArrayList<>();

    private Scene mainScene;
    private ParticleSystem particleSystem;

    @FXML
    private Canvas canvas;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Button menuBtn;

    @FXML
    public void initialize() {
        if (canvas == null || anchorPane == null) {
            throw new IllegalStateException("FXML components not properly initialized");
        }

        initializeParticleSystem();
        initializeMenuButton();

        // Initialize view if data is available
        if (minDFAGraph != null && !minDFAGraph.isEmpty()) {
            refreshView();
        }
    }

    private void initializeParticleSystem() {
        particleSystem = new ParticleSystem(800, 600, 80);
        particleSystem.startAnimation(canvas);
    }

    private void initializeMenuButton() {
        if (menuBtn != null) {
            menuBtn.setOnMouseClicked(mouseEvent -> {
                cleanup();
                FXUtils.loadSceneFromFXML("/azari/amirhossein/dfa_minimization/menu.fxml", mouseEvent);
            });
        }
    }

    private void cleanup() {
        if (particleSystem != null) {
            particleSystem.stop();
        }

        cleanupTransitions();

        if (stateMap != null) {
            stateMap.values().forEach(state -> {
                state.removeStateChangeListener(this);
                if (anchorPane != null) {
                    anchorPane.getChildren().removeAll(
                        state.getCircle(),
                        state.getLabelNode(),
                        state.getStartArrow()
                    );
                }
            });
            stateMap.clear();
        }
    }

    @FXML
    public void handleBack() {
        try {
            cleanup();
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            
            if (mainScene != null) {
                stage.setScene(mainScene);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/azari/amirhossein/dfa_minimization/dfa.fxml"));
                Parent root = loader.load();
                mainScene = new Scene(root);
                stage.setScene(mainScene);
            }
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(700), mainScene.getRoot());
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (Exception e) {
            System.err.println("Error navigating back: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setDFAData(String startState, Set<String> finalStates,
                           HashMap<String, HashMap<String, String>> minDFAGraph) {
        if (startState == null || finalStates == null || minDFAGraph == null) {
            throw new IllegalArgumentException("DFA data cannot be null");
        }

        this.startState = startState;
        this.finalStates = new HashSet<>(finalStates);
        this.minDFAGraph = new HashMap<>(minDFAGraph);
        
        refreshView();
    }

    @FXML
    public void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        if (event.getButton() == MouseButton.PRIMARY) {
            handleTransitionClick(x, y);
        }

    }

    private static final double RADIUS_FACTOR = 0.6;
    private static final double MIN_DISTANCE = 50.0;

    private void createStates() {
        if (minDFAGraph == null) {
            throw new IllegalStateException("DFA graph is not initialized");
        }

        stateMap.clear();

        // Get all states from the graph
        Set<String> allStates = new HashSet<>(minDFAGraph.keySet());
        minDFAGraph.values().forEach(stateTransitions ->
                allStates.addAll(stateTransitions.values()));

        if (allStates.isEmpty()) {
            return;
        }

        // Calculate layout parameters
        double centerX = anchorPane.getPrefWidth() / 2;
        double centerY = anchorPane.getPrefHeight() / 2;
        double radius = Math.min(centerX, centerY) * RADIUS_FACTOR;
        
        // Adjust radius if states would be too close
        int totalStates = allStates.size();
        double circumference = 2 * Math.PI * radius;
        double spacing = circumference / totalStates;
        if (spacing < MIN_DISTANCE) {
            radius = (MIN_DISTANCE * totalStates) / (2 * Math.PI);
        }

        int stateId = 0;
        for (String stateLabel : allStates) {
            try {
                double angle = (2 * Math.PI * stateId) / totalStates;
                double x = centerX + radius * Math.cos(angle);
                double y = centerY + radius * Math.sin(angle);

                boolean isFinal = finalStates.contains(stateLabel);
                boolean isStart = stateLabel.equals(startState);
                State state = new State(stateId, stateLabel, isFinal, isStart, x, y);
                state.addStateChangeListener(this);

                stateMap.put(stateLabel, state);
                stateId++;
            } catch (Exception e) {
                System.err.println("Error creating state " + stateLabel + ": " + e.getMessage());
            }
        }
    }

    private void cleanupTransitions() {
        transitions.forEach(this::removeTransitionFromPane);
        transitions.clear();
    }

    private void removeTransitionFromPane(Transition transition) {
        if (anchorPane != null) {
            anchorPane.getChildren().removeAll(
                transition.getLine(),
                transition.getArrow(),
                transition.getText()
            );
        }
    }

    private void createTransitions() {
        if (minDFAGraph == null || stateMap.isEmpty()) {
            return;
        }

        cleanupTransitions();

        // Map to store transitions between same states with different symbols
        Map<String, List<String>> combinedTransitions = new HashMap<>();

        // Collect all transitions between same states
        minDFAGraph.forEach((fromState, stateTransitions) -> {
            if (!stateMap.containsKey(fromState)) {
                System.err.println("Warning: Source state " + fromState + " not found in state map");
                return;
            }

            stateTransitions.forEach((symbol, toState) -> {
                if (!stateMap.containsKey(toState)) {
                    System.err.println("Warning: Target state " + toState + " not found in state map");
                    return;
                }

                if (symbol == null || symbol.trim().isEmpty()) {
                    System.err.println("Warning: Empty transition symbol between " + fromState + " and " + toState);
                    return;
                }

                String stateKey = fromState + "->" + toState;
                combinedTransitions.computeIfAbsent(stateKey, k -> new ArrayList<>())
                        .add(symbol);
            });
        });

        // Create transitions with combined symbols
        combinedTransitions.forEach((stateKey, symbols) -> {
            try {
                String[] states = stateKey.split("->");
                String fromState = states[0];
                String toState = states[1];

                State source = stateMap.get(fromState);
                State target = stateMap.get(toState);

                Collections.sort(symbols);
                String combinedSymbol = String.join(",", symbols);

                boolean shouldCurve = combinedTransitions.containsKey(toState + "->" + fromState);
                Transition transition = new Transition(source, target, combinedSymbol, shouldCurve);
                transitions.add(transition);
            } catch (Exception e) {
                System.err.println("Error creating transition " + stateKey + ": " + e.getMessage());
            }
        });
    }

    private void drawDFA() {
        if (anchorPane == null) return;


        // Draw states
        stateMap.values().forEach(state -> {
            state.draw(anchorPane);
            state.updateAppearance(anchorPane);
        });

        // Draw transitions
        transitions.forEach(transition -> transition.draw(anchorPane));
    }

    public void refreshView() {
        createStates();
        createTransitions();
        drawDFA();
    }

    public void setStart(String start) {
        this.startState = start;
    }

    public void setFinales(Set<String> finales) {
        this.finalStates = finales;
    }

    public void setMinimizedGraph(HashMap<String, HashMap<String, String>> minimizedGraph) {
        this.minDFAGraph = minimizedGraph;
    }

    @Override
    public void onStateChanged(State state) {
        try {
            updateTransitions();
        } catch (Exception e) {
            System.err.println("Error updating transitions: " + e.getMessage());
        }
    }

    private void handleTransitionClick(double x, double y) {
        if (x < 0 || y < 0 || x > anchorPane.getWidth() || y > anchorPane.getHeight()) {
            return;
        }

        for (Transition transition : transitions) {
            if (transition.isClicked(x, y)) {
                try {
                    transition.rotateSelfLoop();
                    removeTransitionFromPane(transition);
                    transition.draw(anchorPane);
                } catch (Exception e) {
                    System.err.println("Error handling transition click: " + e.getMessage());
                }
                break;
            }
        }
    }

    private void updateTransitions() {
        if (anchorPane == null) return;

        transitions.forEach(transition -> {
            try {
                transition.updatePosition();
                transition.redraw(anchorPane);
            } catch (Exception e) {
                System.err.println("Error updating transition: " + e.getMessage());
            }
        });
    }

    public void setMainScene(Scene mainScene) {
        this.mainScene = mainScene;
    }
}