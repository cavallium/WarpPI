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
			g.gameTick(dt, leftPressed, rightPressed, downPressed, okPressed, backPressed);
	}

	@Override
	public void render() {
		if (TetrisScreen.skin != null) {
			TetrisScreen.skin.use(e);
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

	@Override
	public boolean onKeyPressed(KeyPressedEvent k) {
		System.out.println("pr:"+k.getKey());
		return super.onKeyPressed(k);
	}
	
	@Override
	public boolean onKeyReleased(KeyReleasedEvent k) {
		System.out.println("re:"+k.getKey());
		return super.onKeyReleased(k);
	}
}
