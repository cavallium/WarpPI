package it.cavallium.warppi.gui.screens;

import it.cavallium.warppi.event.KeyboardEventListener;
import it.cavallium.warppi.event.TouchEventListener;
import it.cavallium.warppi.gui.*;

public abstract class Screen implements KeyboardEventListener, TouchEventListener, GraphicalInterface {
	public static final int DEFAULT_BACKGROUND_COLOR = 0xFFC5C2AF;

	public DisplayManager d;
	public volatile boolean created = false;
	public volatile boolean initialized = false;
	public volatile boolean graphicInitialized = false;
	public HistoryBehavior historyBehavior = HistoryBehavior.NORMAL;
	
	public static long lastDebugScreenID = 1;
	public final long debugScreenID;

	public Screen() {
		debugScreenID = lastDebugScreenID++;
	}

	@Override
	public final synchronized void initializeGraphic(ScreenContext ctx) throws InterruptedException {
		if (!graphicInitialized) {
			graphicInitialized(ctx);
			graphicInitialized = true;
		}
	}

	@Override
	public final synchronized void initialize() throws InterruptedException {
		if (!initialized) {
			initialized();
			initialized = true;
		}
	}

	@Override
	public final synchronized void create() throws InterruptedException {
		if (!created) {
			created();
			created = true;
		}
	}

	public final boolean isReadyForInput() {
		return graphicInitialized && initialized;
	}

	/**
	 * Called when creating the screen
	 * Called before initialized()
	 * Called before graphicInitialized()
	 * @throws InterruptedException
	 */
	public abstract void created() throws InterruptedException;

	/**
	 * Load everything except skins, etc...
	 * Called after created()
	 * Called after graphicInitialized()
	 * @throws InterruptedException
	 */
	public abstract void initialized() throws InterruptedException;

	/**
	 * Load skins, etc...
	 * Called after created()
	 * Called before initialized()
	 * @throws InterruptedException
	 */
	public abstract void graphicInitialized(ScreenContext ctx) throws InterruptedException;

	@Override
	public abstract void render(RenderContext ctx);

	@Override
	public void renderTopmost(RenderContext ctx) {

	}

	@Override
	public abstract void beforeRender(ScreenContext ctx, float dt);

	/**
	 * Returns the color used to clear the frame immediately before this screen is rendered.
	 */
	public int getBackgroundColor() {
		return DEFAULT_BACKGROUND_COLOR;
	}

	@Override
	public abstract boolean mustBeRefreshed();
	
	public abstract String getSessionTitle();
}
