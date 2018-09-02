package it.cavallium.warppi.gui;

public interface GraphicalInterface {
	public void create() throws InterruptedException;

	public void initialize() throws InterruptedException;

	public void render();

	public void renderTopmost();

	public void beforeRender(float dt);

	public boolean mustBeRefreshed();
}
