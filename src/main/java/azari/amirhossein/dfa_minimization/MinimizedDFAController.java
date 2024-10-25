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
    public void setDFAData(String startState, Set<String> finalStates,
                           HashMap<String, HashMap<String, String>> minDFAGraph) {
        this.startState = startState;
        this.finalStates = finalStates;
        this.minDFAGraph = minDFAGraph;
        refreshView();
    }
    @FXML
    private Canvas canvas;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Button menuBtn;

    @FXML
    public void handleBack() {
        try {
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            if (mainScene != null) {
                stage.setScene(mainScene);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/azari/amirhossein/dfa_minimization/dfa.fxml"));
                Parent root = loader.load();
                mainScene = new Scene(root);
                stage.setScene(mainScene);
            }
            stage.show();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(700), mainScene.getRoot());
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        if (event.getButton() == MouseButton.PRIMARY) {
            handleTransitionClick(x, y);
        }

    }

    @FXML
    public void initialize() {
        ParticleSystem particleSystem = new ParticleSystem(800, 600, 80);
        particleSystem.startAnimation(canvas);

        // This will be called when FXML is loaded
        if (minDFAGraph != null && !minDFAGraph.isEmpty()) {
            refreshView();
        }
        menuBtn.setOnMouseClicked(mouseEvent -> {
            FXUtils.loadSceneFromFXML("/azari/amirhossein/dfa_minimization/menu.fxml", mouseEvent);
        });
    }

    private void createStates() {
        stateMap.clear(); // Clear existing states

        // Get all states from the graph
        Set<String> allStates = new HashSet<>(minDFAGraph.keySet());
        minDFAGraph.values().forEach(stateTransitions ->
                allStates.addAll(stateTransitions.values()));

        // Calculate positions in a circular layout
        int totalStates = allStates.size();
        int stateId = 0;
        double centerX = anchorPane.getPrefWidth() / 2;
        double centerY = anchorPane.getPrefHeight() / 2;
        double radius = Math.min(centerX, centerY) * 0.6;

        for (String stateLabel : allStates) {
            // Calculate position on circle
            double angle = (2 * Math.PI * stateId) / totalStates;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);

            // Create state
            boolean isFinal = finalStates.contains(stateLabel);
            boolean isStart = stateLabel.equals(startState);
            State state = new State(stateId, stateLabel, isFinal, isStart, x, y);
            state.addStateChangeListener(this);

            stateMap.put(stateLabel, state);
            stateId++;
        }
    }

    private void createTransitions() {
        transitions.clear();

        // Map to store transitions between same states with different symbols
        Map<String, List<String>> combinedTransitions = new HashMap<>();

        // First pass: Collect all transitions between same states
        minDFAGraph.forEach((fromState, stateTransitions) -> {
            stateTransitions.forEach((symbol, toState) -> {
                // Create a key for the state pair
                String stateKey = fromState + "->" + toState;

                // Add symbol to the list of symbols for this state pair
                combinedTransitions.computeIfAbsent(stateKey, k -> new ArrayList<>())
                        .add(symbol);
            });
        });

        // Second pass: Create transitions with combined symbols
        combinedTransitions.forEach((stateKey, symbols) -> {
            String[] states = stateKey.split("->");
            String fromState = states[0];
            String toState = states[1];

            State source = stateMap.get(fromState);
            State target = stateMap.get(toState);

            // Sort symbols for consistent display
            Collections.sort(symbols);

            // Join symbols with comma
            String combinedSymbol = String.join(",", symbols);

            // Check if there's a reverse transition
            boolean shouldCurve = combinedTransitions.containsKey(toState + "->" + fromState);

            // Create single transition with combined symbols
            Transition transition = new Transition(source, target, combinedSymbol, shouldCurve);
            transitions.add(transition);
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


    private void removeTransitionFromPane(Transition transition) {
        anchorPane.getChildren().remove(transition.getLine());
        anchorPane.getChildren().remove(transition.getArrow());
        anchorPane.getChildren().remove(transition.getText());
    }

    @Override
    public void onStateChanged(State state) {
        updateTransitions();
    }

    private void handleTransitionClick(double x, double y) {
        for (Transition transition : transitions) {
            if (transition.isClicked(x, y)) {
                transition.rotateSelfLoop();
                removeTransitionFromPane(transition);
                transition.draw(anchorPane);
                break;
            }
        }
    }


    private void updateTransitions() {
        for (Transition transition : transitions) {
            transition.updatePosition();
            transition.redraw(anchorPane);
        }
    }

    public void setMainScene(Scene mainScene) {
        this.mainScene = mainScene;
    }
}