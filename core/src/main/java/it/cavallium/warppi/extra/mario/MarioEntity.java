package it.cavallium.warppi.extra.mario;

/** Basic mutable body shared by the player and level enemies. */
public class MarioEntity {
	protected double x;
	protected double y;
	public double forceX;
	public double forceY;
	public boolean collisionUp;
	public boolean collisionDown;
	public boolean collisionLeft;
	public boolean collisionRight;
	public boolean subjectToGravity;

	public MarioEntity(final double x, final double y, final double forceX, final double forceY,
			final boolean onGround, final boolean subjectToGravity) {
		this.x = x;
		this.y = y;
		this.forceX = forceX;
		this.forceY = forceY;
		collisionDown = onGround;
		this.subjectToGravity = subjectToGravity;
	}

	public void setPosition(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	public void setPosition(final double x, final double y, final boolean onGround) {
		setPosition(x, y);
		collisionDown = onGround;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getVelocityX() {
		return forceX;
	}

	public double getVelocityY() {
		return forceY;
	}

	public double getWidth() {
		return 1d;
	}

	public double getHeight() {
		return 1d;
	}

	public boolean isOnGround() {
		return collisionDown;
	}

	public void setOnGround(final boolean onGround) {
		collisionDown = onGround;
	}

	public void stop() {
		forceX = 0;
		forceY = 0;
	}

	/**
	 * Simple integration retained for passive entities. The game uses swept,
	 * axis-separated movement for collidable actors.
	 */
	public void gameTick(final double dt) {
		x += computeFutureDX(dt);
		y += computeFutureDY(dt);
		forceX += computeFutureForceDX(dt);
		forceY += computeFutureForceDY(dt);
	}

	public double computeFutureDX(final double dt) {
		return dt * forceX;
	}

	public double computeFutureDY(final double dt) {
		return collisionDown && forceY <= 0 ? 0 : dt * forceY;
	}

	public double computeFutureForceDX(final double dt) {
		final double decay = Math.pow(0.75d, Math.max(0d, dt) * 20d);
		return forceX * decay - forceX;
	}

	public double computeFutureForceDY(final double dt) {
		if (subjectToGravity && !collisionDown)
			return -MarioGame.GRAVITY * dt;
		return -forceY;
	}
}
