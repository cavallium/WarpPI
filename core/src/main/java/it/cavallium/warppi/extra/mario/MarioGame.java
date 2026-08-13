package it.cavallium.warppi.extra.mario;

public final class MarioGame {

	public enum State {
		PLAYING,
		DEAD,
		COURSE_CLEAR,
		WON
	}

	static final double GRAVITY = 32d;
	static final double COYOTE_SECONDS = 0.11d;
	static final double JUMP_BUFFER_SECONDS = 0.13d;

	private static final double FIXED_STEP_SECONDS = 1d / 60d;
	private static final int MAX_STEPS_PER_TICK = 12;
	private static final double MAX_FRAME_SECONDS = FIXED_STEP_SECONDS * MAX_STEPS_PER_TICK;
	static final double WALK_SPEED = 4.2d;
	static final double RUN_SPEED = 7d;
	private static final double DUCK_SPEED = 2.2d;
	private static final double GROUND_ACCELERATION = 26d;
	private static final double GROUND_TURN_ACCELERATION = 52d;
	private static final double AIR_ACCELERATION = 18d;
	private static final double GROUND_FRICTION = 38d;
	private static final double AIR_FRICTION = 5d;
	private static final double JUMP_SPEED = 11.4d;
	private static final double RUN_JUMP_BONUS = 0.8d;
	private static final double JUMP_RELEASE_SPEED = 5.4d;
	private static final double HELD_JUMP_GRAVITY = 19d;
	private static final double ENEMY_SPEED = 1.35d;
	private static final double MUSHROOM_SPEED = 2.1d;
	private static final double ENEMY_ACTIVE_DISTANCE = 12d;
	private static final double STOMP_BOUNCE_SPEED = 7.4d;
	private static final double DAMAGE_BOUNCE_SPEED = 5.5d;
	private static final double DEATH_DELAY_SECONDS = 0.7d;
	private static final double COURSE_CLEAR_SECONDS = 1.15d;
	private static final double GOAL_SPARKLE_SECONDS = 0.13d;
	private static final double COLLISION_EPSILON = 0.000001d;
	private static final int EFFECT_POOL_SIZE = 24;
	private static final int POWER_UP_POOL_SIZE = 4;

	private final PlayerEntity player;
	private final MarioWorld[] worlds;
	private final MarioEffect[] effects;
	private final MarioPowerUp[] powerUps;
	private int currentWorld;
	private int nextEffectIndex;
	private double accumulator;
	private int horizontalInput;
	private boolean runInput;
	private boolean crouchInput;
	private boolean jumpPressedSinceLastSimulation;
	private boolean jumpHeldInput;
	private double previousPlayerY;
	private State state;
	private double stateTimer;
	private double goalSparkleTimer;
	private int goalSparklePhase;
	private double elapsedSeconds;
	private double checkpointX;
	private int checkpointIndex;
	private int score;
	private int deaths;

	public MarioGame() {
		this(MarioWorld.createLevels());
	}

	MarioGame(final MarioWorld world) {
		this(new MarioWorld[] { world });
	}

	private MarioGame(final MarioWorld[] sourceWorlds) {
		if (sourceWorlds == null || sourceWorlds.length == 0)
			throw new IllegalArgumentException("At least one Mario world is required");
		worlds = sourceWorlds.clone();
		player = new PlayerEntity(worlds[0].getSpawnPointX(), worlds[0].getSpawnPointY(), 5);
		effects = new MarioEffect[EFFECT_POOL_SIZE];
		for (int i = 0; i < effects.length; i++)
			effects[i] = new MarioEffect();
		powerUps = new MarioPowerUp[POWER_UP_POOL_SIZE];
		for (int i = 0; i < powerUps.length; i++)
			powerUps[i] = new MarioPowerUp();
		restart();
	}

	public void restart() {
		for (final MarioWorld world : worlds)
			world.reset();
		currentWorld = 0;
		checkpointX = getCurrentWorld().getSpawnPointX();
		checkpointIndex = 0;
		score = 0;
		deaths = 0;
		elapsedSeconds = 0;
		accumulator = 0;
		jumpPressedSinceLastSimulation = false;
		jumpHeldInput = false;
		state = State.PLAYING;
		stateTimer = 0;
		goalSparkleTimer = 0;
		goalSparklePhase = 0;
		clearTransientObjects();
		player.reset(getCurrentWorld().getSpawnPointX(), getCurrentWorld().getSpawnPointY());
		previousPlayerY = player.getY();
	}

