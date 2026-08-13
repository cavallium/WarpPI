package it.cavallium.warppi.extra.mario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class MarioGameTest {

	@Test
	void movementIsStableAcrossDifferentRenderIntervals() {
		final MarioGame twentyFps = new MarioGame();
		final MarioGame hundredFps = new MarioGame();
		deactivateEnemies(twentyFps);
		deactivateEnemies(hundredFps);

		advance(twentyFps, 20, 0.05f, false, false, true, false);
		advance(hundredFps, 100, 0.01f, false, false, true, false);

		assertTrue(twentyFps.getPlayer().getX() > 5d);
		assertEquals(twentyFps.getPlayer().getX(), hundredFps.getPlayer().getX(), 0.03d);
		assertEquals(twentyFps.getPlayer().getVelocityX(),
				hundredFps.getPlayer().getVelocityX(), 0.03d);
	}

	@Test
	void directionUsesExplicitRightAndLeftFramesWithoutTextureFlipping() {
		final MarioGame game = new MarioGame();
		deactivateEnemies(game);

		advance(game, 4, 0.05f, false, false, true, false);
		assertFalse(game.getPlayer().isFacingLeft());
		advance(game, 8, 0.05f, false, true, false, false);
		assertTrue(game.getPlayer().isFacingLeft());
	}

	@Test
	void runningUsesTheCompleteThreeFrameSmwCycle() {
		final PlayerEntity player = new PlayerEntity(0d, 0d, 1);
		player.forceX = MarioGame.RUN_SPEED;
		player.setOnGround(true);
		final Set<Integer> observedFrames = new HashSet<>();

		for (int frame = 0; frame < 3; frame++) {
			player.updateAnimation(0.086d, 1, true);
			observedFrames.add(player.getSpriteFrame());
		}

		assertEquals(Set.of(4, 5, 8), observedFrames);
	}

	@Test
	void galoombaAnimationIsAStableTwoFrameWalkCycle() {
		final MarioEnemy enemy = new MarioEnemy(4d, 2d);
		assertEquals(0, enemy.getSpriteFrame());
		enemy.updateAnimation(0.15d);
		assertEquals(1, enemy.getSpriteFrame());
		enemy.updateAnimation(0.15d);
		assertEquals(0, enemy.getSpriteFrame());
		enemy.turnAround();
		assertEquals(1, enemy.getDirection());
		assertEquals(0, enemy.getSpriteFrame());
	}

	@Test
	void aShortJumpTapIsBufferedUntilTheNextFixedStep() {
		final MarioGame game = new MarioGame();
		deactivateEnemies(game);

		game.gameTick(0.005f, true, false, false, false, true, false);
		game.gameTick(0.005f, false, false, false, false, false, false);
		game.gameTick(0.01f, false, false, false, false, false, false);

		assertTrue(game.getPlayer().getVelocityY() > 0d);
		assertTrue(game.getPlayer().getY() > 2d);
	}

	@Test
	void heldJumpIsHigherThanTappedJumpAndBothLandCleanly() {
		final MarioGame tapped = new MarioGame();
		final MarioGame held = new MarioGame();
		deactivateEnemies(tapped);
		deactivateEnemies(held);

		final double tappedPeak = jumpAndMeasurePeak(tapped, 1);
		final double heldPeak = jumpAndMeasurePeak(held, 7);

		assertTrue(heldPeak > tappedPeak + 0.7d,
				() -> "held=" + heldPeak + ", tapped=" + tappedPeak);
		assertTrue(tappedPeak >= 2.75d && tappedPeak <= 3.35d,
				() -> "tapped peak=" + tappedPeak);
		assertTrue(heldPeak >= 4.7d && heldPeak <= 5.5d,
				() -> "held peak=" + heldPeak);
		assertEquals(2d, tapped.getPlayer().getY(), 0.001d);
		assertEquals(2d, held.getPlayer().getY(), 0.001d);
		assertTrue(tapped.getPlayer().isOnGround());
		assertTrue(held.getPlayer().isOnGround());
	}

	@Test
	void walkRunAndDuckAreThreeDistinctInputs() {
		final MarioGame walking = new MarioGame(flatWorld());
		final MarioGame running = new MarioGame(flatWorld());
		final MarioGame pressingDown = new MarioGame(flatWorld());

		advance(walking, 40, 0.05f, false, false, true, false);
		advance(running, 40, 0.05f, false, false, true, true);
		for (int tick = 0; tick < 40; tick++)
			pressingDown.gameTick(0.05f, false, true, false, true, false, false);

		assertEquals(MarioGame.WALK_SPEED, walking.getPlayer().getVelocityX(), 0.02d);
		assertEquals(MarioGame.RUN_SPEED, running.getPlayer().getVelocityX(), 0.02d);
		assertEquals(MarioGame.WALK_SPEED, pressingDown.getPlayer().getVelocityX(), 0.02d);
		assertTrue(running.getPlayer().getX() > walking.getPlayer().getX() + 3d);
		assertFalse(pressingDown.getPlayer().isRunning());
	}

	@Test
	void marioCanJumpTheFirstStepAndFitThroughTheOneTilePassage() {
		final MarioGame platformGame = new MarioGame();
		deactivateEnemies(platformGame);
		platformGame.getPlayer().setPosition(7d, 2d, true);
		for (int tick = 0; tick < 30; tick++)
			platformGame.gameTick(0.05f, false, false, false, true, tick < 9, true);
		assertTrue(platformGame.getPlayer().getX() > 14d,
				() -> "x=" + platformGame.getPlayer().getX()
						+ ", y=" + platformGame.getPlayer().getY());

		final MarioGame passageGame = new MarioGame();
		deactivateEnemies(passageGame);
		passageGame.getPlayer().setPosition(56d, 2d, true);
		advance(passageGame, 32, 0.05f, false, false, true, true);
		assertTrue(passageGame.getPlayer().getX() > 64d,
				() -> "x=" + passageGame.getPlayer().getX()
						+ ", y=" + passageGame.getPlayer().getY());

		final MarioGame superPassageGame = new MarioGame();
		deactivateEnemies(superPassageGame);
		superPassageGame.getPlayer().powerUp();
		superPassageGame.getPlayer().setPosition(56d, 2d, true);
		for (int tick = 0; tick < 90; tick++)
			superPassageGame.gameTick(0.05f, false, true,
					false, true, false, true);
		assertTrue(superPassageGame.getPlayer().getX() > 64d,
				() -> "super x=" + superPassageGame.getPlayer().getX());
		assertTrue(superPassageGame.getPlayer().isDucking());
		assertEquals(1d, superPassageGame.getPlayer().getHeight(), 0.001d);
	}

	@Test
	void coinPickupRemovesTheCoinAndStartsAReusedAnimation() {
		final MarioGame game = new MarioGame();
		deactivateEnemies(game);
		game.getPlayer().setPosition(4d, 3d, false);

		game.gameTick(0.05f, false, false, false, false, false, false);

		assertEquals(10, game.getScore());
		assertEquals(MarioWorld.BLOCK_EMPTY, game.getCurrentWorld().getBlockIdAt(4, 3));
		assertTrue(game.getActiveEffectCount() > 0);
	}

	@Test
	void questionBlocksProduceCoinsAndMushroomsThenBecomeUsed() {
		final MarioGame coinGame = new MarioGame();
		deactivateEnemies(coinGame);
		hitBlockFromBelow(coinGame, 19, 5);
		assertEquals(MarioWorld.BLOCK_USED, coinGame.getCurrentWorld().getBlockIdAt(19, 5));
		assertTrue(coinGame.getScore() >= 100);
		assertTrue(coinGame.getActiveEffectCount() > 0);

		final MarioGame mushroomGame = new MarioGame();
		deactivateEnemies(mushroomGame);
		hitBlockFromBelow(mushroomGame, 40, 6);
		assertEquals(MarioWorld.BLOCK_USED, mushroomGame.getCurrentWorld().getBlockIdAt(40, 6));
		assertEquals(1, mushroomGame.getActivePowerUpCount());
		advance(mushroomGame, 14, 0.05f, false, false, false, false);
		final MarioPowerUp mushroom = firstActivePowerUp(mushroomGame);
		mushroomGame.getPlayer().setPosition(mushroom.getX(), mushroom.getY(), false);
		mushroomGame.gameTick(0.05f, false, false, false, false, false, false);
		assertTrue(mushroomGame.getPlayer().isSuperMario());
		assertEquals(0, mushroomGame.getActivePowerUpCount());
		assertTrue(mushroomGame.getScore() >= 1_000);
	}

	@Test
	void onlySuperMarioBreaksBricks() {
		final MarioGame smallGame = new MarioGame();
		deactivateEnemies(smallGame);
		hitBlockFromBelow(smallGame, 17, 5);
		assertEquals(MarioWorld.BLOCK_BRICK, smallGame.getCurrentWorld().getBlockIdAt(17, 5));

		final MarioGame superGame = new MarioGame();
		deactivateEnemies(superGame);
		superGame.getPlayer().powerUp();
		hitBlockFromBelow(superGame, 17, 5);
		assertEquals(MarioWorld.BLOCK_EMPTY, superGame.getCurrentWorld().getBlockIdAt(17, 5));
		assertTrue(superGame.getActiveEffectCount() > 0);
	}

	@Test
	void checkpointsDeathAndRespawnFormAPlayableLoop() {
		final MarioGame game = new MarioGame();
		deactivateEnemies(game);
		game.getPlayer().setPosition(37d, 2d, true);
		game.gameTick(0.05f, false, false, false, false, false, false);
		assertEquals(36d, game.getCheckpointX());

		game.getPlayer().setPosition(37d, -5d, false);
		game.gameTick(0.05f, false, false, false, false, false, false);
		assertEquals(MarioGame.State.DEAD, game.getState());
		assertEquals(1, game.getDeaths());

		advance(game, 20, 0.05f, false, false, false, false);
		assertEquals(MarioGame.State.PLAYING, game.getState());
		assertEquals(36d, game.getPlayer().getX(), 0.001d);
		assertTrue(game.getPlayer().isOnGround());
	}

	@Test
	void allThreeCoursesAdvanceThroughTheGoalGate() {
		final MarioGame game = new MarioGame();
		for (int expectedLevel = 1; expectedLevel <= 3; expectedLevel++) {
			assertEquals(expectedLevel, game.getLevelNumber());
			game.getPlayer().setPosition(game.getCurrentWorld().getGoalX(), 2d, true);
			game.gameTick(0.05f, false, false, false, false, false, false);
			assertEquals(MarioGame.State.COURSE_CLEAR, game.getState());
			advance(game, 30, 0.05f, false, false, false, false);
		}
		assertEquals(MarioGame.State.WON, game.getState());
		assertEquals(3, game.getLevelNumber());
		assertEquals(1_500, game.getScore());
	}

	@Test
	void authoredCampaignCanBeCompletedWithOrdinaryRunAndJumpInputs() {
		final MarioGame game = new MarioGame();
		int jumpTicks = 0;
		int releaseTicks = 0;
		int observedDeaths = 0;
		final StringBuilder deathLocations = new StringBuilder();
		for (int tick = 0; tick < 12_000 && game.getState() != MarioGame.State.WON; tick++) {
			deactivateEnemies(game);
			final PlayerEntity player = game.getPlayer();
			if (game.getState() == MarioGame.State.PLAYING && jumpTicks == 0
					&& releaseTicks == 0 && player.isOnGround() && hasHazardAhead(game, player))
				jumpTicks = 14;
			final boolean jump = jumpTicks > 0;
			if (jumpTicks > 0) {
				jumpTicks--;
				if (jumpTicks == 0)
					releaseTicks = 3;
			} else if (releaseTicks > 0) {
				releaseTicks--;
			}
			game.gameTick(0.025f, false, false, false, true, jump, true);
			if (game.getDeaths() > observedDeaths) {
				observedDeaths = game.getDeaths();
				deathLocations.append("level ").append(game.getLevelNumber())
						.append(" at x=").append(game.getPlayer().getX()).append(';');
			}
		}

		assertEquals(MarioGame.State.WON, game.getState(),
				() -> "level=" + game.getLevelNumber() + ", x=" + game.getPlayer().getX()
						+ ", y=" + game.getPlayer().getY() + ", deaths=" + game.getDeaths()
						+ ", locations=" + deathLocations);
		assertEquals(0, game.getDeaths(), deathLocations.toString());
	}

	@Test
	void runtimeGameplayStorageIsBoundedAndPreallocated() {
		final MarioGame game = new MarioGame();
		assertEquals(24, game.getEffects().length);
		assertEquals(4, game.getPowerUps().length);
		assertTrue(game.getLevelStorageBytes() < 16 * 1_024,
				() -> "level bytes=" + game.getLevelStorageBytes());
	}

	private static void hitBlockFromBelow(final MarioGame game, final int blockX, final int blockY) {
		game.getPlayer().setPosition(blockX + 0.1d, 2d, true);
		for (int tick = 0; tick < 24; tick++) {
			game.gameTick(0.025f, false, false, false, false, tick < 14, false);
			if (game.getCurrentWorld().getBlockIdAt(blockX, blockY) == MarioWorld.BLOCK_USED
					|| game.getCurrentWorld().getBlockIdAt(blockX, blockY) == MarioWorld.BLOCK_EMPTY)
				return;
		}
	}

	private static MarioPowerUp firstActivePowerUp(final MarioGame game) {
		for (final MarioPowerUp powerUp : game.getPowerUps())
			if (powerUp.isActive())
				return powerUp;
		throw new AssertionError("No active mushroom");
	}

	private static void deactivateEnemies(final MarioGame game) {
		for (final MarioEnemy enemy : game.getCurrentWorld().getEnemies())
			enemy.deactivate();
	}

	private static MarioWorld flatWorld() {
		final int width = 64;
		final int height = 10;
		final byte[] blocks = new byte[width * height];
		for (int y = 0; y <= 1; y++)
			for (int x = 0; x < width; x++)
				blocks[x + y * width] = MarioWorld.BLOCK_GROUND;
		return new MarioWorld(2, 2, width, height, blocks, new MarioEnemy[0], 62d);
	}

	private static boolean hasHazardAhead(final MarioGame game, final PlayerEntity player) {
		final MarioWorld world = game.getCurrentWorld();
		final int floorY = (int) Math.floor(player.getY() - 0.1d);
		final int lookAheadX = (int) Math.floor(player.getX() + player.getWidth() + 2.4d);
		if (!world.isSolidAt(lookAheadX, floorY))
			return true;
		final int wallX = (int) Math.floor(player.getX() + player.getWidth() + 0.35d);
		final int bottomY = (int) Math.floor(player.getY() + 0.15d);
		final int topY = (int) Math.floor(player.getY() + player.getHeight() - 0.1d);
		for (int y = bottomY; y <= topY; y++)
			if (world.isSolidAt(wallX, y))
				return true;
		return false;
	}

	private static double jumpAndMeasurePeak(final MarioGame game, final int heldTicks) {
		double peak = game.getPlayer().getY();
		for (int tick = 0; tick < 50; tick++) {
			final boolean jump = tick < heldTicks;
			game.gameTick(0.05f, jump, false, false, false, jump, false);
			peak = Math.max(peak, game.getPlayer().getY());
		}
		return peak;
	}

	private static void advance(final MarioGame game, final int ticks, final float dt,
			final boolean jump, final boolean left, final boolean right, final boolean run) {
		for (int tick = 0; tick < ticks; tick++)
			game.gameTick(dt, jump, false, left, right, jump, run);
	}
}
