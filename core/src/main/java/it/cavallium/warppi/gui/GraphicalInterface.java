package it.cavallium.warppi.gui;

public interface GraphicalInterface {
	void create() throws InterruptedException;

	void initialize() throws InterruptedException;

	void render();

	void renderTopmost();

	void beforeRender(float dt);

	boolean mustBeRefreshed();
}
