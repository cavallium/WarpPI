package it.cavallium.warppi.gui;

public abstract class HUD implements GraphicalInterface {
	public DisplayManager d;
	public boolean created = false;
	public boolean graphicInitialized = false;
	public boolean initialized = false;
	public boolean visible = true;
	
	public HUD() {}

	@Override
	public void initialize() throws InterruptedException {
		if (!initialized) {
			initialized = true;
			initialized();
		}
	}

	@Override
	public void initializeGraphic() throws InterruptedException {
		if (!graphicInitialized) {
			graphicInitialized = true;
			graphicInitialized();
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

	public abstract void graphicInitialized() throws InterruptedException;
	
	public abstract void initialized() throws InterruptedException;

	public abstract void renderBackground();

	@Override
	public abstract void render();

	public abstract void renderTopmostBackground();

	@Override
	public abstract void renderTopmost();

	@Override
	public abstract void beforeRender(float dt);

	@Override
	public boolean mustBeRefreshed() {
		return true;
	}

	public void hide() {
		visible = false;
	}

	public void show() {
		visible = true;
	}

}
