package it.cavallium.warppi.extra.tetris;

public class TetrominoICyan extends Tetromino {
	public TetrominoICyan(byte x, byte y, byte rotation) {
		super(x, y, rotation, TetrominoType.I_CYAN);
	}
	
	@Override
	public int getTetrominoGridSize() {
		return 4;
	}
	
	@Override
	public boolean[] getRenderedBlock(final byte rotation) {
		switch(rotation) {
			case 0:
				return new boolean[] {
						o,o,o,o,
						w,w,w,w,
						o,o,o,o,
						o,o,o,o
				};
			case 1:
				return new boolean[] {
						o,o,w,o,
						o,o,w,o,
						o,o,w,o,
						o,o,w,o
				};
			case 2:
				return new boolean[] {
						o,o,o,o,
						o,o,o,o,
						w,w,w,w,
						o,o,o,o
				};
			case 3:
				return new boolean[] {
						o,w,o,o,
						o,w,o,o,
						o,w,o,o,
						o,w,o,o
				};
			default:
				throw new NullPointerException();
		}
	}
}
