package it.cavallium.warppi.extra.tetris;

public class TetrominoZRed extends Tetromino {
	public TetrominoZRed(byte x, byte y, byte rotation) {
		super(x, y, rotation, TetrominoType.Z_RED);
	}
	
	@Override
	public int getTetrominoGridSize() {
		return 3;
	}
	
	@Override
	public boolean[] getRenderedBlock() {
		switch(getRotation()) {
			case 0:
				return new boolean[] {
						w,w,o,
						o,w,w,
						o,o,o
				};
			case 1:
				return new boolean[] {
						o,o,w,
						o,w,w,
						o,w,o
				};
			case 2:
				return new boolean[] {
						o,o,o,
						w,w,o,
						o,w,w
				};
			case 3:
				return new boolean[] {
						o,w,o,
						w,w,o,
						w,o,o
				};
			default:
				throw new NullPointerException();
		}
	}
}