	private void respawn() {
		final MarioWorld world = getCurrentWorld();
		world.resetEnemies();
		clearTransientObjects();
		player.reset(checkpointX, world.findStandingY(checkpointX));
		state = State.PLAYING;
		stateTimer = 0;
		accumulator = 0;
		jumpPressedSinceLastSimulation = false;
		jumpHeldInput = false;
		previousPlayerY = player.getY();
	}

	private void beginNextWorld() {
		if (currentWorld + 1 >= worlds.length) {
			state = State.WON;
			return;
		}
		final boolean keepPower = player.isSuperMario();
		currentWorld++;
		final MarioWorld world = getCurrentWorld();
		world.reset();
		checkpointX = world.getSpawnPointX();
		checkpointIndex = 0;
		clearTransientObjects();
		player.reset(world.getSpawnPointX(), world.getSpawnPointY());
		if (keepPower)
			player.powerUp();
		previousPlayerY = player.getY();
		state = State.PLAYING;
		stateTimer = 0;
		accumulator = 0;
	}

	private void clearTransientObjects() {
		for (final MarioEffect effect : effects)
			effect.clear();
		for (final MarioPowerUp powerUp : powerUps)
			powerUp.deactivate();
		nextEffectIndex = 0;
	}

	public MarioBlock getBlockAt(final int x, final int y) {
		return getCurrentWorld().getBlockAt(x, y);
	}

	public MarioBlock getBlockAt(final double x, final double y) {
		return getBlockAt((int) Math.floor(x), (int) Math.floor(y));
	}

	public MarioWorld getCurrentWorld() {
		return worlds[currentWorld];
	}

	public void gameTick(final float dt, final boolean upPressed, final boolean downPressed,
			final boolean leftPressed, final boolean rightPressed, final boolean jumpPressed,
			final boolean runPressed) {
		if (!Float.isFinite(dt) || dt <= 0f)
			return;

		horizontalInput = (rightPressed ? 1 : 0) - (leftPressed ? 1 : 0);
		runInput = runPressed;
		crouchInput = downPressed;
		final boolean effectiveJump = jumpPressed || upPressed;
		if (effectiveJump && !jumpHeldInput)
			jumpPressedSinceLastSimulation = true;
		jumpHeldInput = effectiveJump;

		final double frameSeconds = Math.min(dt, MAX_FRAME_SECONDS);
		accumulator = Math.min(accumulator + frameSeconds,
				FIXED_STEP_SECONDS * MAX_STEPS_PER_TICK);
		int steps = 0;
		while (accumulator + COLLISION_EPSILON >= FIXED_STEP_SECONDS
				&& steps < MAX_STEPS_PER_TICK) {
			if (jumpPressedSinceLastSimulation) {
				player.queueJump();
				jumpPressedSinceLastSimulation = false;
			}
			player.setJumpHeld(jumpHeldInput);
			simulate(FIXED_STEP_SECONDS);
			accumulator -= FIXED_STEP_SECONDS;
			steps++;
		}
		if (accumulator < 0)
			accumulator = 0;
	}

	private void simulate(final double dt) {
		elapsedSeconds += dt;
		updateEffects(dt);
		if (state == State.DEAD) {
			stateTimer -= dt;
			if (stateTimer <= 0)
				respawn();
			return;
		}
		if (state == State.COURSE_CLEAR) {
			updateGoalSparkles(dt);
			stateTimer -= dt;
			if (stateTimer <= 0)
				beginNextWorld();
			return;
		}
		if (state == State.WON)
			return;

		previousPlayerY = player.getY();
		updatePlayer(dt);
		collectCoins();
		updatePowerUps(dt);
		updateEnemies(dt);
		updateCheckpoint();

		if (player.getY() < -4d)
			killPlayer();
		else if (player.getX() + player.getWidth() >= getCurrentWorld().getGoalX())
			completeCourse();
	}

	private void updatePlayer(final double dt) {
		player.tickAssistanceTimers(dt);
		player.setDucking(crouchInput, canPlayerStand());
		if (player.canUseBufferedJump()) {
			player.consumeBufferedJump();
			final double runningRatio = Math.min(1d, Math.abs(player.forceX) / RUN_SPEED);
			player.forceY = JUMP_SPEED + RUN_JUMP_BONUS * runningRatio;
			player.collisionDown = false;
		}

		final double maximumSpeed = player.isDucking()
				? DUCK_SPEED : runInput ? RUN_SPEED : WALK_SPEED;
		final double targetSpeed = horizontalInput * maximumSpeed;
		final double acceleration;
		if (horizontalInput == 0)
			acceleration = player.collisionDown ? GROUND_FRICTION : AIR_FRICTION;
		else if (player.collisionDown && player.forceX * horizontalInput < -0.1d)
			acceleration = GROUND_TURN_ACCELERATION;
		else
			acceleration = player.collisionDown ? GROUND_ACCELERATION : AIR_ACCELERATION;
		player.forceX = approach(player.forceX, targetSpeed, acceleration * dt);

		if (!player.isJumpHeld() && player.forceY > JUMP_RELEASE_SPEED)
			player.forceY = JUMP_RELEASE_SPEED;
		final double gravity = player.isJumpHeld() && player.forceY > 0
				? HELD_JUMP_GRAVITY : GRAVITY;
		player.forceY -= gravity * dt;

		moveHorizontally(player, player.forceX * dt);
		moveVertically(player, player.forceY * dt, true);
		player.updateAnimation(dt, horizontalInput, runInput);
	}

