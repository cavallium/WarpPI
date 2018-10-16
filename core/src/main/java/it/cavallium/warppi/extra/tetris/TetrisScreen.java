package it.cavallium.warppi.extra.tetris;

import java.io.IOException;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.Keyboard;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.gui.HistoryBehavior;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.gui.screens.Screen;

public class TetrisScreen extends Screen {

	private TetrisGame g;

	private boolean leftPressed;

	private boolean rightPressed;

	private boolean upPressed;

	private boolean downPressed;

	private boolean okPressed;

	private boolean backPressed;

	private GraphicEngine e;

	private Renderer r;

	private static Skin skin;
	
	public TetrisScreen() {
		super();
		historyBehavior = HistoryBehavior.ALWAYS_KEEP_IN_HISTORY;
	}

	@Override
	public void initialized() {
		try {
			e = d.engine;
			r = d.renderer;
			if (TetrisScreen.skin == null) {
				TetrisScreen.skin = Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine.loadSkin("/tetrisskin.png");
			}
			StaticVars.windowZoom.onNext(2f);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void created() throws InterruptedException {
		g = new TetrisGame();
	}

	@Override
	public void beforeRender(final float dt) {
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glClearColor(0xff000000);
			g.update(dt, leftPressed, rightPressed, downPressed, upPressed, okPressed, backPressed);
			upPressed = false;
	}

	@Override
	public void render() {
		if (TetrisScreen.skin != null) {
			TetrisScreen.skin.use(e);
		}
		r.glColor3f(1, 1, 1);
		BlockColor[] renderedGrid = g.getRenderedGrid();
		int centerScreen = StaticVars.screenSize[0]/2;
		int centerGrid = TetrisGame.WIDTH*6/2-1;
		final int leftOffset = centerScreen - centerGrid;
		final int topOffset = StaticVars.screenSize[1] - TetrisGame.HEIGHT*6-1;
		for (int y = 0; y < TetrisGame.HEIGHT; y++) {
			for (int x = 0; x < TetrisGame.WIDTH; x++) {
				final int offset = x+y*TetrisGame.WIDTH;
				final BlockColor type = renderedGrid[offset];
				if (type != null) {
					r.glFillRect(leftOffset + x * 5, topOffset + (y+3) * 5, 5, 5, renderedGrid[offset].ordinal() * 5, 0, 5, 5);
				} else {
					r.glFillRect(leftOffset + x * 5, topOffset + (y+3) * 5, 5, 5, 1 * 5, 0, 2, 2);
				}
			}
		}
		
		
		Tetromino nextTetromino = g.getNextTetromino();
		if (nextTetromino != null) {
			boolean[] renderedNextTetromino = nextTetromino.getRenderedBlock();
			final BlockColor type = nextTetromino.getColor();
			int nextTetrominoGridSize = nextTetromino.getTetrominoGridSize();
			for (int y = 0; y < nextTetrominoGridSize; y++) {
				for (int x = 0; x < nextTetrominoGridSize; x++) {
					final int offset = x+y*nextTetrominoGridSize;
					if (renderedNextTetromino[offset]) {
						if (type != null) {
							r.glFillRect(leftOffset + (TetrisGame.WIDTH + 3 + x) * 5, topOffset + (3 - y) * 5, 5, 5, type.ordinal() * 5, 0, 5, 5);
						} else {
							//r.glFillRect(leftOffset + x * 5, topOffset + (TetrisGame.HEIGHT+3-y) * 5, 5, 5, 1 * 5, 0, 2, 2);
						}
					}
				}
			}
		}
	}
	
	@Override
	public boolean onKeyPressed(KeyPressedEvent k) {
		switch (k.getKey()) {
			case LEFT: {
				leftPressed = true;
				return true;
			}
			case RIGHT: {
				rightPressed = true;
				return true;
			}
			case UP: {
				upPressed = true;
				return true;
			}
			case DOWN: {
				downPressed = true;
				return true;
			}
			case OK: {
				okPressed = true;
				return true;
			}
			case BACK: {
				backPressed = true;
				return true;
			}
			default: return false;
		}
	}
	
	@Override
	public boolean onKeyReleased(KeyReleasedEvent k) {
		switch (k.getKey()) {
			case LEFT: {
				leftPressed = false;
				return true;
			}
			case RIGHT: {
				rightPressed = false;
				return true;
			}
			case UP: {
				upPressed = false;
				return true;
			}
			case DOWN: {
				downPressed = false;
				return true;
			}
			case OK: {
				okPressed = false;
				return true;
			}
			case BACK: {
				backPressed = false;
				return true;
			}
			default: return false;
		}
	}	
	@Override
	public boolean mustBeRefreshed() {
		return true;
	}

	@Override
	public String getSessionTitle() {
		return "Absolutely Not Tetris";
	}
}
