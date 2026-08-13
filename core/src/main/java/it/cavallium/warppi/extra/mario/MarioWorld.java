package it.cavallium.warppi.extra.mario;

import java.util.Arrays;

public final class MarioWorld {

	public enum HitResult {
		NONE,
		BUMP,
		COIN,
		MUSHROOM,
		BREAK
	}

	public static final byte BLOCK_EMPTY = 0;
	public static final byte BLOCK_GROUND = 1;
	public static final byte BLOCK_BRICK = 2;
	public static final byte BLOCK_COIN = 3;
	public static final byte BLOCK_QUESTION_COIN = 4;
	public static final byte BLOCK_QUESTION_MUSHROOM = 5;
	public static final byte BLOCK_USED = 6;
	public static final byte BLOCK_PIPE = 7;

	private static final int LEVEL_HEIGHT = 18;

	private final String name;
	private final int backgroundIndex;
	private final int spawnPointX;
	private final int spawnPointY;
	private final int width;
	private final int height;
	private final byte[] initialData;
	private final byte[] data;
	private final MarioEvent[] events;
	private final MarioEnemy[] enemies;
	private final double[] checkpointXs;
	private final double goalX;
	private final int goalBaseY;

	/**
	 * Compatibility constructor for the original top-to-bottom two-dimensional
	 * level representation.
	 */
	public MarioWorld(final int[] spawnPoint, final int width, final int height, final byte[][] data,
			final MarioEvent[] events, final MarioEntity[] entities) {
		this("Custom Course", 0, spawnPoint[0], spawnPoint[1], width, height,
				flattenLegacy(width, height, data), events, copyEnemies(entities),
				new double[0], Math.max(spawnPoint[0] + 1, width - 4), 2);
	}

	MarioWorld(final int spawnPointX, final int spawnPointY, final int width, final int height,
			final byte[] data, final MarioEnemy[] enemies, final double goalX) {
		this("Test Course", 0, spawnPointX, spawnPointY, width, height, data,
				new MarioEvent[0], enemies, new double[0], goalX, 2);
	}

	private MarioWorld(final String name, final int backgroundIndex,
			final int spawnPointX, final int spawnPointY, final int width, final int height,
			final byte[] sourceData, final MarioEvent[] events, final MarioEnemy[] enemies,
			final double[] checkpointXs, final double goalX, final int goalBaseY) {
		if (width <= 0 || height <= 0 || sourceData.length != width * height)
			throw new IllegalArgumentException("Invalid Mario world dimensions");
		this.name = name;
		this.backgroundIndex = Math.floorMod(backgroundIndex, 3);
		this.spawnPointX = spawnPointX;
		this.spawnPointY = spawnPointY;
		this.width = width;
		this.height = height;
		initialData = sourceData.clone();
		data = sourceData.clone();
		this.events = events == null ? new MarioEvent[0] : events.clone();
		this.enemies = enemies == null ? new MarioEnemy[0] : enemies.clone();
		this.checkpointXs = checkpointXs == null ? new double[0] : checkpointXs.clone();
		this.goalX = goalX;
		this.goalBaseY = goalBaseY;
	}

	static MarioWorld createDefault() {
		return createLevels()[0];
	}

	static MarioWorld[] createLevels() {
		return new MarioWorld[] { createYoshiMeadow(), createDonutHeights(), createForestTrail() };
	}

	private static MarioWorld createYoshiMeadow() {
		final Builder level = new Builder("YOSHI MEADOW", 0, 104);
		level.ground(0, 103)
				.pit(28, 30).pit(53, 55).pit(78, 80)
				.groundRect(10, 13, 2, 2)
				.groundRect(32, 35, 2, 3)
				.groundRect(82, 86, 2, 3)
				.bricks(17, 21, 5).question(19, 5, false)
				.bricks(39, 44, 6).question(40, 6, true).question(43, 6, false)
				.pipe(47, 1)
				.bricks(57, 64, 3)
				.question(61, 6, false)
				.pipe(69, 2)
				.bricks(88, 93, 5).question(90, 5, true)
				.coins(4, 8, 3, 2).coins(10, 13, 4, 1)
				.coins(22, 27, 4, 2).coins(32, 35, 5, 1)
				.coins(57, 64, 5, 2).coins(71, 76, 4, 2)
				.coins(84, 88, 6, 2).coins(95, 99, 3, 2);
		return level.build(new double[] { 36d, 72d }, 99d,
				new MarioEnemy[] {
						new MarioEnemy(23, 2), new MarioEnemy(44, 2),
						new MarioEnemy(65, 2), new MarioEnemy(87, 4)
				});
	}

