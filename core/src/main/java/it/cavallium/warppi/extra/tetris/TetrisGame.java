package it.cavallium.warppi.extra.tetris;

import java.util.Arrays;

public class TetrisGame {

	public static final int WIDTH = 10, HEIGHT = 22;
	public static final double TICK_TIME = 0.25, DOWN_TIME = 0.10, MOVE_TIMER = 0.125;
	private double tickTimer, downTimer, leftTimer, rightTimer, upTimer;
	private BlockColor[] grid;
	private BlockColor[] hovergrid;
	private volatile BlockColor[] renderedGrid;
	private GameStatus gameStatus;
	private int score;
	private double currentTime;
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

	public void update(float dt, ButtonInfo leftPressed, ButtonInfo rightPressed, ButtonInfo downPressed,
			ButtonInfo upPressed, ButtonInfo okPressed, ButtonInfo backPressed) {
		currentTime += dt;
		tickTimer += dt;
		leftTimer += dt;
		rightTimer += dt;
		downTimer += dt;
		upTimer += dt;
		if (leftPressed.hasUnreadData()) {
			for (int i = leftPressed.readPressed(); i > 0; i--) {
				move(this.currentTetromino, -1, 0, 0);
			}
			leftTimer = -MOVE_TIMER;
		} else if (leftPressed.isPressedNow()) {
			while (leftTimer >= MOVE_TIMER) {
				leftTimer -= MOVE_TIMER;
				move(this.currentTetromino, -1, 0, 0);
			}
		} else {
			leftTimer = 0;
		}
		if (rightPressed.hasUnreadData()) {
			for (int i = rightPressed.readPressed(); i > 0; i--) {
				move(this.currentTetromino, 1, 0, 0);
			}
			rightTimer = -MOVE_TIMER;
		} else if (rightPressed.isPressedNow()) {
			while (rightTimer >= MOVE_TIMER) {
				rightTimer -= MOVE_TIMER;
				move(this.currentTetromino, 1, 0, 0);
			}
		} else {
			rightTimer = 0;
		}
		if (upPressed.hasUnreadData()) {
			for (int i = upPressed.readPressed(); i > 0; i--) {
				move(this.currentTetromino, 0, 0, 1);
			}
			upTimer = -MOVE_TIMER;
		} else if (upPressed.isPressedNow()) {
			while (upTimer >= MOVE_TIMER) {
				upTimer -= MOVE_TIMER;
				move(this.currentTetromino, 0, 0, 1);
			}
		} else {
			upTimer = 0;
		}
		if (downPressed.isPressedNow()) {
			downPressed.readPressed();
			while (downTimer >= DOWN_TIME) {
				downTimer -= DOWN_TIME;
				move(this.currentTetromino, 0, 1, 0);
			}
		} else {
			downTimer = 0;
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

	public void gameTick(ButtonInfo leftPressed, ButtonInfo rightPressed, ButtonInfo downPressed, ButtonInfo okPressed,
			ButtonInfo backPressed) {
		if (move(this.currentTetromino, 0, 1, 0)) {

		} else {
			// Spawn new tetromino and write the old to the permanent grid
			drawCurrentTetromino(grid);
			checkLines();
			placeNextTetromino();
			if (move(this.currentTetromino, 0, 0, 0) == false) {
				// Lose
				this.gameStatus = GameStatus.LOST;
				playAgain();
			}
		}
	}

	private void checkLines() {
		for (int i = HEIGHT - 1; i >= 0; i--) {
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
						int y = i;
						while (y > 0) {
							this.grid[x + (y) * WIDTH] = this.grid[x + (y - 1) * WIDTH];
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
		int half1 = (int) Math.floor(((double) t.getTetrominoGridSize()) / 2d);
		int half2 = blockSize - half1;
		byte aX = (byte) (t.getX() + dX), aY = (byte) (t.getY() + dY);
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
				final int offset = x + y * WIDTH;
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
		final byte middleX = (byte) ((WIDTH - 1) / 2), middleY = 0, rotation = (byte) (Math.random() * 4);
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

	public int getScore() {
		return this.score;
	}
}
