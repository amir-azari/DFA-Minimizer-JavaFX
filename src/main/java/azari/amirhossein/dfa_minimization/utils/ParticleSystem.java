package azari.amirhossein.dfa_minimization.utils;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleSystem {
    private final List<Particle> particles = new ArrayList<>();
    private final double canvasWidth;
    private final double canvasHeight;
    private double mouseX = 0.0;
    private double mouseY = 0.0;
    private Timeline timeline;

    private static class Particle {
        double x, y, vx, vy;
        Color color;

        Particle(double x, double y, double vx, double vy, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
        }
    }

    public ParticleSystem(double canvasWidth, double canvasHeight, int numParticles) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;

        Random random = new Random();
        for (int i = 0; i < numParticles; i++) {
            double x = random.nextDouble() * canvasWidth;
            double y = random.nextDouble() * canvasHeight;
            double vx = (random.nextDouble() * 2 - 1) * 0.5;
            double vy = (random.nextDouble() * 2 - 1) * 0.5;
            Color color = Color.BLACK;
            particles.add(new Particle(x, y, vx, vy, color));
        }
    }

    public void startAnimation(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            updateParticles();
            drawParticles(gc);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        canvas.setOnMouseMoved(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
        });
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    private void updateParticles() {
        for (Particle particle : particles) {
            particle.vx *= 0.9999;
            particle.vy *= 0.9999;
            particle.x += particle.vx;
            particle.y += particle.vy;

            if (particle.x < 0 || particle.x > canvasWidth) particle.vx *= -1;
            if (particle.y < 0 || particle.y > canvasHeight) particle.vy *= -1;

            double dx = mouseX - particle.x;
            double dy = mouseY - particle.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < 100) {
                particle.vx += dx / distance * 0.1;
                particle.vy += dy / distance * 0.1;
            }
        }
    }

    private void drawParticles(GraphicsContext gc) {
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1.0);
        for (int i = 0; i < particles.size(); i++) {
            Particle p1 = particles.get(i);
            for (int j = i + 1; j < particles.size(); j++) {
                Particle p2 = particles.get(j);
                double dx = p2.x - p1.x;
                double dy = p2.y - p1.y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance < 100) {
                    gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }

        for (Particle particle : particles) {
            gc.setFill(particle.color);
            gc.fillOval(particle.x, particle.y, 5, 5);
        }
    }
}
