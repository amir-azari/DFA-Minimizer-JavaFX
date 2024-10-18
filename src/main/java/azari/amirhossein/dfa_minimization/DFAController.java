package azari.amirhossein.dfa_minimization;

import azari.amirhossein.dfa_minimization.utils.State;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static azari.amirhossein.dfa_minimization.utils.FXUtils.showAlert;

import javafx.scene.layout.VBox;

public class DFAController {
    @FXML
    private Pane drawingPane;

    private char[] symbolsArray;
    private char[] statesArray;
    private int currentStateIndex = 0;

    private final List<State> statesList = new ArrayList<>();

    private State selectedState = null;

    @FXML
    public void initialize() {

    }

    @FXML
    public void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        if (event.getButton() == MouseButton.SECONDARY) {
            addState(x, y);
        }else if (event.getButton() == MouseButton.PRIMARY) {
            State clickedState = getClickedState(x, y);
            if (clickedState != null) {
                handleStateSelection(clickedState, event.isControlDown());
            }
        }
    }

    // Add a new state to the DFA
    private void addState(double x, double y) {
        if (currentStateIndex < statesArray.length) {
            String stateName = String.valueOf(statesArray[currentStateIndex]);
            State newState = new State(currentStateIndex, stateName, false, false, x, y);
            statesList.add(newState);
            newState.draw(drawingPane);
            currentStateIndex++;
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
            }else {
                showMultiSymbolSelectionDialog().ifPresent(symbols -> {


                });
                selectedState.deselect();
                selectedState = null;
                clickedState.deselect();
            }
        }
    }
    // Receive data from MenuController
    public void setData(char[] symbolsArray, char[] statesArray) {
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
        for (char symbol : symbolsArray) {
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
}
