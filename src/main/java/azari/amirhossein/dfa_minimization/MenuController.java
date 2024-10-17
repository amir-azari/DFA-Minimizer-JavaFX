package azari.amirhossein.dfa_minimization;

import azari.amirhossein.dfa_minimization.animation.ButtonAnimation;
import azari.amirhossein.dfa_minimization.animation.TextFieldAnimation;
import azari.amirhossein.dfa_minimization.utils.ParticleSystem;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.HashSet;
import java.util.Set;

import static azari.amirhossein.dfa_minimization.utils.FXUtils.loadSceneFromFXML;
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
    private char[] symbolsArray;
    private char[] statesArray;

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
            // Get data
            String symbols = symbolsField.getText();
            String states = statesField.getText();

            // validation input
            if (!symbols.isEmpty() && !states.isEmpty()) {
                symbolsArray = processInput(symbols);
                statesArray = processInput(states);

                if (hasDuplicates(symbolsArray) || hasDuplicates(statesArray) ){
                    showAlert("Error", "Fields must not contain duplicate characters.");

                }else {

                    loadSceneFromFXML("/azari/amirhossein/dfa_minimization/dfa.fxml", mouseEvent);
                }

            } else {
                showAlert("Error", "Please fill both fields.");
            }


        });
    }
    //Check duplicate
    public static boolean hasDuplicates(char[] symbolsArray) {
        Set<Character> seenSymbols = new HashSet<>();
        for (char symbol : symbolsArray) {
            if (!seenSymbols.add(symbol)) {
                return true;
            }
        }
        return false;
    }

    private char[] processInput(String input) {
        String[] parts = input.trim().split(" ");
        StringBuilder charBuilder = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                charBuilder.append(part.trim().charAt(0));
            }
        }
        return charBuilder.toString().toCharArray();
    }

}
