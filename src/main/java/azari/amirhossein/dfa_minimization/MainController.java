package azari.amirhossein.dfa_minimization;

import azari.amirhossein.dfa_minimization.animation.ButtonAnimation;
import azari.amirhossein.dfa_minimization.utils.ParticleSystem;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import static azari.amirhossein.dfa_minimization.utils.FXUtils.loadSceneFromFXML;

public class MainController {
    @FXML
    private Canvas canvas;

    @FXML
    private Button startButton;

    @FXML
    public void initialize() {
        // Start Btn animation
        ButtonAnimation.addHoverAnimation(startButton);

        // Add Particle system
        ParticleSystem particleSystem = new ParticleSystem(800, 600, 80);
        particleSystem.startAnimation(canvas);

        // Open menu page
        startButton.setOnMouseClicked(mouseEvent -> loadSceneFromFXML("/azari/amirhossein/dfa_minimization/menu.fxml", mouseEvent));
    }


}
