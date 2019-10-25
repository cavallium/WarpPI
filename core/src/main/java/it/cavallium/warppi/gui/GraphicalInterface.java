package it.cavallium.warppi.gui;

public interface GraphicalInterface {
	void create() throws InterruptedException;
	
	void initialize() throws InterruptedException;
	
	void initializeGraphic(ScreenContext ctx) throws InterruptedException;

	void render(RenderContext ctx);

	void renderTopmost(RenderContext ctx);

	void beforeRender(ScreenContext ctx, float dt);

	boolean mustBeRefreshed();
}
