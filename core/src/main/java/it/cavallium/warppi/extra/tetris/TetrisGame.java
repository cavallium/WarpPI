package it.cavallium.warppi.extra.tetris;

public class TetrisGame {

	private static final int WIDTH = 10, HEIGHT = 22;
	private BlockType[] grid;
	private BlockType[] hovergrid;
	private GameStatus gameStatus = GameStatus.INITIAL;
	private int score = 0;
	
	public TetrisGame() {
		
	}

	private void playAgain() {
		grid = new BlockType[WIDTH * HEIGHT];
		hovergrid = new BlockType[WIDTH * HEIGHT];
		score = 0;
		gameStatus = GameStatus.PLAYING;
	}
}
