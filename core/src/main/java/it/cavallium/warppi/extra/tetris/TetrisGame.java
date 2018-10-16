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

	public void update(float dt, boolean leftPressed, boolean rightPressed, boolean downPressed, boolean upPressed, boolean okPressed, boolean backPressed) {
		currentTime += dt;
		tickTimer += dt;
		if (!(leftPressed && rightPressed)) {
			if (leftPressed) {
				move(this.currentTetromino, -1, 0, 0);
			} else if (rightPressed) {
				move(this.currentTetromino, 1, 0, 0);
			}
		}
		if (downPressed) {
			move(this.currentTetromino, 0, 1, 0);
		}
		if (upPressed) {
			move(this.currentTetromino, 0, 0, 1);
		}
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
		if (move(this.currentTetromino, 0, 1, 0)) {
			
		} else {
			// Spawn new tetromino and write the old to the permanent grid
			drawCurrentTetromino(grid);
			checkLines();
			placeNextTetromino();
			if (move(this.currentTetromino, 0, 0, 0) == false) {
				// Lose
				this.gameStatus = GameStatus.LOST;
			}
		}
	}
	
	private void checkLines() {
		for(int i = HEIGHT - 1; i >= 0; i--) {
			boolean scored = true;
			while (scored) {
				for (int x = 0; x < WIDTH; x++) {
					if (this.grid[x + i * WIDTH] == null) {
						scored = false;
						break;
					}
				}
				if (scored) {
					this.score += WIDTH;
					for (int x = 0; x < WIDTH; x++) {
						int y = HEIGHT - i - 2;
						while (i + y > 0) {
								this.grid[x + (i + y + 1) * WIDTH] = this.grid[x + (i + y) * WIDTH];
							y--;
						}
					}
				}
			}
		}
	}

	private boolean move(Tetromino t, int dX, int dY, int dRotation) {
		byte rot = (byte) ((t.getRotation() + dRotation) % 4);
		boolean[] block = t.getRenderedBlock(rot);
		int blockSize = t.getTetrominoGridSize();
		int half1 = (int) Math.floor(((double)t.getTetrominoGridSize())/2d);
		int half2 = blockSize - half1;
		byte aX = (byte)(t.getX()+dX), aY = (byte)(t.getY()+dY);
		int blockX = 0, blockY = 0;
		for (int x = aX - half1; x < aX + half2; x++) {
			for (int y = aY - half1; y < aY + half2; y++) {
				if (block[blockX + blockY * blockSize] == true) {
					if (x >= 0 & y >= 0 & x < WIDTH & (x + y * WIDTH) < this.grid.length) {
						if (this.grid[x + y * WIDTH] != null) {
							return false;
						}
					} else {
						return false;
					}
				}
				blockY++;
			}	
			blockY = 0;
			blockX++;
		}
		t.setRotation(rot);
		t.setX(aX);
		t.setY(aY);
		return true;
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
		final byte middleX = (byte)((WIDTH - 1)/2), middleY = 0, rotation = (byte) (Math.random() * 4);
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
	
	public Tetromino getNextTetromino() {
		return this.nextTetromino;
	}

	private void drawCurrentTetromino(BlockColor[] grid) {
		currentTetromino.draw(grid, WIDTH);
	}

	public BlockColor[] getRenderedGrid() {
		return renderedGrid;
	}
}
