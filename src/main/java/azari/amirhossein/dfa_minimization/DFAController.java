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

    @FXML
    public void initialize() {

    }

    @FXML
    public void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        if (event.getButton() == MouseButton.SECONDARY) {
            addState(x, y);
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


    // Receive data from MenuController
    public void setData(char[] symbolsArray, char[] statesArray) {
        this.symbolsArray = symbolsArray;
        this.statesArray = statesArray;
    }
    
}
