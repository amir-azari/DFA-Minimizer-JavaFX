package azari.amirhossein.dfa_minimization.utils;



import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
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
    // Make the state circle and text draggable
    private void makeDraggable() {
        EventHandler<MouseEvent> onPressed = event -> {
            circle.setUserData(new double[]{event.getSceneX(), event.getSceneY()});
        };

        EventHandler<MouseEvent> onDragged = event -> {
            handleDrag(event.getSceneX(), event.getSceneY());
        };

        circle.setOnMousePressed(onPressed);
        circle.setOnMouseDragged(onDragged);
        text.setOnMousePressed(onPressed);
        text.setOnMouseDragged(onDragged);
    }
    // Handle dragging of the state and related updates
    private void handleDrag(double sceneX, double sceneY) {

        double[] startCoords = (double[]) circle.getUserData();
        double deltaX = sceneX - startCoords[0];
        double deltaY = sceneY - startCoords[1];

        x += deltaX;
        y += deltaY;
        circle.setCenterX(x);
        circle.setCenterY(y);
        centerText();

        circle.setUserData(new double[]{sceneX, sceneY});
    }
    // Check if a point (x, y) is within the state circle (clicked)
    public boolean isClicked(double clickX, double clickY) {
        double radius = circle.getRadius();
        return Math.pow(clickX - x, 2) + Math.pow(clickY - y, 2) <= Math.pow(radius, 2);
    }
    // change color for select state
    public void select() {
        circle.setStroke(Color.web(Constants.COLOR_SELECTED));
        circle.setStrokeWidth(2);
    }

    // change color for deselect state
    public void deselect() {
        circle.setStroke(Color.web(Constants.COLOR_SILVER));
        circle.setStrokeWidth(2);
    }
    // Draw the state on the pane
    public void draw(Pane pane) {
        pane.getChildren().addAll(circle, text);
        makeDraggable();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }



}
