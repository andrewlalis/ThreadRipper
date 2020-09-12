package nl.andrewlalis.threadripper.particle;

import nl.andrewlalis.threadripper.engine.Vec2;

import java.util.concurrent.ThreadLocalRandom;

public class ParticleFactory {
	private final double minMass;
	private final double maxMass;
	private final double minCharge;
	private final double maxCharge;
	private final Vec2 minPosition;
	private final Vec2 maxPosition;
	private final Vec2 minVelocity;
	private final Vec2 maxVelocity;

	public ParticleFactory(
			double minMass,
			double maxMass,
			double minCharge,
			double maxCharge,
			Vec2 minPosition,
			Vec2 maxPosition,
			Vec2 minVelocity,
			Vec2 maxVelocity
	) {
		this.minMass = minMass;
		this.maxMass = maxMass;
		this.minCharge = minCharge;
		this.maxCharge = maxCharge;
		this.minPosition = minPosition;
		this.maxPosition = maxPosition;
		this.minVelocity = minVelocity;
		this.maxVelocity = maxVelocity;
	}

	public Particle build() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		final Vec2 position = new Vec2(
				random.nextDouble(this.minPosition.getX(), this.maxPosition.getX()),
				random.nextDouble(this.minPosition.getY(), this.maxPosition.getY())
		);
		final Vec2 velocity = new Vec2(
				random.nextDouble(this.minVelocity.getX(), this.maxVelocity.getX()),
				random.nextDouble(this.minVelocity.getY(), this.maxVelocity.getY())
		);
		final double mass = random.nextDouble(this.minMass, this.maxMass);
//		final double charge = random.nextDouble(this.minCharge, this.maxCharge);

		final double charge = 0;
		return new Particle(
				position,
				velocity,
				mass,
				charge
		);
	}

}
