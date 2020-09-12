package nl.andrewlalis.threadripper.render;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import nl.andrewlalis.threadripper.engine.ParticleChamber;
import nl.andrewlalis.threadripper.engine.Vec2;
import nl.andrewlalis.threadripper.particle.Particle;

import java.util.LinkedList;
import java.util.Set;

/**
 * The renderer is responsible for drawing a particle chamber's contents to
 * a canvas.
 */
@Slf4j
public class ParticleChamberRenderer implements Runnable {
	private static final double DEFAULT_FPS = 60.0;
	private static final int FPS_READING_COUNT = 360;

	private final ParticleChamber chamber;
	private final Canvas canvas;

	private double targetFps;
	private boolean running;

	private Vec2 offset;
	private double scale;

	private LinkedList<Double> recentFpsReadings;

	public ParticleChamberRenderer(ParticleChamber chamber, Canvas canvas) {
		this.chamber = chamber;
		this.canvas = canvas;

		this.targetFps = DEFAULT_FPS;
		this.recentFpsReadings = new LinkedList<>();

		this.offset = new Vec2(0, 0);
		this.scale = 1.0;
	}

	public synchronized void setRunning(boolean running) {
		this.running = running;
	}

	public synchronized void setTargetFps(double targetFps) {
		if (targetFps > 0) {
			this.targetFps = targetFps;
		}
	}

	public synchronized void setOffset(Vec2 offset) {
		this.offset = offset;
	}

	public synchronized void setScale(double scale) {
		if (scale > 0) {
			this.scale = scale;
		}
	}

	@Override
	public void run() {
		this.running = true;
		long previousTimeMilliseconds = System.currentTimeMillis();
		long millisecondsSinceLastFrame = 0L;

		log.info("Starting particle chamber renderer.");
		while (this.running) {
			final long currentTimeMilliseconds = System.currentTimeMillis();
			final long elapsedMilliseconds = currentTimeMilliseconds - previousTimeMilliseconds;

			millisecondsSinceLastFrame += elapsedMilliseconds;

			final double millisecondsPerFrame = 1000.0 / this.targetFps;

			if (millisecondsSinceLastFrame > millisecondsPerFrame) {
				final double currentFps = 1000.0 / millisecondsSinceLastFrame;
				this.updateFpsReading(currentFps);
				this.draw();
				millisecondsSinceLastFrame = 0L;
			}

			previousTimeMilliseconds = currentTimeMilliseconds;
		}
		log.info("Particle chamber renderer stopped.");
	}

	private void draw() {
		final double secondsSinceLastUpdate = this.chamber.getSecondsSinceLastUpdate();
		final Set<Particle> particles = this.chamber.getCopyOfParticles();
		Platform.runLater(() -> {
			GraphicsContext gc = this.canvas.getGraphicsContext2D();
			gc.setFill(Color.WHITE);
			gc.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());


			for (Particle particle : particles) {
				particle.updatePosition(secondsSinceLastUpdate);
				Vec2 pos = particle.getPosition().add(this.offset);
				if (particle.getCharge() > 0) {
					gc.setFill(Color.BLUE);
				} else {
					gc.setFill(Color.RED);
				}
				gc.fillOval(
						pos.getX() - particle.getRadius(),
						pos.getY() - particle.getRadius(),
						particle.getRadius() * 2,
						particle.getRadius() * 2
				);
			}

			gc.setFill(Color.BLACK);
			gc.fillText(String.format("FPS: %.2f", this.getAverageFps()), 10, 10);
		});
	}

	private void updateFpsReading(double recentFpsReading) {
		this.recentFpsReadings.addFirst(recentFpsReading);
		if (this.recentFpsReadings.size() > FPS_READING_COUNT) {
			this.recentFpsReadings.removeLast();
		}
	}

	public double getAverageFps() {
		if (this.recentFpsReadings.size() == 0) {
			return 0.0;
		}
		double sum = 0.0;
		for (double reading : this.recentFpsReadings) {
			sum += reading;
		}
		return sum / this.recentFpsReadings.size();
	}
}
