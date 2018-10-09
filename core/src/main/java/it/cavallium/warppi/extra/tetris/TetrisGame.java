package it.cavallium.warppi.extra.tetris;

public class TetrisGame {

	public static final int WIDTH = 10, HEIGHT = 22;
	private BlockType[] grid;
	private BlockType[] hovergrid;
	private BlockType[] renderedGrid;
	private GameStatus gameStatus = GameStatus.INITIAL;
	private int score = 0;
	private double currentTime = 0;
	
	public TetrisGame() {
		
	}

	void playAgain() {
		grid = new BlockType[WIDTH * HEIGHT];
		hovergrid = new BlockType[WIDTH * HEIGHT];
		renderedGrid = new BlockType[WIDTH * HEIGHT];
		score = 0;
		currentTime = 0;
		gameStatus = GameStatus.PLAYING;
	}

	public void gameTick(float dt, boolean leftPressed, boolean rightPressed, boolean downPressed, boolean okPressed, boolean backPressed) {
		currentTime += dt;
		if (gameStatus == GameStatus.INITIAL) {
			playAgain();
		} else {
			
		}
		renderGrid();
	}
	
	public void renderGrid() {
		this.renderedGrid = new BlockType[WIDTH*HEIGHT];
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				final int offset = x+y*WIDTH;
				renderedGrid[offset] = hovergrid[offset] != null ? hovergrid[offset] : grid[offset];
			}
		}
	}
	
	public BlockType[] getRenderedGrid() {
		return renderedGrid;
	}
}
