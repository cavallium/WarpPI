package it.cavallium.warppi.extra.tetris;

public class TetrisGame {

	static final int WIDTH = 10, HEIGHT = 22;
	BlockType[] grid;
	BlockType[] hovergrid;
	GameStatus gameStatus = GameStatus.INITIAL;
	int score = 0;
	
	public TetrisGame() {
		
	}

	void playAgain() {
		grid = new BlockType[WIDTH * HEIGHT];
		hovergrid = new BlockType[WIDTH * HEIGHT];
		score = 0;
		gameStatus = GameStatus.PLAYING;
	}
}
