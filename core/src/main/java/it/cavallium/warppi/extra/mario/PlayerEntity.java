package it.cavallium.warppi.extra.mario;

public final class PlayerEntity extends MarioEntity {

	private static final int[] WALK_FRAMES = { 1, 2, 3 };
	private static final int[] RUN_FRAMES = { 4, 5, 8 };
	private static final int JUMP_FRAME = 6;
	private static final int SMALL_RUN_JUMP_FRAME = 7;
	private static final int SUPER_DUCK_FRAME = 7;
	private static final double WIDTH = 0.72d;
	private static final double SMALL_HEIGHT = 1d;
	private static final double SUPER_HEIGHT = 1.8d;

	private final int life;
	private double animationElapsed;
	private double jumpBufferTime;
	private double coyoteTime;
	private double invulnerabilityTime;
	private boolean jumpHeld;
	private boolean walking;
	private boolean running;
	private boolean jumping;
	private boolean facingLeft;
	private boolean superMario;
	private boolean ducking;
	private int spriteFrame;

	public PlayerEntity(final double x, final double y, final int life) {
		super(x, y, 0, 0, true, true);
		this.life = life;
	}

	@Override
	public double getWidth() {
		return WIDTH;
	}

	@Override
	public double getHeight() {
		return superMario && !ducking ? SUPER_HEIGHT : SMALL_HEIGHT;
	}

	double getStandingHeight() {
		return superMario ? SUPER_HEIGHT : SMALL_HEIGHT;
	}

	public int getLife() {
		return life;
	}

	void reset(final double spawnX, final double spawnY) {
		setPosition(spawnX, spawnY, true);
		stop();
		collisionUp = false;
		collisionLeft = false;
		collisionRight = false;
		jumpBufferTime = 0;
		coyoteTime = MarioGame.COYOTE_SECONDS;
		invulnerabilityTime = 0;
		jumpHeld = false;
		walking = false;
		running = false;
		jumping = false;
		facingLeft = false;
		superMario = false;
		ducking = false;
		spriteFrame = 0;
		animationElapsed = 0;
	}

	void queueJump() {
		jumpBufferTime = MarioGame.JUMP_BUFFER_SECONDS;
	}

	void setJumpHeld(final boolean jumpHeld) {
		this.jumpHeld = jumpHeld;
	}

	boolean isJumpHeld() {
		return jumpHeld;
	}

	void tickAssistanceTimers(final double dt) {
		jumpBufferTime = Math.max(0d, jumpBufferTime - dt);
		invulnerabilityTime = Math.max(0d, invulnerabilityTime - dt);
		if (collisionDown)
			coyoteTime = MarioGame.COYOTE_SECONDS;
		else
			coyoteTime = Math.max(0d, coyoteTime - dt);
	}

	boolean canUseBufferedJump() {
		return jumpBufferTime > 0d && coyoteTime > 0d && !ducking;
	}

	void consumeBufferedJump() {
		jumpBufferTime = 0d;
		coyoteTime = 0d;
	}

	void setDucking(final boolean requested, final boolean canStand) {
		if (!superMario) {
			ducking = false;
			return;
		}
		ducking = requested || !canStand;
	}

	void powerUp() {
		superMario = true;
		ducking = true;
	}

	boolean takeDamage() {
		if (invulnerabilityTime > 0d)
			return false;
		if (superMario) {
			superMario = false;
			ducking = false;
			invulnerabilityTime = 1.4d;
			return false;
		}
		return true;
	}

	void updateAnimation(final double dt, final int horizontalInput, final boolean runInput) {
		if (horizontalInput > 0)
			facingLeft = false;
		else if (horizontalInput < 0)
			facingLeft = true;

		walking = collisionDown && !ducking && Math.abs(forceX) > 0.15d;
		running = walking && runInput
				&& Math.abs(forceX) > MarioGame.WALK_SPEED + 0.25d;
		jumping = !collisionDown;
		if (ducking) {
			spriteFrame = SUPER_DUCK_FRAME;
			animationElapsed = 0;
		} else if (jumping) {
			spriteFrame = !superMario && runInput && Math.abs(forceX) > 4.8d
					? SMALL_RUN_JUMP_FRAME : JUMP_FRAME;
			animationElapsed = 0;
		} else if (walking) {
			animationElapsed += dt;
			final int[] frames = running ? RUN_FRAMES : WALK_FRAMES;
			final double frameSeconds = running ? 0.085d : 0.14d;
			spriteFrame = frames[(int) (animationElapsed / frameSeconds) % frames.length];
		} else {
			spriteFrame = 0;
			animationElapsed = 0;
		}
	}

	public boolean isWalking() {
		return walking;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isJumping() {
		return jumping;
	}

	public boolean isFacingLeft() {
		return facingLeft;
	}

	/** Compatibility alias retained for older callers. */
	public boolean isFlipped() {
		return facingLeft;
	}

	public boolean isSuperMario() {
		return superMario;
	}

	public boolean isDucking() {
		return ducking;
	}

	public boolean isInvulnerable() {
		return invulnerabilityTime > 0d;
	}

	public boolean isVisibleDuringInvulnerability() {
		return !isInvulnerable() || ((int) (invulnerabilityTime * 20d) & 1) == 0;
	}

	public int getSpriteFrame() {
		return spriteFrame;
	}
}
