package it.cavallium.warppi.gui.screens;

import it.cavallium.warppi.event.KeyboardEventListener;
import it.cavallium.warppi.event.TouchEventListener;
import it.cavallium.warppi.gui.DisplayManager;
import it.cavallium.warppi.gui.GraphicalInterface;
import it.cavallium.warppi.gui.HistoryBehavior;

public abstract class Screen implements KeyboardEventListener, TouchEventListener, GraphicalInterface {
	public DisplayManager d;
	public boolean created = false;
	public boolean initialized = false;
	public HistoryBehavior historyBehavior = HistoryBehavior.NORMAL;
	
	public static long lastDebugScreenID = 1;
	public final long debugScreenID;

	public Screen() {
		debugScreenID = lastDebugScreenID++;
	}

	@Override
	public void initialize() throws InterruptedException {
		if (!initialized) {
			initialized = true;
			initialized();
		}
	}

	@Override
	public void create() throws InterruptedException {
		if (!created) {
			created = true;
			created();
		}
	}

	public abstract void created() throws InterruptedException;

	public abstract void initialized() throws InterruptedException;

	@Override
	public abstract void render();

	@Override
	public void renderTopmost() {

	}

	@Override
	public abstract void beforeRender(float dt);

	@Override
	public abstract boolean mustBeRefreshed();
	
	public abstract String getSessionTitle();
}
