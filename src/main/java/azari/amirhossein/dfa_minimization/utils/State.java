package azari.amirhossein.dfa_minimization.utils;



import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

public class State {
    private int id;
    private String label;
    private boolean isFinalState;
    private boolean isStartState;
    private double x;
    private double y;

    private Circle circle;
    private Text text;

    public State(int id, String label, boolean isFinalState, boolean isStartState, double x, double y) {
        this.id = id;
        this.label = label;
        this.isFinalState = isFinalState;
        this.isStartState = isStartState;
        this.x = x;
        this.y = y;

        createCircle();
        createText();
    }

    // Create state circle
    private void createCircle() {
        circle = new Circle(x, y, Constants.RADIUS);
        circle.setStrokeWidth(2);
        circle.setStroke(Color.web(Constants.COLOR_SILVER));
        circle.setFill(Color.web(Constants.COLOR_BLUE));
    }

    // Create state label
    private void createText() {
        text = new Text(label);
        text.setFont(Font.font("System", FontWeight.BOLD, 14));
        text.setBoundsType(TextBoundsType.LOGICAL);
        centerText();
    }
    // Center text in the state circle
    private void centerText() {
        double textWidth = text.getBoundsInLocal().getWidth();
        double textHeight = text.getBoundsInLocal().getHeight();
        text.setX(x - textWidth / 2);
        text.setY(y + textHeight / 4);
    }

    // Draw the state on the pane
    public void draw(Pane pane) {
        pane.getChildren().addAll(circle, text);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }



}
