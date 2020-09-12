package nl.andrewlalis.threadripper.engine;

import lombok.Getter;
import nl.andrewlalis.threadripper.particle.Particle;

/**
 * Describes an update to a particle's motion.
 */
@Getter
public class ParticleUpdate {
	/**
	 * The particle to be updated.
	 */
	private final Particle focusParticle;

	/**
	 * The acceleration that the particle experiences, in meters per second
	 * squared.
	 */
	private final Vec2 acceleration;

	public ParticleUpdate(Particle focusParticle, Vec2 acceleration) {
		this.focusParticle = focusParticle;
		this.acceleration = acceleration;
	}
}