	private boolean canPlayerStand() {
		if (!player.isSuperMario() || !player.isDucking())
			return true;
		final int minX = (int) Math.floor(player.getX() + COLLISION_EPSILON);
		final int maxX = (int) Math.floor(player.getX() + player.getWidth() - COLLISION_EPSILON);
		final int minY = (int) Math.floor(player.getY() + player.getHeight() + COLLISION_EPSILON);
		final int maxY = (int) Math.floor(player.getY() + player.getStandingHeight() - COLLISION_EPSILON);
		for (int y = minY; y <= maxY; y++)
			for (int x = minX; x <= maxX; x++)
				if (getCurrentWorld().isSolidAt(x, y))
					return false;
		return true;
	}

	private void collectCoins() {
		final MarioWorld world = getCurrentWorld();
		final int minX = (int) Math.floor(player.getX() + COLLISION_EPSILON);
		final int maxX = (int) Math.floor(player.getX() + player.getWidth() - COLLISION_EPSILON);
		final int minY = (int) Math.floor(player.getY() + COLLISION_EPSILON);
		final int maxY = (int) Math.floor(player.getY() + player.getHeight() - COLLISION_EPSILON);
		for (int y = minY; y <= maxY; y++)
			for (int x = minX; x <= maxX; x++)
				if (world.collectCoinAt(x, y)) {
					score += 10;
					spawnEffect(MarioEffect.Type.COIN_PICKUP, x + 0.5d, y + 0.5d, 0.45d);
				}
	}

	private void updatePowerUps(final double dt) {
		for (final MarioPowerUp powerUp : powerUps) {
			if (!powerUp.isActive())
				continue;
			if (powerUp.isEmerging()) {
				powerUp.updateEmergence(dt);
				continue;
			}
			if (powerUp.collisionDown && !hasSupportAhead(powerUp, powerUp.getDirection()))
				powerUp.turnAround();
			powerUp.forceX = powerUp.getDirection() * MUSHROOM_SPEED;
			powerUp.forceY -= GRAVITY * dt;
			if (moveHorizontally(powerUp, powerUp.forceX * dt))
				powerUp.turnAround();
			moveVertically(powerUp, powerUp.forceY * dt, false);
			if (powerUp.getY() < -3d) {
				powerUp.deactivate();
				continue;
			}
			if (intersects(player, powerUp)) {
				powerUp.deactivate();
				player.powerUp();
				score += 1_000;
				spawnEffect(MarioEffect.Type.SPARKLE,
						player.getX() + player.getWidth() / 2d,
						player.getY() + player.getHeight() / 2d, 0.5d);
			}
		}
	}

	private void updateEnemies(final double dt) {
		final MarioWorld world = getCurrentWorld();
		for (final MarioEnemy enemy : world.getEnemies()) {
			if (!enemy.isActive())
				continue;
			if (Math.abs(enemy.getX() - player.getX()) > ENEMY_ACTIVE_DISTANCE)
				continue;

			enemy.updateAnimation(dt);
			if (enemy.collisionDown && !hasSupportAhead(enemy, enemy.getDirection()))
				enemy.turnAround();
			enemy.forceX = enemy.getDirection() * ENEMY_SPEED;
			enemy.forceY -= GRAVITY * dt;
			if (moveHorizontally(enemy, enemy.forceX * dt))
				enemy.turnAround();
			moveVertically(enemy, enemy.forceY * dt, false);
			if (enemy.getY() < -3d) {
				enemy.deactivate();
				continue;
			}

			if (intersects(player, enemy)) {
				final double enemyTop = enemy.getY() + enemy.getHeight();
				if (player.forceY <= 0 && previousPlayerY >= enemyTop - 0.12d) {
					enemy.deactivate();
					player.setPosition(player.getX(), enemyTop);
					player.forceY = STOMP_BOUNCE_SPEED;
					player.collisionDown = false;
					score += 100;
					spawnEffect(MarioEffect.Type.SPARKLE,
							enemy.getX() + enemy.getWidth() / 2d, enemyTop, 0.35d);
				} else if (!player.isInvulnerable()) {
					if (player.takeDamage()) {
						killPlayer();
						return;
					}
					player.forceY = DAMAGE_BOUNCE_SPEED;
					player.forceX = player.getX() < enemy.getX() ? -WALK_SPEED : WALK_SPEED;
				}
			}
		}
	}

