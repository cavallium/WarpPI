package org.warp.picalculator.extra.mario;

public class PlayerEntity extends MarioEntity {

	@SuppressWarnings("unused")
	private final int life;
	public float walkAnimation = 0;
	public float jumptime = 0;
	public boolean walking = false;
	public boolean running = false;
	public boolean jumping = false;
	public boolean flipped = false;
	public int[] marioSkinPos = new int[] { 0, 0 };
	private double controllerDX;
	private double controllerDY;

	public PlayerEntity(double x, double y, int life) {
		super(x, y, 0, 0, true, true);
		this.life = life;
	}

	@Override
	public void gameTick(double dt) {
		walkAnimation += dt;
		x += computeFutureDX(dt);
		y += computeFutureDY(dt);
		forceX += computeFutureForceDX(dt);
		forceY += computeFutureForceDY(dt);
		if (controllerDX == 0) {
			walking = false;
			walkAnimation = 0;
		} else {
			if (controllerDX > 0) { //RIGHT
				walking = true;
				flipped = false;
			}
			if (controllerDX < 0) { //LEFT
				walking = true;
				flipped = true;
			}
		}
		if (controllerDY > 0) { //JUMP
			if (collisionUp) {
				jumptime = Float.MAX_VALUE;
				jumping = false;
			}
			jumptime += dt;
			if (jumptime <= 0.5f && !jumping && collisionDown) {
				jumping = true;
				collisionDown = false;
			} else if (jumptime <= 0.5f) {} else {
				jumping = false;
			}
		} else {
			jumping = false;
			if (collisionDown) {
				jumptime = 0;
			} else {
				jumptime = Float.MAX_VALUE;
			}
		}
		if (!walking & !running & !jumping) {
			marioSkinPos[0] = 0;
			marioSkinPos[1] = 0;
		} else if (collisionDown & walking & !running & !jumping && walkAnimation >= 0.08) {
			while (walkAnimation > 0.08) {
				walkAnimation -= 0.08;
				if (marioSkinPos[0] == 1 & marioSkinPos[1] == 0) {
					marioSkinPos[0] += 2;
				} else if (marioSkinPos[0] == 3 & marioSkinPos[1] == 0) {
					marioSkinPos[0] -= 1;
				} else if (marioSkinPos[0] == 2 & marioSkinPos[1] == 0) {
					marioSkinPos[0] -= 1;
				} else {
					marioSkinPos[0] = 1;
					marioSkinPos[1] = 0;
				}
			}
		} else if (jumping) {
			marioSkinPos[0] = 5;
			marioSkinPos[1] = 1;
		}
	}

	@Override
	public double computeFutureDX(double dt) {
		return super.computeFutureDX(dt);
	}

	public double computeFuturedDY(double dt) {
		return super.computeFutureDY(dt);
	}

	@Override
	public double computeFutureForceDX(double dt) {
		double forceX = this.forceX;
		if (controllerDX == 0) {} else {
			if (controllerDX > 0) { //RIGHT
				if (forceX < 500f / 16f) {
					forceX += dt * 500f / 16f;
				}
			}
			if (controllerDX < 0) { //LEFT
				if (forceX > -500f / 16f) {
					forceX -= dt * 500f / 16f;
				}
			}
		}

		return (forceX + super.computeFutureForceDX(dt)) - this.forceX;
	}

	@Override
	public double computeFutureForceDY(double dt) {
		float jumptime = this.jumptime;
		double forceY = this.forceY;
		if (controllerDY > 0) { //JUMP
			if (collisionUp) {
				jumptime = Float.MAX_VALUE;
			}
			jumptime += dt;
			if (jumptime <= 0.5f && !jumping && collisionDown) {
				forceY = dt * (4 * 1569.6f) / 16f;
			} else if (jumptime <= 0.5f) {
				forceY = dt * (4 * 1569.6f) / 16f;
			}
		}
		return (forceY + super.computeFutureForceDY(dt)) - this.forceY;
	}

	public void move(float dt, double dX, double dY) {
		walkAnimation += dt;

		controllerDX = dX;
		controllerDY = dY;
	}

}
