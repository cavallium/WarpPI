package it.cavallium.warppi.extra.tetris;

public class TetrominoLOrange extends Tetromino {
	public TetrominoLOrange(byte x, byte y, byte rotation) {
		super(x, y, rotation, TetrominoType.L_ORANGE);
	}
	
	@Override
	public int getTetrominoGridSize() {
		return 3;
	}
	
	@Override
	public boolean[] getRenderedBlock(final byte rotation) {
		switch(rotation) {
			case 0:
				return new boolean[] {
						o,o,w,
						w,w,w,
						o,o,o
				};
			case 1:
				return new boolean[] {
						o,w,o,
						o,w,o,
						o,w,w
				};
			case 2:
				return new boolean[] {
						o,o,o,
						w,w,w,
						w,o,o
				};
			case 3:
				return new boolean[] {
						w,w,o,
						o,w,o,
						o,w,o
				};
			default:
				throw new NullPointerException();
		}
	}
}
