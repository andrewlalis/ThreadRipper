package nl.andrewlalis.threadripper.engine;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import nl.andrewlalis.threadripper.particle.Particle;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class ParticleChamber implements Runnable {
	private static final int DEFAULT_THREAD_POOL = 100;
	private static final double DEFAULT_UPDATE_FPS = 60;

	private final Set<Particle> particles;
	private int threadCount;
	private ExecutorService executorService;
	private CompletionService<ParticleUpdate> particleUpdateService;

	private boolean running;
	private double updateFps;

	private final Canvas canvas;

	public ParticleChamber(Canvas canvas) {
		this.particles = new HashSet<>();

		this.threadCount = DEFAULT_THREAD_POOL;
		this.executorService = Executors.newFixedThreadPool(this.threadCount);
		this.particleUpdateService = new ExecutorCompletionService<>(this.executorService);

		this.updateFps = DEFAULT_UPDATE_FPS;

		this.canvas = canvas;
	}

	/**
	 * Adds one or more particles to the chamber.
	 * @param particles The particles to add.
	 */
	public void addParticle(Particle... particles) {
		this.particles.addAll(Arrays.asList(particles));
	}

	public synchronized void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public void run() {
		this.running = true;
		long previousTimeMilliseconds = System.currentTimeMillis();
		long millisecondsSinceLastUpdate = 0L;

		log.info("Starting particle chamber.");
		while (this.running) {
			final long currentTimeMilliseconds = System.currentTimeMillis();
			final long elapsedMilliseconds = currentTimeMilliseconds - previousTimeMilliseconds;

			millisecondsSinceLastUpdate += elapsedMilliseconds;

			final double millisecondsPerFrame = 1000.0 / this.updateFps;

			if (millisecondsSinceLastUpdate > millisecondsPerFrame) {
				final double secondsSinceLastUpdate = millisecondsSinceLastUpdate / 1000.0;
				millisecondsSinceLastUpdate = 0L;
				//log.info("Updating particles after {} seconds elapsed.", secondsSinceLastUpdate);
				this.updateParticles(secondsSinceLastUpdate);
				this.drawParticles();
			}

			previousTimeMilliseconds = currentTimeMilliseconds;
		}
		log.info("Particle chamber stopped.");
	}

	/**
	 * Updates all the particles in the simulation.
	 * @param deltaTime The amount of seconds that have passed since the last update.
	 */
	private void updateParticles(double deltaTime) {
		// First submit a new callable task for each particle.
		for (Particle particle : this.particles) {
			this.particleUpdateService.submit(new ParticleUpdater(particle, this.particles));
		}

		int updatesReceived = 0;
		boolean errorEncountered = false;
		final List<ParticleUpdate> updates = new ArrayList<>(this.particles.size());

		// Iterate until we've received the results of each particle updater's calculations.
		while (updatesReceived < this.particles.size() && !errorEncountered) {
			try {
				Future<ParticleUpdate> updateFuture = this.particleUpdateService.take();
				updates.add(updateFuture.get());
				updatesReceived++;
			} catch (Exception e) {
				e.printStackTrace();
				errorEncountered = true;
			}
		}

		// Implement the updates for each particle.
		for (ParticleUpdate update : updates) {
			update.getFocusParticle().updateVelocity(update.getAcceleration(), deltaTime);
			update.getFocusParticle().updatePosition(deltaTime);
			//log.info("Particle updated: {}", update.getFocusParticle().toString());
		}
	}

	/**
	 * Draws all particles on the canvas.
	 */
	private void drawParticles() {
		Platform.runLater(() -> {
			GraphicsContext gc = this.canvas.getGraphicsContext2D();
			gc.setFill(Color.WHITE);
			gc.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
			gc.setFill(Color.BLACK);
			gc.setStroke(Color.BLUE);
			for (Particle particle : this.particles) {
				Vec2 pos = particle.getPosition();
				gc.fillOval(pos.getX() - 3, pos.getY() - 3, 6, 6);
			}
		});
	}
}
