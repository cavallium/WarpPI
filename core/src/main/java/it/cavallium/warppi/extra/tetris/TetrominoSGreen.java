package it.cavallium.warppi.extra.tetris;

public class TetrominoSGreen extends Tetromino {
	public TetrominoSGreen(byte x, byte y, byte rotation) {
		super(x, y, rotation, TetrominoType.S_GREEN);
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
						o,w,w,
						w,w,o,
						o,o,o
				};
			case 1:
				return new boolean[] {
						o,w,o,
						o,w,w,
						o,o,w
				};
			case 2:
				return new boolean[] {
						o,o,o,
						o,w,w,
						w,w,o
				};
			case 3:
				return new boolean[] {
						w,o,o,
						w,w,o,
						o,w,o
				};
			default:
				throw new NullPointerException();
		}
	}
}
