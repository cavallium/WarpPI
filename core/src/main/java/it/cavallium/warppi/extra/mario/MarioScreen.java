package it.cavallium.warppi.extra.mario;

import java.io.IOException;

import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.gui.HistoryBehavior;
import it.cavallium.warppi.gui.RenderContext;
import it.cavallium.warppi.gui.ScreenContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.gui.screens.Screen;

public final class MarioScreen extends Screen {

	static final int ATLAS_SIZE = 512;
	static final int TILE_SIZE = 16;
	static final int SMALL_MARIO_SIZE = 24;
	static final int SUPER_MARIO_SIZE = 32;
	static final int MARIO_FRAME_COUNT = 9;

	static final int BACKGROUND_WIDTH = 256;
	static final int BACKGROUND_HEIGHT = 216;
	static final int SMALL_MARIO_X = 256;
	static final int SMALL_MARIO_RIGHT_Y = 240;
	static final int SMALL_MARIO_LEFT_Y = 216;
	static final int SUPER_MARIO_X = 0;
	static final int SUPER_MARIO_RIGHT_Y = 464;
	static final int SUPER_MARIO_LEFT_Y = 432;
	static final int GOOMBA_LEFT_X = 256;
	static final int GOOMBA_RIGHT_X = 288;
	static final int GOOMBA_Y = 328;
	static final int COIN_X = 320;
	static final int COIN_Y = 328;
	static final int MUSHROOM_X = 368;
	static final int MUSHROOM_Y = 328;
	static final int SPARKLE_X = 384;
	static final int SPARKLE_Y = 328;
	static final int GOAL_TAPE_X = 416;
	static final int GOAL_TAPE_Y = 328;
	static final int TERRAIN_X = 256;
	static final int TERRAIN_Y = 344;
	static final int QUESTION_X = 400;
	static final int BRICK_X = 416;
	static final int USED_X = 432;
	static final int BLOCK_ROW_Y = 344;
	static final int PIPE_X = 256;
	static final int PIPE_TOP_Y = 360;
	static final int PIPE_BODY_Y = 376;
	static final int REFLEX_X = 304;
	static final int REFLEX_Y = 392;
	static final int GOAL_POST_X = 496;
	static final int GOAL_POST_Y = 360;
	static final int GOAL_POST_WIDTH = 16;
	static final int GOAL_POST_HEIGHT = 144;

	private static final int ENEMY_SPRITE_SIZE = 16;
	private static final int[] BACKGROUND_X = { 0, 256, 0 };
	private static final int[] BACKGROUND_Y = { 0, 0, 216 };
	private static final int SKY_COLOR = 0xFFF8E0B0;
	private static final float HELP_SECONDS = 6f;

	private MarioGame game;
	private Skin marioAtlas;
	private boolean leftPressed;
	private boolean rightPressed;
	private boolean jumpPressed;
	private boolean runPressed;
	private boolean downPressed;
	private double cameraX;
	private double cameraY;
	private boolean cameraInitialized;
	private float helpRemaining;
	private int cachedHudScore = -1;
	private int cachedHudLevel = -1;
	private int cachedHudProgress = -1;
	private String cachedHudLeft = "";
	private String cachedHudRight = "";

	public MarioScreen() {
		historyBehavior = HistoryBehavior.ALWAYS_KEEP_IN_HISTORY;
	}

	@Override
	public void created() {
		game = new MarioGame();
		helpRemaining = HELP_SECONDS;
		cachedHudScore = -1;
		cachedHudLevel = -1;
		cachedHudProgress = -1;
	}

	@Override
	public void initialized() {
		marioAtlas = loadSkin("/mario-atlas.png");
	}

	private Skin loadSkin(final String resource) {
		try {
			return d.display.getGraphicEngine().loadSkin(resource);
		} catch (final IOException | RuntimeException exception) {
			System.err.println("Mario asset disabled (" + resource + "): " + exception.getMessage());
			return null;
		}
	}

	@Override
	public void graphicInitialized(final ScreenContext ctx) {
		marioAtlas = initializeSkin(marioAtlas);
	}

	private Skin initializeSkin(final Skin candidate) {
		if (candidate == null || candidate.isInitialized())
			return candidate;
		try {
			candidate.initialize(d.display);
			return candidate;
		} catch (final RuntimeException exception) {
			System.err.println("Mario texture disabled: " + exception.getMessage());
			return null;
		}
	}

