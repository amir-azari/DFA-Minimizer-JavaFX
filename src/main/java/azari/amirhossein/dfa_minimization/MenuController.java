package azari.amirhossein.dfa_minimization;

import azari.amirhossein.dfa_minimization.animation.ButtonAnimation;
import azari.amirhossein.dfa_minimization.animation.TextFieldAnimation;
import azari.amirhossein.dfa_minimization.utils.ParticleSystem;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static azari.amirhossein.dfa_minimization.utils.FXUtils.showAlert;

public class MenuController {
    @FXML
    private Canvas canvas;
    @FXML
    private Button confirmButton;
    @FXML
    private TextField symbolsField;
    @FXML
    private TextField statesField;

    //Other
    private String[] symbolsArray;
    private String[] statesArray;

    @FXML
    public void initialize() {
        // Confirm Btn animation
        ButtonAnimation.addHoverAnimation(confirmButton);

        // TextField animation
        TextFieldAnimation.addHoverOpacityAnimation(symbolsField);
        TextFieldAnimation.addHoverOpacityAnimation(statesField);

        // Add Particle system
        ParticleSystem particleSystem = new ParticleSystem(800, 600, 80);
        particleSystem.startAnimation(canvas);

        confirmButton.setOnMouseClicked(mouseEvent -> {
            String symbols = symbolsField.getText();
            String states = statesField.getText();

            if (!symbols.isEmpty() && !states.isEmpty()) {
                symbolsArray = processInput(symbols);
                statesArray = processInput(states);

                if (hasDuplicates(symbolsArray) || hasDuplicates(statesArray)) {
                    showAlert("Error", "Fields must not contain duplicate characters.");
                } else {
                    // Pass data to DFAController
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/azari/amirhossein/dfa_minimization/dfa.fxml"));
                        Parent root = loader.load();

                        // Get DFAController and pass data
                        DFAController dfaController = loader.getController();
                        dfaController.setData(symbolsArray, statesArray);

                        // Set the scene and show
                        Stage stage = (Stage) confirmButton.getScene().getWindow();
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
            } else {
                showAlert("Error", "Please fill both fields.");
            }
        });
    }
    //Check duplicate
    public static boolean hasDuplicates(String[] symbolsArray) {
        Set<String> seenSymbols = new HashSet<>();
        for (String symbol : symbolsArray) {
            if (!seenSymbols.add(symbol)) {
                return true;
            }
        }
        return false;
    }

    private String[] processInput(String input) {

        String[] parts = input.trim().split(" ");
        return Arrays.stream(parts)
                .filter(part -> part != null && !part.isEmpty())
                .toArray(String[]::new);
    }


}