	private boolean hasSupportAhead(final MarioEntity entity, final int direction) {
		final double ahead = direction > 0
				? entity.getX() + entity.getWidth() + 0.08d : entity.getX() - 0.08d;
		final int tileX = (int) Math.floor(ahead);
		final int tileY = (int) Math.floor(entity.getY() - 0.08d);
		return getCurrentWorld().isSolidAt(tileX, tileY);
	}

	private void updateCheckpoint() {
		final MarioWorld world = getCurrentWorld();
		while (checkpointIndex < world.getCheckpointCount()
				&& player.getX() >= world.getCheckpointX(checkpointIndex)) {
			checkpointX = world.getCheckpointX(checkpointIndex);
			checkpointIndex++;
		}
	}

	private void completeCourse() {
		if (state != State.PLAYING)
			return;
		state = State.COURSE_CLEAR;
		stateTimer = COURSE_CLEAR_SECONDS;
		goalSparkleTimer = 0d;
		player.stop();
		score += 500;
	}

	private void updateGoalSparkles(final double dt) {
		goalSparkleTimer -= dt;
		if (goalSparkleTimer > 0d)
			return;
		goalSparkleTimer += GOAL_SPARKLE_SECONDS;
		final MarioWorld world = getCurrentWorld();
		final double x = world.getGoalX() - 1.7d + (goalSparklePhase % 5) * 0.85d;
		final double y = world.getGoalBaseY() + 1.2d + (goalSparklePhase * 3 % 7);
		spawnEffect(MarioEffect.Type.SPARKLE, x, y, 0.42d);
		goalSparklePhase++;
	}

	private void killPlayer() {
		if (state != State.PLAYING)
			return;
		state = State.DEAD;
		stateTimer = DEATH_DELAY_SECONDS;
		deaths++;
		player.stop();
	}

	private boolean moveHorizontally(final MarioEntity entity, final double dx) {
		entity.collisionLeft = false;
		entity.collisionRight = false;
		if (dx == 0d)
			return false;

		double nextX = entity.getX() + dx;
		final int minY = (int) Math.floor(entity.getY() + COLLISION_EPSILON);
		final int maxY = (int) Math.floor(entity.getY() + entity.getHeight() - COLLISION_EPSILON);
		if (dx > 0) {
			final int tileX = (int) Math.floor(nextX + entity.getWidth() - COLLISION_EPSILON);
			for (int y = minY; y <= maxY; y++) {
				if (getCurrentWorld().isSolidAt(tileX, y)) {
					nextX = tileX - entity.getWidth();
					entity.forceX = 0;
					entity.collisionRight = true;
					break;
				}
			}
		} else {
			final int tileX = (int) Math.floor(nextX + COLLISION_EPSILON);
			for (int y = minY; y <= maxY; y++) {
				if (getCurrentWorld().isSolidAt(tileX, y)) {
					nextX = tileX + 1d;
					entity.forceX = 0;
					entity.collisionLeft = true;
					break;
				}
			}
		}
		entity.setPosition(nextX, entity.getY());
		return entity.collisionLeft || entity.collisionRight;
	}

	private boolean moveVertically(final MarioEntity entity, final double dy,
			final boolean hitBlocks) {
		entity.collisionUp = false;
		entity.collisionDown = false;
		double nextY = entity.getY() + dy;
		final int minX = (int) Math.floor(entity.getX() + COLLISION_EPSILON);
		final int maxX = (int) Math.floor(entity.getX() + entity.getWidth() - COLLISION_EPSILON);
		if (dy > 0) {
			final int tileY = (int) Math.floor(nextY + entity.getHeight() - COLLISION_EPSILON);
			boolean collision = false;
			for (int x = minX; x <= maxX; x++) {
				if (getCurrentWorld().isSolidAt(x, tileY)) {
					collision = true;
					if (hitBlocks)
						handleBlockHit(x, tileY);
				}
			}
			if (collision) {
				nextY = tileY - entity.getHeight();
				entity.forceY = 0;
				entity.collisionUp = true;
			}
		} else {
			final int tileY = (int) Math.floor(nextY + COLLISION_EPSILON);
			for (int x = minX; x <= maxX; x++) {
				if (getCurrentWorld().isSolidAt(x, tileY)) {
					nextY = tileY + 1d;
					entity.forceY = 0;
					entity.collisionDown = true;
					break;
				}
			}
		}
		entity.setPosition(entity.getX(), nextY);
		return entity.collisionUp || entity.collisionDown;
	}