	private static MarioWorld createDonutHeights() {
		final Builder level = new Builder("DONUT HEIGHTS", 2, 112);
		level.ground(0, 111)
				.pit(24, 26).pit(50, 53).pit(83, 86)
				.groundRect(8, 11, 2, 3)
				.groundRect(28, 32, 2, 4)
				.groundRect(55, 59, 2, 3)
				.groundRect(88, 92, 2, 4)
				.bricks(14, 20, 6).question(16, 6, false).question(19, 6, true)
				.pipe(36, 2)
				.bricks(42, 47, 6).question(45, 6, false)
				.pipe(64, 1)
				.bricks(69, 76, 6).question(71, 6, true).question(74, 6, false)
				.pipe(96, 2)
				.coins(3, 7, 3, 2).coins(9, 12, 5, 1)
				.coins(24, 27, 5, 1).coins(29, 32, 6, 1)
				.coins(51, 54, 5, 1).coins(56, 60, 5, 1)
				.coins(78, 87, 5, 2).coins(89, 93, 6, 1)
				.coins(101, 107, 3, 2);
		return level.build(new double[] { 39d, 78d }, 107d,
				new MarioEnemy[] {
						new MarioEnemy(13, 2), new MarioEnemy(33, 2),
						new MarioEnemy(61, 2), new MarioEnemy(77, 2),
						new MarioEnemy(94, 2)
				});
	}

	private static MarioWorld createForestTrail() {
		final Builder level = new Builder("FOREST TRAIL", 1, 120);
		level.ground(0, 119)
				.pit(31, 34).pit(62, 64).pit(94, 96)
				.groundRect(12, 16, 2, 3)
				.groundRect(36, 40, 2, 4)
				.groundRect(69, 74, 2, 3)
				.groundRect(101, 106, 2, 4)
				.bricks(5, 10, 5).question(7, 5, true).question(9, 5, false)
				.pipe(21, 2)
				.bricks(43, 50, 6).question(45, 6, false).question(48, 6, true)
				.pipe(52, 1)
				.bricks(75, 82, 5).question(77, 5, false).question(80, 5, true)
				.pipe(84, 2)
				.bricks(106, 112, 6).question(109, 6, false)
				.coins(2, 5, 3, 1).coins(12, 17, 5, 2)
				.coins(26, 35, 5, 2).coins(37, 41, 6, 1)
				.coins(57, 66, 5, 2).coins(68, 73, 5, 1)
				.coins(89, 98, 5, 2).coins(100, 105, 6, 1)
				.coins(113, 116, 3, 1);
		return level.build(new double[] { 42d, 84d }, 115d,
				new MarioEnemy[] {
						new MarioEnemy(18, 2), new MarioEnemy(27, 2),
						new MarioEnemy(51, 2), new MarioEnemy(73, 2),
						new MarioEnemy(90, 2), new MarioEnemy(106, 2)
				});
	}

	private static byte[] flattenLegacy(final int width, final int height, final byte[][] legacy) {
		final byte[] result = new byte[width * height];
		if (legacy == null)
			return result;
		for (int y = 0; y < height; y++) {
			final int sourceY = height - 1 - y;
			if (sourceY < 0 || sourceY >= legacy.length || legacy[sourceY] == null)
				continue;
			System.arraycopy(legacy[sourceY], 0, result, y * width,
					Math.min(width, legacy[sourceY].length));
		}
		return result;
	}

	private static MarioEnemy[] copyEnemies(final MarioEntity[] entities) {
		if (entities == null || entities.length == 0)
			return new MarioEnemy[0];
		return Arrays.stream(entities)
				.filter(MarioEnemy.class::isInstance)
				.map(MarioEnemy.class::cast)
				.toArray(MarioEnemy[]::new);
	}

