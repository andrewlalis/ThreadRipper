package nl.andrewlalis.threadripper.engine;

import lombok.extern.slf4j.Slf4j;
import nl.andrewlalis.threadripper.particle.Particle;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class ParticleChamber implements Runnable {
	private static final int DEFAULT_THREAD_POOL = 100;
	private static final double DEFAULT_UPDATES_PER_SECOND = 60;

	private final Set<Particle> particles;
	private double simulationRate = 1.0;
	private boolean allowCollision = true;

	private int threadCount;
	private final ExecutorService executorService;
	private final CompletionService<ParticleUpdate> particleUpdateService;

	private boolean running;
	private double updateFps;
	private double secondsSinceLastUpdate;

	public ParticleChamber() {
		this.particles = new HashSet<>();

		this.threadCount = DEFAULT_THREAD_POOL;
		this.executorService = Executors.newFixedThreadPool(this.threadCount);
		this.particleUpdateService = new ExecutorCompletionService<>(this.executorService);

		this.updateFps = DEFAULT_UPDATES_PER_SECOND;
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

	public synchronized void setSimulationRate(double simulationRate) {
		this.simulationRate = simulationRate;
	}

	public synchronized void setAllowCollision(boolean allowCollision) {
		this.allowCollision = allowCollision;
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
			this.secondsSinceLastUpdate = millisecondsSinceLastUpdate / 1000.0;

			final double millisecondsPerFrame = 1000.0 / this.updateFps;

			if (millisecondsSinceLastUpdate > millisecondsPerFrame) {
				final double secondsSinceLastUpdate = millisecondsSinceLastUpdate / 1000.0;
				millisecondsSinceLastUpdate = 0L;
				//log.info("Updating particles after {} seconds elapsed.", secondsSinceLastUpdate);
				this.updateParticles(secondsSinceLastUpdate * this.simulationRate);
			}

			previousTimeMilliseconds = currentTimeMilliseconds;
		}
		this.executorService.shutdown();
		log.info("Particle chamber stopped.");
	}

	/**
	 * Updates all the particles in the simulation.
	 * @param deltaTime The amount of seconds that have passed since the last update.
	 */
	private void updateParticles(double deltaTime) {
		// First submit a new callable task for each particle.
		for (Particle particle : this.particles) {
			this.particleUpdateService.submit(new ParticleUpdater(particle, this.particles, this.allowCollision));
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

		Set<Particle> particlesToRemove = new HashSet<>(this.particles.size());
		Set<Particle> particlesToAdd = new HashSet<>(this.particles.size());
		// Implement the updates for each particle.
		for (ParticleUpdate update : updates) {
			update.getFocusParticle().updateVelocity(update.getAcceleration(), deltaTime);
			update.getFocusParticle().updatePosition(deltaTime);
			if (!update.getCollidesWith().isEmpty() && !particlesToRemove.contains(update.getFocusParticle())) {
				particlesToRemove.addAll(update.getCollidesWith());
				particlesToRemove.add(update.getFocusParticle());
				// Create a new particle as combination.
				Particle p = update.getFocusParticle();
				for (Particle collided : update.getCollidesWith()) {
					p = p.combine(collided);
				}
				particlesToAdd.add(p);
			}
		}

		this.particles.removeAll(particlesToRemove);
		this.particles.addAll(particlesToAdd);
	}

	public double getSecondsSinceLastUpdate() {
		return this.secondsSinceLastUpdate;
	}

	public double getSimulationRate() {
		return simulationRate;
	}

	public Set<Particle> getCopyOfParticles() {
		Set<Particle> set = new HashSet<>(this.particles.size());
		for (Particle p : this.particles) {
			set.add(p.getCopy());
		}
		return set;
	}
}
