package it.cavallium.warppi.extra.tetris;

public class TetrominoTPurple extends Tetromino {
	public TetrominoTPurple(byte x, byte y, byte rotation) {
		super(x, y, rotation, TetrominoType.I_CYAN);
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
						o,w,o,
						w,w,w,
						o,o,o
				};
			case 1:
				return new boolean[] {
						o,w,o,
						o,w,w,
						o,w,o
				};
			case 2:
				return new boolean[] {
						o,o,o,
						w,w,w,
						o,w,o
				};
			case 3:
				return new boolean[] {
						o,w,o,
						w,w,o,
						o,w,o
				};
			default:
				throw new NullPointerException();
		}
	}
}
