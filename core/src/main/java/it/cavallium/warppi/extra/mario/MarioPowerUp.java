package it.cavallium.warppi.extra.mario;

final class MarioPowerUp extends MarioEntity {

	private static final double SIZE = 0.8d;
	private static final double EMERGE_SECONDS = 0.55d;

	private double emergeStartY;
	private double emergeTargetY;
	private double emergeElapsed;
	private int direction;
	private boolean emerging;
	private boolean active;

	MarioPowerUp() {
		super(0d, 0d, 0d, 0d, false, true);
	}

	@Override
	public double getWidth() {
		return SIZE;
	}

	@Override
	public double getHeight() {
		return SIZE;
	}

	void spawn(final double x, final double blockY) {
		emergeStartY = blockY + 0.05d;
		emergeTargetY = blockY + 1.02d;
		setPosition(x + (1d - SIZE) / 2d, emergeStartY, false);
		forceX = 0d;
		forceY = 0d;
		emergeElapsed = 0d;
		direction = 1;
		emerging = true;
		active = true;
	}

	void updateEmergence(final double dt) {
		emergeElapsed = Math.min(EMERGE_SECONDS, emergeElapsed + dt);
		final double progress = emergeElapsed / EMERGE_SECONDS;
		setPosition(getX(), emergeStartY + (emergeTargetY - emergeStartY) * progress);
		if (emergeElapsed >= EMERGE_SECONDS) {
			emerging = false;
			collisionDown = false;
		}
	}

	void deactivate() {
		active = false;
		emerging = false;
		stop();
	}

	boolean isActive() {
		return active;
	}

	boolean isEmerging() {
		return emerging;
	}

	int getDirection() {
		return direction;
	}

	void turnAround() {
		direction = -direction;
	}
}
