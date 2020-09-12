package nl.andrewlalis.threadripper.engine;

import lombok.Getter;
import nl.andrewlalis.threadripper.particle.Particle;

import java.util.Set;

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

	/**
	 * A set of particles that this one has collided with.
	 */
	private final Set<Particle> collidesWith;

	public ParticleUpdate(Particle focusParticle, Vec2 acceleration, Set<Particle> collidesWith) {
		this.focusParticle = focusParticle;
		this.acceleration = acceleration;
		this.collidesWith = collidesWith;
	}
}
