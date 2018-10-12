package it.cavallium.warppi.extra.tetris;

public abstract class Tetromino {
	private byte x, y, rotation;
	private final TetrominoType type;
	public final boolean o = false, w = true;
	
	public Tetromino(byte x, byte y, byte rotation, TetrominoType type) {
		super();
		this.x = x;
		this.y = y;
		this.rotation = rotation;
		this.type = type;
	}

	public byte getX() {
		return x;
	}

	public void setX(byte x) {
		this.x = x;
	}

	public byte getY() {
		return y;
	}

	public void setY(byte y) {
		this.y = y;
	}

	public byte getRotation() {
		return rotation;
	}

	public void setRotation(byte rotation) {
		this.rotation = rotation;
	}

	public TetrominoType getType() {
		return type;
	}
	
	public BlockColor getColor() {
		switch(type) {
			case I_CYAN:
				return BlockColor.CYAN;
			case J_BLUE:
				return BlockColor.BLUE;
			case L_ORANGE:
				return BlockColor.ORANGE;
			case O_YELLOW:
				return BlockColor.YELLOW;
			case S_GREEN:
				return BlockColor.GREEN;
			case T_PURPLE:
				return BlockColor.PURPLE;
			case Z_RED:
				return BlockColor.RED;
			default:
				return BlockColor.RED;
		}
	}

	public void draw(BlockColor[] grid, final int WIDTH) {
		boolean[] blockGrid = getRenderedBlock();
		final int tetrominoGridSize = getTetrominoGridSize();
		final int centerOffset = (int) Math.floor((double)tetrominoGridSize/2d);
		final BlockColor type = getColor();
		for (int bx = 0; bx < tetrominoGridSize; bx++) {
			for (int by = 0; by < tetrominoGridSize; by++) {
				if (blockGrid[bx+by*tetrominoGridSize] == w) {
					final int index = x+bx-centerOffset + (y+by-centerOffset) * WIDTH;
					if (index < grid.length && index >= 0) {
						grid[index] = type;
					}
				}
			}
		}
	}

	public void fixInitialPosition() {
		this.y -= (byte) (this.getTetrominoGridSize()/2);
	}
	
	public abstract int getTetrominoGridSize();
	protected abstract boolean[] getRenderedBlock();
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + rotation;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tetromino other = (Tetromino) obj;
		if (rotation != other.rotation)
			return false;
		if (type != other.type)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Tetromino = {\n\t\"x\": \"" + x + "\",\n\ty\": \"" + y + "\",\n\trotation\": \"" + rotation + "\",\n\ttype\": \"" + type + "\"\n}";
	}
	
	
}
