package azari.amirhossein.dfa_minimization.animation;

import javafx.animation.ScaleTransition;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class ButtonAnimation {

    public static void addHoverAnimation(Button button) {
        //Zoom animation
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), button);
        scaleUp.setToX(1.1);
        scaleUp.setToY(1.1);

        button.setOnMouseEntered(e -> scaleUp.playFromStart());

        //Minimize animation
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), button);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);


        button.setOnMouseExited(e -> scaleDown.playFromStart());
    }
}
