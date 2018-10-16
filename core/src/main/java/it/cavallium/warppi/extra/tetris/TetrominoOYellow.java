package it.cavallium.warppi.extra.tetris;

public class TetrominoOYellow extends Tetromino {
	public TetrominoOYellow(byte x, byte y, byte rotation) {
		super(x, y, rotation, TetrominoType.O_YELLOW);
	}	
	@Override
	public int getTetrominoGridSize() {
		return 2;
	}
	
	@Override
	public boolean[] getRenderedBlock(byte rotation) {
		return new boolean[] {
				w,w,
				w,w,
		};
	}
}