	private void handleBlockHit(final int x, final int y) {
		final MarioWorld.HitResult result = getCurrentWorld().hitBlockAt(x, y,
				player.isSuperMario());
		switch (result) {
			case BUMP -> spawnEffect(MarioEffect.Type.BLOCK_BUMP, x, y, 0.2d);
			case COIN -> {
				spawnEffect(MarioEffect.Type.BLOCK_BUMP, x, y, 0.2d);
				spawnEffect(MarioEffect.Type.COIN_PICKUP, x + 0.5d, y + 1d, 0.55d);
				score += 100;
			}
			case MUSHROOM -> {
				spawnEffect(MarioEffect.Type.BLOCK_BUMP, x, y, 0.2d);
				spawnMushroom(x, y);
			}
			case BREAK -> {
				spawnEffect(MarioEffect.Type.BRICK_BREAK, x + 0.5d, y + 0.5d, 0.6d);
				score += 50;
			}
			case NONE -> { }
		}
	}

	private void spawnMushroom(final int x, final int y) {
		for (final MarioPowerUp powerUp : powerUps) {
			if (!powerUp.isActive()) {
				powerUp.spawn(x, y);
				return;
			}
		}
	}

	private void spawnEffect(final MarioEffect.Type type, final double x, final double y,
			final double duration) {
		for (int i = 0; i < effects.length; i++) {
			final int index = (nextEffectIndex + i) % effects.length;
			if (!effects[index].isActive()) {
				effects[index].start(type, x, y, duration);
				nextEffectIndex = (index + 1) % effects.length;
				return;
			}
		}
		effects[nextEffectIndex].start(type, x, y, duration);
		nextEffectIndex = (nextEffectIndex + 1) % effects.length;
	}

	private void updateEffects(final double dt) {
		for (final MarioEffect effect : effects)
			effect.update(dt);
	}

	double getBlockBumpOffset(final int x, final int y) {
		for (final MarioEffect effect : effects)
			if (effect.isActive() && effect.getType() == MarioEffect.Type.BLOCK_BUMP
					&& (int) effect.getX() == x && (int) effect.getY() == y)
				return Math.sin(effect.getProgress() * Math.PI) * 0.22d;
		return 0d;
	}

	MarioEffect[] getEffects() {
		return effects;
	}

	MarioPowerUp[] getPowerUps() {
		return powerUps;
	}

	private static boolean intersects(final MarioEntity first, final MarioEntity second) {
		return first.getX() < second.getX() + second.getWidth()
				&& first.getX() + first.getWidth() > second.getX()
				&& first.getY() < second.getY() + second.getHeight()
				&& first.getY() + first.getHeight() > second.getY();
	}

	private static double approach(final double value, final double target, final double maximumChange) {
		if (value < target)
			return Math.min(value + maximumChange, target);
		if (value > target)
			return Math.max(value - maximumChange, target);
		return target;
	}

	public PlayerEntity getPlayer() {
		return player;
	}

	public State getState() {
		return state;
	}

	public int getScore() {
		return score;
	}

	public int getDeaths() {
		return deaths;
	}

	public double getElapsedSeconds() {
		return elapsedSeconds;
	}

	public double getCheckpointX() {
		return checkpointX;
	}

	public int getLevelNumber() {
		return currentWorld + 1;
	}

	public int getLevelCount() {
		return worlds.length;
	}

	public String getLevelName() {
		return getCurrentWorld().getName();
	}

	int getLevelStorageBytes() {
		int total = 0;
		for (final MarioWorld world : worlds)
			total += world.storageBytes();
		return total;
	}

	int getActiveEffectCount() {
		int count = 0;
		for (final MarioEffect effect : effects)
			if (effect.isActive())
				count++;
		return count;
	}

	int getActivePowerUpCount() {
		int count = 0;
		for (final MarioPowerUp powerUp : powerUps)
			if (powerUp.isActive())
				count++;
		return count;
	}

	public int getProgressPercent() {
		return Math.max(0, Math.min(100,
				(int) Math.round(player.getX() / getCurrentWorld().getGoalX() * 100d)));
	}
}
