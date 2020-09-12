package nl.andrewlalis.threadripper.engine;

import lombok.extern.slf4j.Slf4j;
import nl.andrewlalis.threadripper.particle.Particle;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Callable which is dedicated to finding the net force which is applied to a
 * particular particle of interest, with respect to all other particles.
 */
@Slf4j
public class ParticleUpdater implements Callable<ParticleUpdate> {
	private final Particle focusParticle;
	private final Set<Particle> particles;
	private final boolean allowCollision;

	public ParticleUpdater(
			Particle focusParticle,
			Set<Particle> particles,
			boolean allowCollision
	) {
		this.focusParticle = focusParticle;
		this.particles = particles;
		this.allowCollision = allowCollision;
	}

	@Override
	public ParticleUpdate call() throws Exception {
		double accelerationX = 0L;
		double accelerationY = 0L;
		Set<Particle> collidesWith = new HashSet<>();
		for (Particle particle : this.particles) {
			if (!particle.equals(this.focusParticle)) {
				Vec2 partialAcceleration = this.computeAcceleration(particle);
				accelerationX += partialAcceleration.getX();
				accelerationY += partialAcceleration.getY();

				// Collision detection:
				final double distance = this.focusParticle.getPosition().distance(particle.getPosition());
				if (
						Math.abs(this.focusParticle.getRadius() - particle.getRadius()) <= distance
						&& distance <= (this.focusParticle.getRadius() + particle.getRadius())
				) {
					collidesWith.add(particle);
				}
			}
		}
		return new ParticleUpdate(this.focusParticle, new Vec2(accelerationX, accelerationY), collidesWith);
	}

	/**
	 * Computes the acceleration which another particle imparts on this updater's
	 * focus particle.
	 * @param other The other particle which is acting upon the focus particle.
	 * @return The acceleration, in m/s^2, which the focus particle experiences
	 * from this particle.
	 */
	private Vec2 computeAcceleration(Particle other) {
		final double radius = this.focusParticle.getPosition().distance(other.getPosition());
		final double dY = other.getPosition().getY() - this.focusParticle.getPosition().getY();
		final double dX = other.getPosition().getX() - this.focusParticle.getPosition().getX();

		final double angle = Math.atan2(dY, dX);

		final double gravityNewtons = Constants.G * (this.focusParticle.getMass() * other.getMass()) / Math.pow(radius, 2);
		final double gravityAcceleration = gravityNewtons / this.focusParticle.getMass();
		final Vec2 gravityAccelerationVector = Vec2.fromPolar(gravityAcceleration, angle);

		final double emNewtons = Constants.Ke * (this.focusParticle.getCharge() * other.getCharge()) / Math.pow(radius, 2);
		final boolean isRepulsion = emNewtons < 0.0;
		double emAcceleration = emNewtons / this.focusParticle.getMass();
		if (isRepulsion) {
			emAcceleration *= -1.0;
		}
		final Vec2 emAccelerationVector = Vec2.fromPolar(emAcceleration, angle);

		final Vec2 totalAcceleration = gravityAccelerationVector.add(emAccelerationVector);
		return totalAcceleration;
	}
}
