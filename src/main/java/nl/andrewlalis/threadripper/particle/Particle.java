package nl.andrewlalis.threadripper.particle;

import lombok.Getter;
import nl.andrewlalis.threadripper.engine.Vec2;

import java.util.Objects;

/**
 * Represents a single particle that exists in a simulation.
 */
@Getter
public class Particle {
	private static long NEXT_PARTICLE_ID = 1L;

	/**
	 * Unique id for this particle.
	 */
	private final long id;

	/**
	 * The particle's position, in meters.
	 */
	private Vec2 position;

	/**
	 * The particle's velocity, in meters.
	 */
	private Vec2 velocity;

	/**
	 * The particle's mass, in kilograms.
	 */
	private double mass;

	/**
	 * The particle's charge, in coulombs.
	 */
	private double charge;

	/**
	 * The particle's radius, in meters.
	 */
	private double radius;

	public Particle(Vec2 position) {
		this(position, new Vec2(0, 0), 1, 0, 1);
	}

	public Particle(Vec2 position, double mass, double charge, double radius) {
		this(position, new Vec2(0, 0), mass, charge, radius);
	}

	public Particle(Vec2 position, Vec2 velocity, double mass, double charge, double radius) {
		this.position = position;
		this.velocity = velocity;
		this.mass = mass;
		this.charge = charge;
		this.radius = radius;

		this.id = NEXT_PARTICLE_ID;
		NEXT_PARTICLE_ID++;
	}

	/**
	 * Updates this particle's velocity, according to a delta velocity value,
	 * @param acceleration Acceleration due to net force, in meters per second,
	 *                     squared.
	 * @param deltaTime The amount of time elapsed, in seconds.
	 */
	public void updateVelocity(Vec2 acceleration, double deltaTime) {
		this.velocity = this.velocity.add(acceleration.multiply(deltaTime));
	}

	/**
	 * Updates this particle's position, according to its current velocity.
	 * @param deltaTime The amount of time that has elapsed, in seconds.
	 */
	public void updatePosition(double deltaTime) {
		this.position = this.position.add(this.getVelocity().multiply(deltaTime));
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof Particle) {
			Particle other = (Particle) obj;
			return other.getId() == this.getId();
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format(
				"{id: %d, mass: %f, charge: %f, position: %s, velocity: %s}",
				this.getId(),
				this.getMass(),
				this.getCharge(),
				this.getPosition(),
				this.getVelocity()
		);
	}

	public Particle getCopy() {
		return new Particle(
				this.getPosition().getCopy(),
				this.getVelocity().getCopy(),
				this.getMass(),
				this.getCharge(),
				this.getRadius()
		);
	}

	public Particle combine(Particle other) {
		final Vec2 position = new Vec2(
				(this.getPosition().getX() + other.getPosition().getX()) / 2.0,
				(this.getPosition().getY() + other.getPosition().getY()) / 2.0
		);
		final Vec2 velocity = new Vec2(
				(this.getVelocity().getX() + other.getVelocity().getX()) / 2.0,
				(this.getVelocity().getY() + other.getVelocity().getY()) / 2.0
		);
		return new Particle(
				position,
				velocity,
				this.getMass() + other.getMass(),
				this.getCharge() + other.getCharge(),
				this.getRadius() + other.getRadius()
		);
	}
}
