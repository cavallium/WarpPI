package it.cavallium.warppi.extra.tetris;

public class TetrominoJBlue extends Tetromino {
	public TetrominoJBlue(byte x, byte y, byte rotation) {
		super(x, y, rotation, TetrominoType.J_BLUE);
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
						w,o,o,
						w,w,w,
						o,o,o
				};
			case 1:
				return new boolean[] {
						o,w,w,
						o,w,o,
						o,w,o
				};
			case 2:
				return new boolean[] {
						o,o,o,
						w,w,w,
						o,o,w
				};
			case 3:
				return new boolean[] {
						o,w,o,
						o,w,o,
						w,w,o
				};
			default:
				throw new NullPointerException();
		}
	}
}
