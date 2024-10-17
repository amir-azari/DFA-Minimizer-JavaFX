package azari.amirhossein.dfa_minimization.animation;

import javafx.animation.FadeTransition;
import javafx.scene.control.TextField;
import javafx.util.Duration;


public class TextFieldAnimation {

    public static void addHoverOpacityAnimation(TextField textField) {

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), textField);
        fadeIn.setToValue(0.8);


        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), textField);
        fadeOut.setToValue(1.0);

        textField.setOnMouseEntered(e -> fadeIn.playFromStart());
        textField.setOnMouseExited(e -> fadeOut.playFromStart());
    }
}

