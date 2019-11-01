package it.cavallium.warppi.extra.tetris;

import java.io.IOException;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.device.input.Keyboard;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.gui.HistoryBehavior;
import it.cavallium.warppi.gui.RenderContext;
import it.cavallium.warppi.gui.ScreenContext;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.Skin;
import it.cavallium.warppi.gui.screens.Screen;

public class TetrisScreen extends Screen {

	private TetrisGame g;

	private ButtonInfo leftPressed = new ButtonInfo();

	private ButtonInfo rightPressed = new ButtonInfo();

	private ButtonInfo upPressed = new ButtonInfo();

	private ButtonInfo downPressed = new ButtonInfo();

	private ButtonInfo okPressed = new ButtonInfo();

	private ButtonInfo backPressed = new ButtonInfo();

	private DisplayOutputDevice e;

	private Renderer r;

	private static Skin skin;
	
	public TetrisScreen() {
		super();
		historyBehavior = HistoryBehavior.ALWAYS_KEEP_IN_HISTORY;
	}

	@Override
	public void initialized() {
		StaticVars.windowZoom.submit(2f);
	}

	@Override
	public void graphicInitialized(ScreenContext ctx) {
		try {
			e = d.display;
			r = d.renderer;
			if (TetrisScreen.skin == null) {
				TetrisScreen.skin = d.display.getGraphicEngine().loadSkin("/tetrisskin.png");
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void created() throws InterruptedException {
		g = new TetrisGame();
	}

	@Override
	public void beforeRender(ScreenContext ctx, final float dt) {
		d.renderer.glClearColor(0xff000000);
			g.update(dt, leftPressed, rightPressed, downPressed, upPressed, okPressed, backPressed);
	}

	@Override
	public void render(RenderContext ctx) {
		DisplayOutputDevice display = d.display;
		if (TetrisScreen.skin != null) {
			TetrisScreen.skin.use(e);
		}
		r.glColor3f(1, 1, 1);
		BlockColor[] renderedGrid = g.getRenderedGrid();
		int centerScreen = ctx.getWidth()/2;
		int centerGrid = TetrisGame.WIDTH*6/2-1;
		final int leftOffset = centerScreen - centerGrid;
		final int topOffset = ctx.getHeight() - TetrisGame.HEIGHT*6-1;
		for (int y = 0; y < TetrisGame.HEIGHT; y++) {
			for (int x = 0; x < TetrisGame.WIDTH; x++) {
				final int offset = x+y*TetrisGame.WIDTH;
				final BlockColor type = renderedGrid[offset];
				if (type != null) {
					r.glFillRect(leftOffset + x * 5, topOffset + (y+3) * 5, 5, 5, renderedGrid[offset].ordinal() * 5, 0, 5, 5);
				} else {
					r.glFillRect(leftOffset + x * 5, topOffset + (y+3) * 5, 5, 5, 7 * 5, 0, 5, 5);
				}
			}
		}
		
		
		Tetromino nextTetromino = g.getNextTetromino();
		if (nextTetromino != null) {
			r.glColor3f(0.25f, 0.25f, 0.25f);
			r.glFillColor(leftOffset + (TetrisGame.WIDTH + 3) * 5, topOffset + 3 * 5, 5*4, 5*4);
			r.glColor3f(1,1,1);
			boolean[] renderedNextTetromino = nextTetromino.getRenderedBlock();
			final BlockColor type = nextTetromino.getColor();
			int nextTetrominoGridSize = nextTetromino.getTetrominoGridSize();
			int nextGridOffset = 4*5/2 - nextTetrominoGridSize*5/2;
			for (int y = 0; y < nextTetrominoGridSize; y++) {
				for (int x = 0; x < nextTetrominoGridSize; x++) {
					final int offset = x+y*nextTetrominoGridSize;
					if (renderedNextTetromino[offset]) {
						if (type != null) {
							r.glFillRect(leftOffset + nextGridOffset + (TetrisGame.WIDTH + 3 + x) * 5, topOffset + nextGridOffset + (3 + y) * 5, 5, 5, type.ordinal() * 5, 0, 5, 5);
						}
					}
				}
			}
		}
		r.glColor3f(1,1,1);
		r.glDrawStringLeft(leftOffset + (TetrisGame.WIDTH + 3) * 5, topOffset + (3+5) * 5, "SCORE:"+g.getScore());
	}
	
	@Override
	public boolean onKeyPressed(KeyPressedEvent k) {
		switch (k.getKey()) {
			case LEFT: {
				leftPressed.press();
				return true;
			}
			case RIGHT: {
				rightPressed.press();
				return true;
			}
			case UP: {
				upPressed.press();
				return true;
			}
			case DOWN: {
				downPressed.press();
				return true;
			}
			case OK: {
				okPressed.press();
				g.playAgain();
				return true;
			}
			case BACK: {
				backPressed.press();
				return true;
			}
			default: return false;
		}
	}
	
	@Override
	public boolean onKeyReleased(KeyReleasedEvent k) {
		switch (k.getKey()) {
			case LEFT: {
				leftPressed.release();
				return true;
			}
			case RIGHT: {
				rightPressed.release();
				return true;
			}
			case UP: {
				upPressed.release();
				return true;
			}
			case DOWN: {
				downPressed.release();
				return true;
			}
			case OK: {
				okPressed.release();
				return true;
			}
			case BACK: {
				backPressed.release();
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
		return "Tetris";
	}
}
