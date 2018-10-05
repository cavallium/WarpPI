package it.cavallium.warppi.extra.tetris;

public class MarioBlock {
	private final int x;
	private final int y;
	private final byte id;

	public MarioBlock(final int x, final int y, final byte b) {
		this.x = x;
		this.y = y;
		id = b;
	}

	public boolean isSolid() {
		return MarioBlock.isSolid(id);
	}

	public byte getID() {
		return id;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public static boolean isSolid(final byte id) {
		return id != 0b0;
	}
}
