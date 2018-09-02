package it.cavallium.warppi.extra.mario;

public class MarioWorld {

	private final int[] spawnPoint;
	private final int width;
	private final int height;
	private final byte[][] data;
	@SuppressWarnings("unused")
	private final MarioEvent[] events;
	private final MarioEntity[] entities;

	/**
	 * @param width
	 * @param height
	 * @param data
	 * @param events
	 * @param marioEnemies
	 */
	public MarioWorld(int[] spawnPoint, int width, int height, byte[][] data, MarioEvent[] events, MarioEntity[] entities) {
		this.spawnPoint = spawnPoint;
		this.width = width;
		this.height = height;
		this.data = data;
		this.events = events;
		this.entities = entities;
	}

	public byte getBlockIdAt(int x, int y) {
		final int idy = (height - 1 - y);
		if (idy < 0 || idy >= data.length) {
			return 0b0;
		}
		final int idx = x;
		if (idx < 0 || idx >= data[0].length) {
			return 0b0;
		}
		return data[idy][idx];
	}

	public MarioBlock getBlockAt(int x, int y) {
		return new MarioBlock(x, y, getBlockIdAt(x, y));
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void reset() {

	}

	public double getSpawnPointX() {
		return spawnPoint[0];
	}

	public double getSpawnPointY() {
		return spawnPoint[1];
	}

	public MarioEntity[] getEntities() {
		return entities;
	}

}
