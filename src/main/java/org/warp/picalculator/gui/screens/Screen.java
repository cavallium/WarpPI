package org.warp.picalculator.gui.screens;

import org.warp.picalculator.event.KeyboardEventListener;
import org.warp.picalculator.event.TouchEventListener;
import org.warp.picalculator.gui.DisplayManager;
import org.warp.picalculator.gui.GraphicalInterface;

public abstract class Screen implements KeyboardEventListener, TouchEventListener, GraphicalInterface {
	public DisplayManager d;
	public boolean created = false;
	public boolean initialized = false;
	public boolean canBeInHistory = false;

	public Screen() {}

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
}
