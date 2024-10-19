package azari.amirhossein.dfa_minimization.models;


import azari.amirhossein.dfa_minimization.utils.Constants;
import azari.amirhossein.dfa_minimization.utils.StateChangeListener;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import java.util.ArrayList;
import java.util.List;

public class State {

    private int id;
    private String label;
    private boolean isFinalState;
    private boolean isStartState;
    private double x;
    private double y;

    private Circle circle;
    private Text text;

    //Start state symbol
    private Line startArrow;
    private Polygon arrowHead;

    private List<StateChangeListener> listeners = new ArrayList<>();


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
        circle.setStrokeWidth(Constants.STROKE_CIRCLE_SIZE);
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

        if (isStartState) {
            updateStartArrow();
        }

        circle.setUserData(new double[]{sceneX, sceneY});

        // Notify listeners about the state change
        notifyListeners();
    }

    public void addStateChangeListener(StateChangeListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (StateChangeListener listener : listeners) {
            listener.onStateChanged(this);
        }
    }
    // Create arrow for start state
    private void createStartArrow() {
        double arrowSize = Constants.ARROW_SIZE;
        double startX = x - Constants.START_ARROW_OFFSET_X;
        double endX = x - Constants.END_ARROW_OFFSET_X;

        startArrow = new Line(startX, y, endX, y);
        startArrow.setStroke(Color.web(Constants.COLOR_TRANSITION));
        startArrow.setStrokeWidth(Constants.LINE_SIZE);

        arrowHead = new Polygon();
        double[] points = calculateArrowPoints(startX, endX, y, arrowSize);
        arrowHead.getPoints().addAll(points[0], points[1], points[2], points[3], points[4], points[5]);
        arrowHead.setFill(Color.web(Constants.COLOR_TRANSITION));
    }

    // Update appearance based on state type (start/accepting)
    public void updateAppearance(Pane pane) {
        circle.setStroke(isFinalState ? Color.web(Constants.COLOR_BLACK) : Color.web(Constants.COLOR_SILVER));

        if (isStartState && startArrow == null) {
            createStartArrow();
            pane.getChildren().addAll(startArrow, arrowHead);
        } else if (!isStartState && startArrow != null) {
            pane.getChildren().removeAll(startArrow, arrowHead);
            startArrow = null;
            arrowHead = null;
        }
    }
    private void updateStartArrow() {
        if (startArrow != null && arrowHead != null) {
            double arrowSize = Constants.ARROW_SIZE;
            double startX = x - Constants.START_ARROW_OFFSET_X;
            double endX = x - Constants.END_ARROW_OFFSET_X;

            startArrow.setStartX(startX);
            startArrow.setStartY(y);
            startArrow.setEndX(endX);
            startArrow.setEndY(y);

            double[] points = calculateArrowPoints(startX, endX, y, arrowSize);
            arrowHead.getPoints().setAll(points[0], points[1], points[2], points[3], points[4], points[5]);
        }
    }

    // Check if a point (x, y) is within the state circle (clicked)
    public boolean isClicked(double clickX, double clickY) {
        double radius = circle.getRadius();
        return Math.pow(clickX - x, 2) + Math.pow(clickY - y, 2) <= Math.pow(radius, 2);
    }

    // change color for select state
    public void select() {
        circle.setStroke(Color.web(Constants.COLOR_SELECTED));
        circle.setStrokeWidth(Constants.LINE_SIZE);
    }

    // change color for deselect state
    public void deselect() {
        circle.setStroke(Color.web(Constants.COLOR_SILVER));
        circle.setStrokeWidth(Constants.LINE_SIZE);
    }
    private double[] calculateArrowPoints(double startX, double endX, double y, double arrowSize) {
        double angle = Math.atan2(0, endX - startX);
        double arrowX1 = endX - arrowSize * Math.cos(angle - Math.PI / 6);
        double arrowY1 = y - arrowSize * Math.sin(angle - Math.PI / 6);
        double arrowX2 = endX - arrowSize * Math.cos(angle + Math.PI / 6);
        double arrowY2 = y - arrowSize * Math.sin(angle + Math.PI / 6);

        return new double[] {endX, y, arrowX1, arrowY1, arrowX2, arrowY2};
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
    public void setFinalState(boolean finalState) {
        isFinalState = finalState;
    }

    public boolean isStartState() {
        return isStartState;
    }

    public void setStartState(boolean startState) {
        isStartState = startState;
    }
}
