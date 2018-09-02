package org.warp.picalculator.gui.screens;

public class EmptyScreen extends Screen {

	public float endLoading;

	public EmptyScreen() {
		super();
		canBeInHistory = false;
	}

	@Override
	public void created() throws InterruptedException {
		endLoading = 0;
	}

	@Override
	public void initialized() throws InterruptedException {}

	@Override
	public void render() {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeRender(float dt) {

	}

	@Override
	public boolean mustBeRefreshed() {
		return true;
	}

}
