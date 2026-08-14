package it.cavallium.warppi.gui.screens;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.extra.mario.MarioScreen;
import it.cavallium.warppi.gui.GraphicUtils;
import it.cavallium.warppi.gui.HistoryBehavior;
import it.cavallium.warppi.gui.RenderContext;
import it.cavallium.warppi.gui.ScreenContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;
import it.cavallium.warppi.gui.graphicengine.Skin;

public class LoadingScreen extends Screen {

	public float endLoading;
	boolean mustRefresh = true;
	public float loadingTextTranslation = 0.0f;
	public boolean loaded = false;
	private float previousZoomValue = 1;
	private volatile boolean ended = false;

	private static final int TITLE_WIDTH = 99;
	private static final int RASPBERRY_PI_WIDTH = 217;
	private static final int WARPPI_WIDTH = 158;
	private static final int SPINNER_WIDTH = 44;
	private static final int SPINNER_HEIGHT = 45;
	private static final int LOADING_RASPBERRY_PI_X = TITLE_WIDTH;
	private static final int LOADING_WARPPI_X = TITLE_WIDTH + RASPBERRY_PI_WIDTH;
	private static final int PROGRESS_WIDTH = 64;
	private static final int PROGRESS_HEIGHT = 3;
	private static final int PROGRESS_Y_OFFSET = 62;

	public LoadingScreen() {
		super();
		historyBehavior = HistoryBehavior.DONT_KEEP_IN_HISTORY;
	}

	@Override
	public void created() throws InterruptedException {
		WarpPI.INSTANCE.isLoaded().subscribe((loaded) -> {
			this.loaded = loaded;
		});
		endLoading = 0;
	}

	@Override
	public void initialized() throws InterruptedException {
		float lastZoomValue = StaticVars.windowZoom.getLastValue();
		previousZoomValue = lastZoomValue;
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getHUD().hide();
		if (lastZoomValue != 1.0f) {
			StaticVars.windowZoom.submit(1f);
		}
	}
	
	@Override
	public void graphicInitialized(ScreenContext ctx) throws InterruptedException {}

	@Override
	public boolean allowsUserInput() {
		return false;
	}

	@Override
	public void beforeRender(ScreenContext ctx, final float dt) {
		loadingTextTranslation = GraphicUtils.sinDeg(endLoading * 90f) * 10f;

		endLoading += dt;
		if (!ended && loaded && ((WarpPI.getPlatform().getSettings().isDebugEnabled() && endLoading >= 1.5f) || endLoading >= 3.5f)) {
			ended = true;
			if (previousZoomValue != 1.0f) {
				StaticVars.windowZoom.submit(previousZoomValue);
			}
			d.requestLoadingSkinUnload();
			WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getHUD().show();
			WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().setScreen(new MathInputScreen());
		}
		mustRefresh = true;
	}

	@Override
	public void render(RenderContext ctx) {
		final DisplayOutputDevice display = d.display;
		final Renderer renderer = ctx.getRenderer();
		renderer.glColor3i(255, 255, 255);
		renderLoadingAssets(ctx, display, renderer);
		renderProgressBar(ctx.getWidth(), ctx.getHeight(), renderer,
				WarpPI.INSTANCE.getLoadingProgressPercent());
	}

	static void renderProgressBar(final int screenWidth, final int screenHeight,
			final Renderer renderer, final int progressPercent) {
		final int x = (screenWidth - PROGRESS_WIDTH) / 2;
		final int y = screenHeight / 2 + PROGRESS_Y_OFFSET;
		final int fillWidth = progressBarFillWidth(progressPercent);

		renderer.glColor3i(154, 154, 142);
		renderer.glFillColor(x, y, PROGRESS_WIDTH, PROGRESS_HEIGHT);
		if (fillWidth > 0) {
			renderer.glColor3i(0, 0, 0);
			renderer.glFillColor(x, y, fillWidth, PROGRESS_HEIGHT);
		}
		renderer.glColor3i(255, 255, 255);
	}

	static int progressBarFillWidth(final int progressPercent) {
		final int boundedPercent = Math.max(0, Math.min(100, progressPercent));
		return PROGRESS_WIDTH * boundedPercent / 100;
	}

	private void renderLoadingAssets(final RenderContext ctx, final DisplayOutputDevice display,
			final Renderer renderer) {
		final Skin opaque = d.getLoadingSkin();
		opaque.use(display);
		renderer.glFillRect(ctx.getWidth() / 2f - 49, ctx.getHeight() / 2f - 64,
				TITLE_WIDTH, 48, 0, 0, TITLE_WIDTH, 48);
		renderer.glFillRect(ctx.getWidth() - 221, ctx.getHeight() - 48,
				RASPBERRY_PI_WIDTH, 48, LOADING_RASPBERRY_PI_X, 0,
				RASPBERRY_PI_WIDTH, 48);
		renderer.glFillRect(ctx.getWidth() - 160 - 24 - 224, ctx.getHeight() - 48,
				WARPPI_WIDTH, 48, LOADING_WARPPI_X, 0, WARPPI_WIDTH, 48);

		final Skin spinner = d.getLoadingSpinnerSkin();
		spinner.use(display);
		renderer.glFillRect(ctx.getWidth() / 2f - 22,
				ctx.getHeight() / 2f - loadingTextTranslation + 1,
				SPINNER_WIDTH, SPINNER_HEIGHT, 0, 0, SPINNER_WIDTH, SPINNER_HEIGHT);
	}

	@Override
	public boolean mustBeRefreshed() {
		if (mustRefresh) {
			mustRefresh = false;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getSessionTitle() {
		return "Loading...";
	}

}
