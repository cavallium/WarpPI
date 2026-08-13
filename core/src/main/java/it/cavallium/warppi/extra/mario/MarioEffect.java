package it.cavallium.warppi.extra.mario;

final class MarioEffect {

	enum Type {
		COIN_PICKUP,
		BLOCK_BUMP,
		BRICK_BREAK,
		SPARKLE
	}

	private Type type;
	private double x;
	private double y;
	private double elapsed;
	private double duration;
	private boolean active;

	void start(final Type type, final double x, final double y, final double duration) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.duration = duration;
		elapsed = 0d;
		active = true;
	}

	void update(final double dt) {
		if (!active)
			return;
		elapsed += dt;
		if (elapsed >= duration)
			active = false;
	}

	void clear() {
		active = false;
	}

	boolean isActive() {
		return active;
	}

	Type getType() {
		return type;
	}

	double getX() {
		return x;
	}

	double getY() {
		return y;
	}

	double getElapsed() {
		return elapsed;
	}

	double getProgress() {
		return duration <= 0d ? 1d : Math.min(1d, elapsed / duration);
	}
}