	@Override
	public boolean onKeyPressed(final KeyPressedEvent event) {
		switch (event.getKey()) {
			case OK:
			case SIMPLIFY:
			case UP:
				if (game.getState() == MarioGame.State.WON)
					restartGame();
				else
					jumpPressed = true;
				return true;
			case SHIFT:
			case STEP:
				runPressed = true;
				return true;
			case LEFT:
				leftPressed = true;
				return true;
			case RIGHT:
				rightPressed = true;
				return true;
			case DOWN:
				downPressed = true;
				return true;
			case RESET:
				restartGame();
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean onKeyReleased(final KeyReleasedEvent event) {
		switch (event.getKey()) {
			case OK:
			case SIMPLIFY:
			case UP:
				jumpPressed = false;
				return true;
			case SHIFT:
			case STEP:
				runPressed = false;
				return true;
			case LEFT:
				leftPressed = false;
				return true;
			case RIGHT:
				rightPressed = false;
				return true;
			case DOWN:
				downPressed = false;
				return true;
			default:
				return false;
		}
	}

	private void restartGame() {
		game.restart();
		leftPressed = false;
		rightPressed = false;
		jumpPressed = false;
		runPressed = false;
		downPressed = false;
		cameraInitialized = false;
		helpRemaining = HELP_SECONDS;
		cachedHudScore = -1;
		cachedHudLevel = -1;
		cachedHudProgress = -1;
	}

	@Override
	public void beforeRender(final ScreenContext ctx, final float dt) {
		game.gameTick(dt, false, downPressed, leftPressed, rightPressed, jumpPressed, runPressed);
		final float safeDt = Float.isFinite(dt) ? Math.max(0f, Math.min(dt, 0.25f)) : 0f;
		helpRemaining = Math.max(0f, helpRemaining - safeDt);
		updateCamera(ctx);
	}

	@Override
	public int getBackgroundColor() {
		return SKY_COLOR;
	}

	private void updateCamera(final ScreenContext ctx) {
		final PlayerEntity player = game.getPlayer();
		final MarioWorld world = game.getCurrentWorld();
		final double viewportWidth = ctx.getWidth() / (double) TILE_SIZE;
		final double viewportHeight = ctx.getHeight() / (double) TILE_SIZE;
		final double playerCenterX = player.getX() + player.getWidth() / 2d;
		final double playerCenterY = player.getY() + player.getHeight() / 2d;

		if (!cameraInitialized) {
			cameraX = clamp(playerCenterX - viewportWidth * 0.42d, 0d,
					Math.max(0d, world.getWidth() - viewportWidth));
			cameraY = clamp(playerCenterY - viewportHeight * 0.38d, 0d,
					Math.max(0d, world.getHeight() - viewportHeight));
			cameraInitialized = true;
		} else {
			cameraX = deadZoneCamera(cameraX, playerCenterX, viewportWidth,
					world.getWidth(), 0.32d, 0.58d);
			cameraY = deadZoneCamera(cameraY, playerCenterY, viewportHeight,
					world.getHeight(), 0.30d, 0.62d);
		}
	}

	static double deadZoneCamera(final double current, final double subject,
			final double viewport, final double worldSize,
			final double nearFraction, final double farFraction) {
		double result = current;
		if (subject < result + viewport * nearFraction)
			result = subject - viewport * nearFraction;
		else if (subject > result + viewport * farFraction)
			result = subject - viewport * farFraction;
		return clamp(result, 0d, Math.max(0d, worldSize - viewport));
	}

	private static double clamp(final double value, final double minimum, final double maximum) {
		return Math.max(minimum, Math.min(maximum, value));
	}

	@Override
	public void render(final RenderContext ctx) {
		final Renderer renderer = ctx.getRenderer();
		final MarioWorld world = game.getCurrentWorld();
		drawBackground(renderer, ctx, world);

		final int firstX = firstVisibleTile(cameraX, world.getWidth());
		final int lastX = lastVisibleTile(cameraX, ctx.getWidth(), world.getWidth());
		final int firstY = firstVisibleTile(cameraY, world.getHeight());
		final int lastY = lastVisibleTile(cameraY, ctx.getHeight(), world.getHeight());
		drawPowerUps(renderer, ctx, firstX, lastX, true);
		drawTiles(renderer, ctx, world, firstX, lastX, firstY, lastY);
		drawGoal(renderer, ctx, world);
		drawPowerUps(renderer, ctx, firstX, lastX, false);
		drawEnemies(renderer, ctx, world, firstX, lastX);
		drawEffects(renderer, ctx, firstX, lastX);
		drawPlayer(renderer, ctx);
		drawGameHud(renderer, ctx);
	}

	private void drawBackground(final Renderer renderer, final RenderContext ctx,
			final MarioWorld world) {
		renderer.glClearSkin();
		final int background = world.getBackgroundIndex();
		if (marioAtlas == null)
			return;

		marioAtlas.use(d.display);
		renderer.glColor(0xFFFFFFFF);
		final float parallax = (float) (cameraX * TILE_SIZE * 0.08d);
		float x = -(parallax % BACKGROUND_WIDTH);
		if (x > 0)
			x -= BACKGROUND_WIDTH;
		final float y = ctx.getHeight() - BACKGROUND_HEIGHT
				+ (float) (cameraY * TILE_SIZE * 0.04d);
		for (; x < ctx.getWidth(); x += BACKGROUND_WIDTH)
			renderer.glFillRect(x, y, BACKGROUND_WIDTH, BACKGROUND_HEIGHT,
					BACKGROUND_X[background], BACKGROUND_Y[background],
					BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
	}

	private void drawTiles(final Renderer renderer, final RenderContext ctx, final MarioWorld world,
			final int firstX, final int lastX, final int firstY, final int lastY) {
		for (int y = firstY; y <= lastY; y++) {
			for (int x = firstX; x <= lastX; x++) {
				final byte block = world.getBlockIdAt(x, y);
				if (block == MarioWorld.BLOCK_EMPTY)
					continue;
				if (block == MarioWorld.BLOCK_COIN) {
					drawCoin(renderer, ctx, x, y);
					continue;
				}
				if (!MarioBlock.isSolid(block))
					continue;
				final double bump = game.getBlockBumpOffset(x, y);
				final float screenX = worldToScreenX(x);
				final float screenY = worldToScreenY(ctx, y + 1d + bump);
				if (marioAtlas == null) {
					drawFallbackBlock(renderer, screenX, screenY, block);
					continue;
				}
				switch (block) {
					case MarioWorld.BLOCK_GROUND -> {
						final int terrainVisual = terrainVisual(world, x, y);
						drawTerrainTile(renderer, screenX, screenY,
								terrainVisual & 0xF, terrainVisual >>> 4);
					}
					case MarioWorld.BLOCK_BRICK -> renderer.glFillRect(screenX, screenY,
							TILE_SIZE, TILE_SIZE, BRICK_X, BLOCK_ROW_Y, TILE_SIZE, TILE_SIZE);
					case MarioWorld.BLOCK_QUESTION_COIN, MarioWorld.BLOCK_QUESTION_MUSHROOM ->
						renderer.glFillRect(screenX, screenY, TILE_SIZE, TILE_SIZE,
								QUESTION_X, BLOCK_ROW_Y, TILE_SIZE, TILE_SIZE);
					case MarioWorld.BLOCK_USED -> renderer.glFillRect(screenX, screenY,
							TILE_SIZE, TILE_SIZE, USED_X, BLOCK_ROW_Y, TILE_SIZE, TILE_SIZE);
					case MarioWorld.BLOCK_PIPE -> drawPipeTile(renderer, screenX, screenY,
							world, x, y);
					default -> { }
				}
			}
		}
	}

	private void drawCoin(final Renderer renderer, final RenderContext ctx, final int x, final int y) {
		final float screenX = worldToScreenX(x);
		final float screenY = worldToScreenY(ctx, y + 1d);
		if (marioAtlas != null) {
			final int phase = Math.floorMod((int) (game.getElapsedSeconds() * 8d) + x, 4);
			final int frame = phase == 3 ? 1 : phase;
			renderer.glFillRect(screenX, screenY, TILE_SIZE, TILE_SIZE,
					COIN_X + frame * TILE_SIZE, COIN_Y, TILE_SIZE, TILE_SIZE);
		} else {
			renderer.glColor(0xFFFFD43B);
			renderer.glFillColor(screenX + 5, screenY + 2, 6, 13);
			renderer.glColor(0xFFFFF2A0);
			renderer.glFillColor(screenX + 7, screenY + 4, 2, 9);
		}
	}

	private static void drawFallbackBlock(final Renderer renderer, final float x, final float y,
			final byte block) {
		final int color = switch (block) {
			case MarioWorld.BLOCK_BRICK -> 0xFFD68B3C;
			case MarioWorld.BLOCK_QUESTION_COIN, MarioWorld.BLOCK_QUESTION_MUSHROOM -> 0xFFFFC928;
			case MarioWorld.BLOCK_USED -> 0xFF8A7968;
			case MarioWorld.BLOCK_PIPE -> 0xFF35A853;
			default -> 0xFF985126;
		};
		renderer.glColor(color);
		renderer.glFillColor(x, y, TILE_SIZE, TILE_SIZE);
	}

	private static void drawPipeTile(final Renderer renderer, final float screenX, final float screenY,
			final MarioWorld world, final int x, final int y) {
		final boolean leftHalf = world.getBlockIdAt(x - 1, y) != MarioWorld.BLOCK_PIPE;
		final boolean top = world.getBlockIdAt(x, y + 1) != MarioWorld.BLOCK_PIPE;
		renderer.glFillRect(screenX, screenY, TILE_SIZE, TILE_SIZE,
				PIPE_X + (leftHalf ? 0 : TILE_SIZE), top ? PIPE_TOP_Y : PIPE_BODY_Y,
				TILE_SIZE, TILE_SIZE);
	}

	private static void drawTerrainTile(final Renderer renderer, final float screenX,
			final float screenY, final int variant, final int reflexMask) {
		if (reflexMask == 0) {
			renderer.glFillRect(screenX, screenY, TILE_SIZE, TILE_SIZE,
					TERRAIN_X + variant * TILE_SIZE, TERRAIN_Y, TILE_SIZE, TILE_SIZE);
			return;
		}

		drawTerrainQuadrant(renderer, screenX, screenY, variant, 0, 0,
				(reflexMask & 1) != 0, 0);
		drawTerrainQuadrant(renderer, screenX, screenY, variant, 8, 0,
				(reflexMask & 2) != 0, 1);
		drawTerrainQuadrant(renderer, screenX, screenY, variant, 0, 8,
				(reflexMask & 4) != 0, 2);
		drawTerrainQuadrant(renderer, screenX, screenY, variant, 8, 8,
				(reflexMask & 8) != 0, 3);
	}

	private static void drawTerrainQuadrant(final Renderer renderer, final float screenX,
			final float screenY, final int variant, final int offsetX, final int offsetY,
			final boolean reflex, final int reflexIndex) {
		final int atlasX = reflex
				? REFLEX_X + reflexIndex * TILE_SIZE + offsetX
				: TERRAIN_X + variant * TILE_SIZE + offsetX;
		final int atlasY = (reflex ? REFLEX_Y : TERRAIN_Y) + offsetY;
		renderer.glFillRect(screenX + offsetX, screenY + offsetY, 8, 8,
				atlasX, atlasY, 8, 8);
	}

	static int terrainVariant(final MarioWorld world, final int x, final int y) {
		return terrainVisual(world, x, y) & 0xF;
	}

	static int terrainReflexMask(final MarioWorld world, final int x, final int y) {
		return terrainVisual(world, x, y) >>> 4;
	}

	private static int terrainVisual(final MarioWorld world, final int x, final int y) {
		final boolean left = isGround(world, x - 1, y);
		final boolean right = isGround(world, x + 1, y);
		final boolean above = isGround(world, x, y + 1);
		final boolean below = isGround(world, x, y - 1);
		final int column = !left ? 0 : !right ? 2 : 1;
		final int row = !above ? 0 : !below ? 2 : 1;
		final int variant = row * 3 + column;
		int mask = 0;
		if (above && left && !isGround(world, x - 1, y + 1))
			mask |= 1;
		if (above && right && !isGround(world, x + 1, y + 1))
			mask |= 2;
		if (below && left && !isGround(world, x - 1, y - 1))
			mask |= 4;
		if (below && right && !isGround(world, x + 1, y - 1))
			mask |= 8;
		return variant | (mask << 4);
	}

	private static boolean isGround(final MarioWorld world, final int x, final int y) {
		return world.getBlockIdAt(x, y) == MarioWorld.BLOCK_GROUND;
	}

	private void drawGoal(final Renderer renderer, final RenderContext ctx, final MarioWorld world) {
		final float left = worldToScreenX(world.getGoalX() - 1.75d);
		final float right = worldToScreenX(world.getGoalX() + 1.75d);
		if (right < -GOAL_POST_WIDTH || left > ctx.getWidth() + GOAL_POST_WIDTH)
			return;
		final float top = worldToScreenY(ctx, world.getGoalBaseY() + 9d);
		final float bottom = worldToScreenY(ctx, world.getGoalBaseY());
		if (marioAtlas != null) {
			renderer.glFillRect(left, top, GOAL_POST_WIDTH, GOAL_POST_HEIGHT,
					GOAL_POST_X, GOAL_POST_Y, GOAL_POST_WIDTH, GOAL_POST_HEIGHT);
			renderer.glFillRect(right, top, GOAL_POST_WIDTH, GOAL_POST_HEIGHT,
					GOAL_POST_X, GOAL_POST_Y, GOAL_POST_WIDTH, GOAL_POST_HEIGHT);
			final double oscillation = Math.sin(game.getElapsedSeconds() * 2.4d) * 0.5d + 0.5d;
			final float tapeY = top + 20f + (float) oscillation
					* Math.max(0f, bottom - top - 40f);
			drawGoalTape(renderer, left + 8f, right + 8f, tapeY);
		} else {
			renderer.glColor(0xFFF4F4F4);
			renderer.glFillColor(left + 6, top, 4, bottom - top);
			renderer.glFillColor(right + 6, top, 4, bottom - top);
			renderer.glColor(0xFF4DA7E8);
			renderer.glFillColor(left + 8, top + 28, right - left, 6);
		}
	}

	private static void drawGoalTape(final Renderer renderer, final float start,
			final float end, final float y) {
		if (end - start < 16f)
			return;
		renderer.glFillRect(start, y, 8, 8, GOAL_TAPE_X, GOAL_TAPE_Y, 8, 8);
		for (float x = start + 8f; x < end - 8f; x += 8f) {
			final float width = Math.min(8f, end - 8f - x);
			renderer.glFillRect(x, y, width, 8,
					GOAL_TAPE_X + 8, GOAL_TAPE_Y, width, 8);
		}
		renderer.glFillRect(end - 8f, y, 8, 8,
				GOAL_TAPE_X + 16, GOAL_TAPE_Y, 8, 8);
	}

	private void drawPowerUps(final Renderer renderer, final RenderContext ctx,
			final int firstX, final int lastX, final boolean emerging) {
		for (final MarioPowerUp powerUp : game.getPowerUps()) {
			if (!powerUp.isActive() || powerUp.getX() + 1d < firstX || powerUp.getX() > lastX + 1d)
				continue;
			if (powerUp.isEmerging() != emerging)
				continue;
			final float bodyWidth = (float) (powerUp.getWidth() * TILE_SIZE);
			final float x = worldToScreenX(powerUp.getX())
					- (TILE_SIZE - bodyWidth) / 2f;
			final float y = worldToScreenY(ctx, powerUp.getY()) - TILE_SIZE;
			if (marioAtlas != null)
				renderer.glFillRect(x, y, TILE_SIZE, TILE_SIZE,
						MUSHROOM_X, MUSHROOM_Y, TILE_SIZE, TILE_SIZE);
			else {
				renderer.glColor(0xFFE84136);
				renderer.glFillColor(x, y + 2, TILE_SIZE, TILE_SIZE - 2);
			}
		}
	}

	private void drawEnemies(final Renderer renderer, final RenderContext ctx, final MarioWorld world,
			final int firstX, final int lastX) {
		for (final MarioEnemy enemy : world.getEnemies()) {
			if (!enemy.isActive() || enemy.getX() + enemy.getWidth() < firstX
					|| enemy.getX() > lastX + 1)
				continue;
			final float bodyWidth = (float) (enemy.getWidth() * TILE_SIZE);
			final float x = worldToScreenX(enemy.getX()) - (ENEMY_SPRITE_SIZE - bodyWidth) / 2f;
			final float y = worldToScreenY(ctx, enemy.getY()) - ENEMY_SPRITE_SIZE;
			if (marioAtlas != null) {
				final int directionX = enemy.getDirection() < 0 ? GOOMBA_LEFT_X : GOOMBA_RIGHT_X;
				renderer.glFillRect(x, y, ENEMY_SPRITE_SIZE, ENEMY_SPRITE_SIZE,
						directionX + enemy.getSpriteFrame() * ENEMY_SPRITE_SIZE,
						GOOMBA_Y, ENEMY_SPRITE_SIZE, ENEMY_SPRITE_SIZE);
			} else {
				renderer.glColor(0xFF9B5528);
				renderer.glFillColor(x, y + 2, ENEMY_SPRITE_SIZE, ENEMY_SPRITE_SIZE - 2);
			}
		}
	}

	private void drawEffects(final Renderer renderer, final RenderContext ctx,
			final int firstX, final int lastX) {
		for (final MarioEffect effect : game.getEffects()) {
			if (!effect.isActive() || effect.getX() < firstX - 2d || effect.getX() > lastX + 2d)
				continue;
			switch (effect.getType()) {
				case COIN_PICKUP -> drawCoinEffect(renderer, ctx, effect);
				case BRICK_BREAK -> drawBrickDebris(renderer, ctx, effect);
				case SPARKLE -> drawSparkle(renderer, ctx, effect);
				case BLOCK_BUMP -> { }
			}
		}
	}

	private void drawCoinEffect(final Renderer renderer, final RenderContext ctx,
			final MarioEffect effect) {
		final double progress = effect.getProgress();
		final double y = effect.getY() + Math.sin(progress * Math.PI) * 1.25d + progress * 0.35d;
		if (progress > 0.7d) {
			drawSparkleAt(renderer, ctx, effect.getX(), y,
					(int) (effect.getElapsed() * 18d) & 3);
			return;
		}
		final float x = worldToScreenX(effect.getX()) - TILE_SIZE / 2f;
		final float screenY = worldToScreenY(ctx, y) - TILE_SIZE / 2f;
		if (marioAtlas != null) {
			final int frame = (int) (effect.getElapsed() * 12d) % 3;
			renderer.glFillRect(x, screenY, TILE_SIZE, TILE_SIZE,
					COIN_X + frame * TILE_SIZE, COIN_Y, TILE_SIZE, TILE_SIZE);
		} else {
			renderer.glColor(0xFFFFD43B);
			renderer.glFillColor(x + 5, screenY + 2, 6, 13);
		}
	}

	private void drawBrickDebris(final Renderer renderer, final RenderContext ctx,
			final MarioEffect effect) {
		final double progress = effect.getProgress();
		for (int piece = 0; piece < 4; piece++) {
			final int column = piece & 1;
			final int row = piece >> 1;
			final double direction = column == 0 ? -1d : 1d;
			final double x = effect.getX() + direction * (0.2d + progress * (0.65d + row * 0.2d));
			final double y = effect.getY() + (row == 0 ? 0.35d : -0.05d)
					+ progress * (2.1d - row * 0.35d) - progress * progress * 2.8d;
			final float screenX = worldToScreenX(x) - 4f;
			final float screenY = worldToScreenY(ctx, y) - 4f;
			if (marioAtlas != null)
				renderer.glFillRect(screenX, screenY, 8, 8,
						BRICK_X + column * 8, BLOCK_ROW_Y + row * 8, 8, 8);
			else {
				renderer.glColor(0xFFD68B3C);
				renderer.glFillColor(screenX, screenY, 8, 8);
			}
		}
	}

	private void drawSparkle(final Renderer renderer, final RenderContext ctx,
			final MarioEffect effect) {
		drawSparkleAt(renderer, ctx, effect.getX(), effect.getY(),
				Math.floorMod((int) (effect.getElapsed() * 18d), 3));
	}

	private void drawSparkleAt(final Renderer renderer, final RenderContext ctx,
			final double worldX, final double worldY, final int frame) {
		final float x = worldToScreenX(worldX) - 6f;
		final float y = worldToScreenY(ctx, worldY) - 6f;
		if (marioAtlas != null)
			renderer.glFillRect(x, y, 12, 12,
					SPARKLE_X + Math.floorMod(frame, 3) * 8, SPARKLE_Y, 8, 8);
		else {
			renderer.glColor(0xFFFFFF80);
			renderer.glFillColor(x + 4, y, 4, 12);
			renderer.glFillColor(x, y + 4, 12, 4);
		}
	}

	private void drawPlayer(final Renderer renderer, final RenderContext ctx) {
		final PlayerEntity player = game.getPlayer();
		if (!player.isVisibleDuringInvulnerability())
			return;
		final int spriteSize = player.isSuperMario() ? SUPER_MARIO_SIZE : SMALL_MARIO_SIZE;
		final int atlasX = player.isSuperMario() ? SUPER_MARIO_X : SMALL_MARIO_X;
		final int atlasY;
		if (player.isSuperMario())
			atlasY = player.isFacingLeft() ? SUPER_MARIO_LEFT_Y : SUPER_MARIO_RIGHT_Y;
		else
			atlasY = player.isFacingLeft() ? SMALL_MARIO_LEFT_Y : SMALL_MARIO_RIGHT_Y;
		final float bodyWidth = (float) (player.getWidth() * TILE_SIZE);
		final float screenX = worldToScreenX(player.getX()) - (spriteSize - bodyWidth) / 2f;
		final float screenY = worldToScreenY(ctx, player.getY()) - spriteSize;
		if (marioAtlas != null)
			renderer.glFillRect(screenX, screenY, spriteSize, spriteSize,
					atlasX + spriteSize * player.getSpriteFrame(), atlasY, spriteSize, spriteSize);
		else {
			renderer.glColor(0xFFD9342B);
			renderer.glFillColor(worldToScreenX(player.getX()),
					worldToScreenY(ctx, player.getY() + player.getHeight()), bodyWidth,
					(float) (player.getHeight() * TILE_SIZE));
		}
	}

	private void drawGameHud(final Renderer renderer, final RenderContext ctx) {
		final DisplayOutputDevice display = d.display;
		if (d.fonts == null || d.fonts.length == 0 || d.fonts[0] == null)
			return;
		final boolean showHelp = helpRemaining > 0f && game.getState() == MarioGame.State.PLAYING;
		final String centeredMessage = switch (game.getState()) {
			case DEAD -> "TRY AGAIN";
			case COURSE_CLEAR -> "COURSE CLEAR!";
			case WON -> "ALL COURSES CLEAR!  OK TO REPLAY";
			case PLAYING -> null;
		};

		renderer.glClearSkin();
		renderer.glColor4i(12, 25, 38, 200);
		renderer.glFillColor(5, 5, ctx.getWidth() - 10, 19);
		if (showHelp) {
			renderer.glColor4i(12, 25, 38, 185);
			renderer.glFillColor(28, 29, ctx.getWidth() - 56, 18);
		}
		if (centeredMessage != null) {
			renderer.glColor4i(10, 20, 30, 215);
			renderer.glFillColor(55, ctx.getHeight() / 2f - 18, ctx.getWidth() - 110, 36);
		}

		d.fonts[0].use(display);
		renderer.glColor(0xFFFFFFFF);
		updateHudText();
		renderer.glDrawStringLeft(9, 10, cachedHudLeft);
		renderer.glDrawStringRight(ctx.getWidth() - 9, 10, cachedHudRight);

		if (showHelp)
			renderer.glDrawStringCenter(ctx.getWidth() / 2f, 34,
					"LEFT/RIGHT MOVE  SHIFT/STEP RUN  DOWN DUCK  UP/OK JUMP");
		if (centeredMessage != null)
			renderer.glDrawStringCenter(ctx.getWidth() / 2f, ctx.getHeight() / 2f - 4,
					centeredMessage);
	}

	private void updateHudText() {
		final int score = game.getScore();
		final int level = game.getLevelNumber();
		final int progress = game.getProgressPercent();
		if (score != cachedHudScore || level != cachedHudLevel) {
			cachedHudScore = score;
			cachedHudLevel = level;
			cachedHudLeft = "SCORE " + score + "  " + level + " OF "
					+ game.getLevelCount() + "  " + game.getLevelName();
		}
		if (progress != cachedHudProgress) {
			cachedHudProgress = progress;
			cachedHudRight = progress + "%";
		}
	}

	private float worldToScreenX(final double worldX) {
		return (float) ((worldX - cameraX) * TILE_SIZE);
	}

	private float worldToScreenY(final RenderContext ctx, final double worldY) {
		return (float) (ctx.getHeight() - (worldY - cameraY) * TILE_SIZE);
	}

	static int firstVisibleTile(final double camera, final int worldSize) {
		return Math.max(0, Math.min(worldSize - 1, (int) Math.floor(camera)));
	}

	static int lastVisibleTile(final double camera, final int viewportPixels, final int worldSize) {
		return Math.max(0, Math.min(worldSize - 1,
				(int) Math.ceil(camera + viewportPixels / (double) TILE_SIZE) - 1));
	}

	MarioGame getGame() {
		return game;
	}

	@Override
	public boolean mustBeRefreshed() {
		return true;
	}

	@Override
	public String getSessionTitle() {
		return "Mario Adventure";
	}
}
