package nl.andrewlalis.threadripper.engine;

import lombok.Getter;

/**
 * Simple vector for two double values, X and Y.
 */
@Getter
public class Vec2 {
	private final double x;
	private final double y;

	public Vec2(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public static Vec2 fromPolar(double radius, double theta) {
		return new Vec2(
				radius * Math.cos(theta),
				radius * Math.sin(theta)
		);
	}

	public Vec2 add(Vec2 other) {
		return new Vec2(this.getX() + other.getX(), this.getY() + other.getY());
	}

	public Vec2 multiply(double factor) {
		return new Vec2(this.getX() * factor, this.getY() * factor);
	}

	public double distance(Vec2 other) {
		final double deltaX = this.getX() - other.getX();
		final double deltaY = this.getY() - other.getY();
		return Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof Vec2) {
			Vec2 other = (Vec2) obj;
			return other.getX() == this.getX() && other.getY() == this.getY();
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("[%f, %f]", this.getX(), this.getY());
	}

	public Vec2 getCopy() {
		return new Vec2(this.getX(), this.getY());
	}
}