	public byte getBlockIdAt(final int x, final int y) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			return BLOCK_EMPTY;
		return data[x + y * width];
	}

	public MarioBlock getBlockAt(final int x, final int y) {
		return new MarioBlock(x, y, getBlockIdAt(x, y));
	}

	boolean isSolidAt(final int x, final int y) {
		if (x < 0 || x >= width)
			return true;
		if (y < 0)
			return false;
		return y < height && MarioBlock.isSolid(data[x + y * width]);
	}

	boolean collectCoinAt(final int x, final int y) {
		if (getBlockIdAt(x, y) != BLOCK_COIN)
			return false;
		data[x + y * width] = BLOCK_EMPTY;
		return true;
	}

	HitResult hitBlockAt(final int x, final int y, final boolean canBreakBricks) {
		final byte block = getBlockIdAt(x, y);
		return switch (block) {
			case BLOCK_QUESTION_COIN -> {
				data[x + y * width] = BLOCK_USED;
				yield HitResult.COIN;
			}
			case BLOCK_QUESTION_MUSHROOM -> {
				data[x + y * width] = BLOCK_USED;
				yield HitResult.MUSHROOM;
			}
			case BLOCK_BRICK -> {
				if (canBreakBricks) {
					data[x + y * width] = BLOCK_EMPTY;
					yield HitResult.BREAK;
				}
				yield HitResult.BUMP;
			}
			case BLOCK_USED -> HitResult.BUMP;
			default -> HitResult.NONE;
		};
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public String getName() {
		return name;
	}

	public int getBackgroundIndex() {
		return backgroundIndex;
	}

	public void reset() {
		System.arraycopy(initialData, 0, data, 0, data.length);
		resetEnemies();
	}

	void resetEnemies() {
		for (final MarioEnemy enemy : enemies)
			enemy.reset();
	}

	public double getSpawnPointX() {
		return spawnPointX;
	}

	public double getSpawnPointY() {
		return spawnPointY;
	}

	public MarioEntity[] getEntities() {
		return enemies.clone();
	}

	MarioEnemy[] getEnemies() {
		return enemies;
	}

	public double getGoalX() {
		return goalX;
	}

	public int getGoalBaseY() {
		return goalBaseY;
	}

	public MarioEvent[] getEvents() {
		return events.clone();
	}

	int getCheckpointCount() {
		return checkpointXs.length;
	}

	double getCheckpointX(final int index) {
		return checkpointXs[index];
	}

	int storageBytes() {
		return initialData.length + data.length;
	}

	double findStandingY(final double worldX) {
		final int tileX = Math.max(0, Math.min(width - 1, (int) Math.floor(worldX)));
		for (int y = height - 1; y >= 0; y--)
			if (isSolidAt(tileX, y))
				return y + 1d;
		return spawnPointY;
	}

	private static final class Builder {
		private final String name;
		private final int backgroundIndex;
		private final int width;
		private final byte[] blocks;

		private Builder(final String name, final int backgroundIndex, final int width) {
			this.name = name;
			this.backgroundIndex = backgroundIndex;
			this.width = width;
			blocks = new byte[width * LEVEL_HEIGHT];
		}

		private Builder ground(final int fromX, final int toX) {
			return groundRect(fromX, toX, 0, 1);
		}

		private Builder groundRect(final int fromX, final int toX, final int fromY, final int toY) {
			for (int y = fromY; y <= toY; y++)
				for (int x = fromX; x <= toX; x++)
					set(x, y, BLOCK_GROUND);
			return this;
		}

		private Builder pit(final int fromX, final int toX) {
			for (int y = 0; y < LEVEL_HEIGHT; y++)
				for (int x = fromX; x <= toX; x++)
					set(x, y, BLOCK_EMPTY);
			return this;
		}

		private Builder bricks(final int fromX, final int toX, final int y) {
			for (int x = fromX; x <= toX; x++)
				set(x, y, BLOCK_BRICK);
			return this;
		}

		private Builder question(final int x, final int y, final boolean mushroom) {
			set(x, y, mushroom ? BLOCK_QUESTION_MUSHROOM : BLOCK_QUESTION_COIN);
			return this;
		}

		private Builder pipe(final int x, final int tileHeight) {
			for (int y = 2; y < 2 + tileHeight; y++) {
				set(x, y, BLOCK_PIPE);
				set(x + 1, y, BLOCK_PIPE);
			}
			return this;
		}

		private Builder coins(final int fromX, final int toX, final int y, final int spacing) {
			for (int x = fromX; x <= toX; x += Math.max(1, spacing))
				if (get(x, y) == BLOCK_EMPTY)
					set(x, y, BLOCK_COIN);
			return this;
		}

		private MarioWorld build(final double[] checkpoints, final double goalX,
				final MarioEnemy[] enemies) {
			return new MarioWorld(name, backgroundIndex, 2, 2, width, LEVEL_HEIGHT,
					blocks, new MarioEvent[0], enemies, checkpoints, goalX, 2);
		}

		private byte get(final int x, final int y) {
			if (x < 0 || x >= width || y < 0 || y >= LEVEL_HEIGHT)
				return BLOCK_EMPTY;
			return blocks[x + y * width];
		}

		private void set(final int x, final int y, final byte block) {
			if (x >= 0 && x < width && y >= 0 && y < LEVEL_HEIGHT)
				blocks[x + y * width] = block;
		}
	}
}
