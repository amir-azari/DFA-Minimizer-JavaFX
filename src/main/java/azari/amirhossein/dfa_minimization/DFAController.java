package azari.amirhossein.dfa_minimization;

import azari.amirhossein.dfa_minimization.models.MinimizationProcess;
import azari.amirhossein.dfa_minimization.utils.ParticleSystem;
import azari.amirhossein.dfa_minimization.models.State;
import azari.amirhossein.dfa_minimization.utils.StateChangeListener;
import azari.amirhossein.dfa_minimization.models.Transition;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.*;

import static azari.amirhossein.dfa_minimization.utils.FXUtils.showAlert;

import javafx.scene.layout.VBox;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DFAController implements StateChangeListener {
    @FXML
    private Pane drawingPane;
    @FXML
    private Canvas canvas;
    @FXML
    private Button confirmButton;

    @FXML
    private void handleUndo() {
        if (!undoStack.isEmpty()) {
            Transition lastTransition = undoStack.pop();
            transitionsList.remove(lastTransition);
            removeTransitionOfGraph(lastTransition.getFromState().getLabel(), lastTransition.getSymbol() , lastTransition.getToState().getLabel());
            removeTransitionFromPane(lastTransition);
            redoStack.push(lastTransition);

        }
        System.out.println(graph);
    }
    @FXML
    private void handleRedo() {
        if (!redoStack.isEmpty()) {
            Transition lastUndone = redoStack.pop();
            transitionsList.add(lastUndone);
            lastUndone.draw(drawingPane);

            addTransitionToGraph(
                    lastUndone.getFromState().getLabel(),
                    lastUndone.getSymbol(),
                    lastUndone.getToState().getLabel()
            );

            undoStack.push(lastUndone);
        }
        System.out.println(graph);
    }



    @FXML
    public void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/azari/amirhossein/dfa_minimization/menu.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) drawingPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(700), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] symbolsArray;
    private String[] statesArray;
    private int currentStateIndex = 0;
    private String startState;
    private Set<String> finalStates = new HashSet<String>();
    private HashMap<String, HashMap<String, String>> graph = new HashMap<>();

    private final List<State> statesList = new ArrayList<>();
    private final List<Transition> transitionsList = new ArrayList<>();

    private State selectedState = null;
    private final Stack<Transition> undoStack = new Stack<>();
    private final Stack<Transition> redoStack = new Stack<>();
    @FXML
    public void initialize() {
        ParticleSystem particleSystem = new ParticleSystem(800, 600, 80);
        particleSystem.startAnimation(canvas);

            confirmButton.setOnMouseClicked(mouseEvent -> {
                if (!areInputsValid()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Input");
                    alert.setHeaderText("Please ensure all inputs are valid");
                    alert.setContentText(getErrorMessage());
                    alert.showAndWait();
                    return;
                }

                ArrayList<String> symbolsList = new ArrayList<>(Arrays.asList(symbolsArray));
                ArrayList<String> statesList = new ArrayList<>(Arrays.asList(statesArray));
                ArrayList<String> finalStatesList = new ArrayList<>(finalStates);

                MinimizationProcess process = new MinimizationProcess(symbolsList, statesList, startState, finalStatesList, graph, confirmButton);
                process.start();
            });


    }

    @FXML
    public void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        if (event.getButton() == MouseButton.SECONDARY) {
            addState(x, y);
        } else if (event.getButton() == MouseButton.PRIMARY) {
            State clickedState = getClickedState(x, y);
            if (clickedState != null) {
                handleStateSelection(clickedState, event.isControlDown());
            }else {
                handleTransitionClick(x, y);
            }
        }
        if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY && !event.isControlDown()) {  // Double-click to change state type
            State clickedState = getClickedState(x, y);
            if (clickedState != null) {
                showStateTypeDialog(clickedState);
            }
        }
    }

    // Add a new state to the DFA
    private void addState(double x, double y) {
        if (currentStateIndex < statesArray.length) {
            String stateName = String.valueOf(statesArray[currentStateIndex]);
            State newState = new State(currentStateIndex, stateName, false, false, x, y);
            newState.addStateChangeListener(this);
            statesList.add(newState);
            newState.draw(drawingPane);
            currentStateIndex++;

            graph.put(stateName, new HashMap<>());

        } else {
            showAlert("No more states", "All states have been added.");
        }
    }

    // Get the clicked state based on mouse coordinates
    private State getClickedState(double x, double y) {
        for (State state : statesList) {
            if (state.isClicked(x, y)) {
                return state;
            }
        }
        return null;
    }

    // Handle state selection logic
    private void handleStateSelection(State clickedState, boolean ctrlDown) {
        if (clickedState != null && ctrlDown) {
            clickedState.select();
            if (selectedState == null) {
                selectedState = clickedState;
                selectedState.select();
            } else {
                showMultiSymbolSelectionDialog().ifPresent(symbols -> {
                    String combinedSymbols = String.join(",", symbols);
                    addTransition(selectedState, clickedState, combinedSymbols);
                });
                selectedState.deselect();
                if (selectedState.isFinalState()){
                    selectedState.updateAppearance(drawingPane);
                }
                selectedState = null;
                clickedState.deselect();
                if (clickedState.isFinalState()){
                    clickedState.updateAppearance(drawingPane);
                }
            }
        }
    }

    // Add a transition between two states
    private void addTransition(State fromState, State toState, String symbols) {
        HashMap<String, String> stateTransitions = graph.getOrDefault(fromState.getLabel(), new HashMap<>());
        String[] symbolArray = symbols.split(",");

        for (String symbol : symbolArray) {
            String currentDestination = stateTransitions.get(symbol);
            if (currentDestination != null && !currentDestination.equals(toState.getLabel())) {
                showAlert("Invalid Transition",
                        "Symbol '" + symbol + "' is already used for state '" + fromState.getLabel() +
                                "' to reach state '" + currentDestination + "'");
                return;
            }
        }

        Transition existingForward = null;
        Transition existingReverse = null;

        for (Transition transition : transitionsList) {
            if (transition.getFromState() == fromState && transition.getToState() == toState) {
                existingForward = transition;
            } else if (transition.getFromState() == toState && transition.getToState() == fromState) {
                existingReverse = transition;
            }
        }

        if (existingForward != null) {
            removeTransitionOfGraph(fromState.getLabel() , existingForward.getSymbol() , toState.getLabel());
            int index = undoStack.indexOf(existingForward);
            if (index != -1) {
                undoStack.remove(index);
            }
            transitionsList.remove(existingForward);
            removeTransitionFromPane(existingForward);
        }

        boolean shouldBeCurved = existingReverse != null;
        Transition newTransition = new Transition(fromState, toState, symbols, shouldBeCurved);
        transitionsList.add(newTransition);
        newTransition.draw(drawingPane);

        undoStack.push(newTransition);
        redoStack.clear();
        // Split symbols and add each transition individually
        for (String symbol : symbols.split(",")) {
            graph.get(fromState.getLabel()).put(symbol, toState.getLabel());
        }
        if (existingReverse != null) {
            existingReverse.setCurved(true);
            removeTransitionFromPane(existingReverse);
            existingReverse.draw(drawingPane);
        }
        System.out.println(graph);
    }
    private void removeTransitionFromPane(Transition transition) {
        drawingPane.getChildren().remove(transition.getLine());
        drawingPane.getChildren().remove(transition.getArrow());
        drawingPane.getChildren().remove(transition.getText());
    }

    // Receive data from MenuController
    public void setData(String[] symbolsArray, String[] statesArray) {
        this.symbolsArray = symbolsArray;
        this.statesArray = statesArray;
    }

    // Show a dialog for selecting multiple symbols for a transition
    private Optional<List<String>> showMultiSymbolSelectionDialog() {
        List<String> selectedSymbols = new ArrayList<>();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Select Symbols");
        alert.setHeaderText("Choose symbols for the transition:");
        VBox checkboxContainer = new VBox();
        for (String symbol : symbolsArray) {
            CheckBox checkBox = new CheckBox(String.valueOf(symbol));
            checkboxContainer.getChildren().add(checkBox);
        }

        alert.getDialogPane().setContent(checkboxContainer);
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                for (Node node : checkboxContainer.getChildren()) {
                    CheckBox checkBox = (CheckBox) node;
                    if (checkBox.isSelected()) {
                        selectedSymbols.add(checkBox.getText());
                    }
                }
            }
        });

        return selectedSymbols.isEmpty() ? Optional.empty() : Optional.of(selectedSymbols);
    }
    // Show a dialog to change the state type (start, final, reset)
    private void showStateTypeDialog(State clickedState) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Set State Type");
        alert.setHeaderText("Choose the type of state:");

        ButtonType startButton = new ButtonType("Start State");
        ButtonType finalButton = new ButtonType("Final State");
        ButtonType resetButton = new ButtonType("Reset");
        alert.getButtonTypes().setAll(resetButton, startButton, finalButton, ButtonType.CANCEL);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("alertStyle.css")).toExternalForm());
        dialogPane.lookupButton(finalButton).getStyleClass().add("cancel-button");
        dialogPane.lookupButton(resetButton).getStyleClass().add("reset-button");
        dialogPane.lookupButton(startButton).getStyleClass().add("other-button");
        dialogPane.lookupButton(finalButton).getStyleClass().add("other-button");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == startButton) {
                setStartState(clickedState);
            } else if (result.get() == finalButton) {
                clickedState.setFinalState(true);
                finalStates.add(clickedState.getLabel());
            } else if (result.get() == resetButton) {
                if (startState != null && startState.equals(clickedState.getLabel())) {
                    startState = null;
                }
                finalStates.remove(clickedState.getLabel());
                clickedState.setStartState(false);
                clickedState.setFinalState(false);
            }
            clickedState.updateAppearance(drawingPane);
        }
    }
    // Set a state as the start state and update appearances
    private void setStartState(State newStartState) {
        for (State state : statesList) {
            if (state.isStartState()) {
                state.setStartState(false);
                state.updateAppearance(drawingPane);
                break;
            }
        }
        newStartState.setStartState(true);
        startState = newStartState.getLabel();
        newStartState.updateAppearance(drawingPane);
    }
    @Override
    public void onStateChanged(State state) {
        updateTransitions();
    }
    private void handleTransitionClick(double x, double y) {
        for (Transition transition : transitionsList) {
            if (transition.isClicked(x, y)) {
                transition.rotateSelfLoop();
                removeTransitionFromPane(transition);
                transition.draw(drawingPane);
                break;
            }
        }
    }
    private void updateTransitions() {
        for (Transition transition : transitionsList) {
            transition.updatePosition();
            transition.redraw(drawingPane);
        }
    }
    public void removeTransitionOfGraph(String outerKey, String innerKey, String value) {

        HashMap<String, String> innerMap = graph.get(outerKey);
        if (innerMap != null) {
            for (String symbol : innerKey.split(",")) {
                if (value.equals(innerMap.get(symbol))) {
                    innerMap.remove(symbol);

                }
            }
        }
    }
    public void addTransitionToGraph(String fromState, String symbols, String toState) {
        for (String symbol : symbols.split(",")) {
            if (!graph.containsKey(fromState)) {
                graph.put(fromState, new HashMap<>());
            }
            graph.get(fromState).put(symbol, toState);
        }
    }

    private boolean areInputsValid() {

        if (drawingPane.getChildren().stream().noneMatch(node -> node instanceof Circle)) {
            return false;
        }
        if (symbolsArray == null || symbolsArray.length == 0) {
            return false;
        }
        if (statesArray == null || statesArray.length == 0) {
            return false;
        }
        if (startState == null || startState.isEmpty()) {
            return false;
        }
        if (finalStates == null || finalStates.isEmpty()) {
            return false;
        }
        if (transitionsList.isEmpty()) {
            return false;
        }

        return true;
    }

    private String getErrorMessage() {
        if (drawingPane.getChildren().stream().noneMatch(node -> node instanceof Circle)) {
            return "No states are displayed on the screen.";
        }
        if (symbolsArray == null || symbolsArray.length == 0) {
            return "No symbols are defined.";
        }
        if (statesArray == null || statesArray.length == 0) {
            return "No states are defined.";
        }
        if (startState == null || startState.isEmpty()) {
            return "Start state is not set.";
        }
        if (finalStates == null || finalStates.isEmpty()) {
            return "No final states are defined.";
        }
        if (transitionsList.isEmpty()) {
            return "No transitions are defined.";
        }


        return "";
    }
}
