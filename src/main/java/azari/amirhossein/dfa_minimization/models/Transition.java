package azari.amirhossein.dfa_minimization.models;

import azari.amirhossein.dfa_minimization.utils.Constants;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

public class Transition {
    private State fromState;
    private State toState;
    private String symbol;
    private boolean isSelfLoop;

    private Shape line;
    private Polygon arrow;
    private Text text;
    private boolean isCurved;
    private Pane pane;

    private static final double OFFSET_ANGLE = Math.PI / 12;

    public Transition(State fromState, State toState, String symbol, boolean isCurved) {
        this.fromState = fromState;
        this.toState = toState;
        this.symbol = symbol;
        this.isCurved = isCurved;
        this.isSelfLoop = fromState == toState;

        createLine();
    }

    // Create line or curve line
    private void createLine() {
        double startX = fromState.getX();
        double startY = fromState.getY();
        double endX = toState.getX();
        double endY = toState.getY();

        double radius = Constants.RADIUS;
        double angle = Math.atan2(endY - startY, endX - startX);

        if (isSelfLoop){
            startX = fromState.getX() - (Constants.RADIUS/2);
            startY = fromState.getY() - Constants.RADIUS;
            endX = startX + Constants.RADIUS;
            endY = startY;
            double controlX1 = startX - 25;
            double controlY1 = startY - 25 ;
            double controlX2 = endX + 25;
            double controlY2 = endY - 25;

            line = new CubicCurve(startX, startY, controlX1, controlY1, controlX2, controlY2, endX, endY);
            line.setFill(null);
            line.setStroke(Color.web(Constants.COLOR_TRANSITION));
            line.setStrokeWidth(2);
        }else if (isCurved) {
            // Calculate offset angles for start and end
            double startAngle = angle + OFFSET_ANGLE;
            double endAngle = angle - OFFSET_ANGLE;

            Point2D startPoint = calculateLineEndPoint(startX, startY, radius, startAngle);
            Point2D endPoint = calculateLineEndPoint(endX, endY, -radius, endAngle);


            double midX = (startPoint.getX() + endPoint.getX()) / 2;
            double midY = (startPoint.getY() + endPoint.getY()) / 2;

            double controlOffsetX = (startY - endY) * Constants.CURVE_CONTROL_OFFSET;
            double controlOffsetY = (endX - startX) * Constants.CURVE_CONTROL_OFFSET;

            CubicCurve curve = new CubicCurve(
                    startPoint.getX(), startPoint.getY(),
                    midX + controlOffsetX, midY + controlOffsetY,
                    midX + controlOffsetX, midY + controlOffsetY,
                    endPoint.getX(), endPoint.getY()
            );

            curve.setFill(null);
            curve.setStroke(Color.web(Constants.COLOR_TRANSITION));
            curve.setStrokeWidth(Constants.LINE_SIZE);
            line = curve;
        } else {
            Point2D startPoint = calculateLineEndPoint(startX, startY, radius, angle);
            Point2D endPoint = calculateLineEndPoint(endX, endY, -radius, angle);

            Line straightLine = new Line(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
            straightLine.setStroke(Color.web(Constants.COLOR_TRANSITION));
            straightLine.setStrokeWidth(Constants.LINE_SIZE);
            line = straightLine;
        }
    }

    // Calculate the coordinates of the start and end points
    private Point2D calculateLineEndPoint(double x, double y, double radius, double angle) {
        double lineX = x + radius * Math.cos(angle);
        double lineY = y + radius * Math.sin(angle);
        return new Point2D(lineX, lineY);
    }

    // Draw an arrow at the end of the line
    private void drawArrow(Pane pane) {
        double endX, endY, angle;

        if (line instanceof Line l) {
            endX = l.getEndX();
            endY = l.getEndY();
            angle = Math.atan2(l.getEndY() - l.getStartY(), l.getEndX() - l.getStartX());
        } else {
            CubicCurve c = (CubicCurve) line;
            endX = c.getEndX();
            endY = c.getEndY();
            double controlX = c.getControlX2();
            double controlY = c.getControlY2();
            double dx = endX - controlX;
            double dy = endY - controlY;
            angle = Math.atan2(dy, dx);
        }

        double arrowSize = Constants.ARROW_SIZE;

        double arrowX1 = endX - arrowSize * Math.cos(angle - Math.PI / 6);
        double arrowY1 = endY - arrowSize * Math.sin(angle - Math.PI / 6);
        double arrowX2 = endX - arrowSize * Math.cos(angle + Math.PI / 6);
        double arrowY2 = endY - arrowSize * Math.sin(angle + Math.PI / 6);

        arrow = new Polygon();
        arrow.getPoints().addAll(endX, endY, arrowX1, arrowY1, arrowX2, arrowY2);
        arrow.setFill(Color.web(Constants.COLOR_TRANSITION));

        pane.getChildren().add(arrow);
    }

    // Draw the symbol in the middle of the line
    private void drawSymbol(Pane pane) {
        if (isSelfLoop) {
            drawSelfLoopSymbol(pane);
        }else {
            double[] coordinates = calculateCoordinates();
            double x = coordinates[0];
            double y = coordinates[1];
            double nx = coordinates[2];
            double ny = coordinates[3];

            double offset = Constants.SYMBOL_OFFSET;
            x += nx * offset;
            y += ny * offset;

            text = new Text(symbol);
            text.setX(x - text.getBoundsInLocal().getWidth() / 2);
            text.setY(y + text.getBoundsInLocal().getHeight() / 4);
            pane.getChildren().add(text);
        }
    }
    private void drawSelfLoopSymbol(Pane pane) {
        CubicCurve curve = (CubicCurve) line;
        double t = 0.5;

        double x = calculateBezierCoordinate(curve.getStartX(), curve.getControlX1(), curve.getControlX2(), curve.getEndX(), t);
        double y = calculateBezierCoordinate(curve.getStartY(), curve.getControlY1(), curve.getControlY2(), curve.getEndY(), t);

        text = new Text(symbol);
        text.setX(x - text.getBoundsInLocal().getWidth() / 2);
        text.setY(y - text.getBoundsInLocal().getHeight() / 2);
        pane.getChildren().add(text);
    }
    //calculate coordinates for curve line and straight line
    private double[] calculateCoordinates() {
        double x, y, nx, ny;
        if (line instanceof Line l) {
            x = (l.getStartX() + l.getEndX()) / 2;
            y = (l.getStartY() + l.getEndY()) / 2;
            double dx = l.getEndX() - l.getStartX();
            double dy = l.getEndY() - l.getStartY();
            double length = Math.sqrt(dx * dx + dy * dy);
            nx = -dy / length;
            ny = dx / length;
        } else {
            CubicCurve c = (CubicCurve) line;
            double t = 0.5;
            x = calculateBezierCoordinate(c.getStartX(), c.getControlX1(), c.getControlX2(), c.getEndX(), t);
            y = calculateBezierCoordinate(c.getStartY(), c.getControlY1(), c.getControlY2(), c.getEndY(), t);

            double dx = calculateBezierDerivative(c.getStartX(), c.getControlX1(), c.getControlX2(), c.getEndX(), t);
            double dy = calculateBezierDerivative(c.getStartY(), c.getControlY1(), c.getControlY2(), c.getEndY(), t);

            double length = Math.sqrt(dx * dx + dy * dy);
            nx = -dy / length;
            ny = dx / length;
        }
        return new double[]{x, y, nx, ny};
    }

    private double calculateBezierCoordinate(double p0, double p1, double p2, double p3, double t) {
        return Math.pow(1 - t, 3) * p0 +
                3 * Math.pow(1 - t, 2) * t * p1 +
                3 * (1 - t) * Math.pow(t, 2) * p2 +
                Math.pow(t, 3) * p3;
    }

    private double calculateBezierDerivative(double p0, double p1, double p2, double p3, double t) {
        return -3 * Math.pow(1 - t, 2) * p0 +
                3 * (Math.pow(1 - t, 2) - 2 * t * (1 - t)) * p1 +
                3 * (2 * t * (1 - t) - Math.pow(t, 2)) * p2 +
                3 * Math.pow(t, 2) * p3;
    }

    // add views to pane
    public void draw(Pane pane) {
        this.pane = pane;
        pane.getChildren().add(line);
        drawArrow(pane);
        drawSymbol(pane);
    }

    public void updatePosition() {
        if (pane != null) {
            pane.getChildren().remove(line);
            pane.getChildren().remove(arrow);
            pane.getChildren().remove(text);
        }
        createLine();
    }

    public void redraw(Pane pane) {
        this.pane = pane;
        pane.getChildren().add(line);
        drawArrow(pane);
        drawSymbol(pane);
    }

    public void setCurved(boolean curved) {
        if (this.isCurved != curved) {
            this.isCurved = curved;
            if (pane != null) {
                pane.getChildren().remove(line);
                pane.getChildren().remove(arrow);
                pane.getChildren().remove(text);
            }
            createLine();
            if (pane != null) {
                // Redraw the transition
                draw(pane);
            }
        }
    }

    public Shape getLine() {
        return line;
    }

    public State getFromState() {
        return fromState;
    }

    public State getToState() {
        return toState;
    }

    public Polygon getArrow() {
        return arrow;
    }

    public Text getText() {
        return text;
    }
}