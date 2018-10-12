package it.cavallium.warppi.extra.tetris;

import java.util.Arrays;

public class TetrisGame {

	public static final int WIDTH = 10, HEIGHT = 22;
	public static final double TICK_TIME = 0.25;
	private BlockColor[] grid;
	private BlockColor[] hovergrid;
	private volatile BlockColor[] renderedGrid;
	private GameStatus gameStatus;
	private int score;
	private double currentTime;
	private double tickTimer;
	private Tetromino currentTetromino;
	private Tetromino nextTetromino;
	
	public TetrisGame() {
		resetVariables();
	}

	void playAgain() {
		resetVariables();
		gameStatus = GameStatus.PLAYING;
		placeNextTetromino();
	}
	
	private void resetVariables() {
		grid = new BlockColor[WIDTH * HEIGHT];
		hovergrid = new BlockColor[WIDTH * HEIGHT];
		renderedGrid = new BlockColor[WIDTH * HEIGHT];
		score = 0;
		currentTime = 0;
		tickTimer = 0;
		gameStatus = GameStatus.INITIAL;
		currentTetromino = null;
		nextTetromino = generateRandomTetromino();
		nextTetromino.fixInitialPosition();
	}

	public void update(float dt, boolean leftPressed, boolean rightPressed, boolean downPressed, boolean okPressed, boolean backPressed) {
		currentTime += dt;
		tickTimer += dt;
		while (tickTimer >= TICK_TIME) {
			tickTimer -= TICK_TIME;
			gameTick(leftPressed, rightPressed, downPressed, okPressed, backPressed);
		}
		if (gameStatus == GameStatus.INITIAL) {
			playAgain();
		} else {
			
		}
		renderGrid();
	}

	public void gameTick(boolean leftPressed, boolean rightPressed, boolean downPressed, boolean okPressed, boolean backPressed) {
		this.currentTetromino.setY((byte) (this.currentTetromino.getY() - 1));
	}
	
	public void renderGrid() {
		this.renderedGrid = Arrays.copyOf(grid, grid.length);
		drawCurrentTetromino(this.renderedGrid);
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				final int offset = x+y*WIDTH;
				renderedGrid[offset] = hovergrid[offset] != null ? hovergrid[offset] : renderedGrid[offset];
			}
		}
	}
	
	private void placeNextTetromino() {
		currentTetromino = nextTetromino;
		nextTetromino = generateRandomTetromino();
		nextTetromino.fixInitialPosition();
	}
	
	private Tetromino generateRandomTetromino() {
		int s = (int) (Math.random() * 7);
		final byte middleX = (byte)((WIDTH - 1)/2), middleY = (byte)(HEIGHT - 1), rotation = (byte) (Math.random() * 4);
		switch (s) {
			case 0:
				return new TetrominoICyan(middleX, middleY, rotation);
			case 1:
				return new TetrominoJBlue(middleX, middleY, rotation);
			case 2:
				return new TetrominoLOrange(middleX, middleY, rotation);
			case 3:
				return new TetrominoOYellow(middleX, middleY, rotation);
			case 4:
				return new TetrominoSGreen(middleX, middleY, rotation);
			case 5:
				return new TetrominoTPurple(middleX, middleY, rotation);
			default:
				return new TetrominoZRed(middleX, middleY, rotation);
		}
	}

	private void drawCurrentTetromino(BlockColor[] grid) {
		currentTetromino.draw(grid, WIDTH);
	}

	public BlockColor[] getRenderedGrid() {
		return renderedGrid;
	}
}
