package it.cavallium.warppi.extra.mario;

public final class MarioEnemy extends MarioEntity {

	private static final double SIZE = 0.8d;
	private static final double FRAME_SECONDS = 0.14d;

	private final double spawnX;
	private final double spawnY;
	private final int initialDirection;
	private int direction;
	private double animationElapsed;
	private boolean active;

	public MarioEnemy(final double x, final double y) {
		this(x, y, -1.35d, 0, true, true);
	}

	public MarioEnemy(final double x, final double y, final double forceX, final double forceY,
			final boolean onGround, final boolean subjectToGravity) {
		super(x, y, forceX, forceY, onGround, subjectToGravity);
		spawnX = x;
		spawnY = y;
		initialDirection = forceX > 0 ? 1 : -1;
		direction = initialDirection;
		active = true;
	}

	@Override
	public double getWidth() {
		return SIZE;
	}

	@Override
	public double getHeight() {
		return SIZE;
	}

	void reset() {
		setPosition(spawnX, spawnY, true);
		stop();
		direction = initialDirection;
		animationElapsed = 0d;
		active = true;
	}

	void updateAnimation(final double dt) {
		animationElapsed += dt;
	}

	public int getSpriteFrame() {
		return (int) (animationElapsed / FRAME_SECONDS) & 1;
	}

	public boolean isActive() {
		return active;
	}

	void deactivate() {
		active = false;
		stop();
	}

	public int getDirection() {
		return direction;
	}

	void turnAround() {
		direction = -direction;
	}
}
