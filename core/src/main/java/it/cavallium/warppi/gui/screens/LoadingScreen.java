package it.cavallium.warppi.gui.screens;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.gui.GraphicUtils;
import it.cavallium.warppi.gui.HistoryBehavior;

public class LoadingScreen extends Screen {

	public float endLoading;
	boolean mustRefresh = true;
	public float loadingTextTranslation = 0.0f;
	public boolean loaded = false;
	private float previousZoomValue = 1;
	private volatile boolean ended = false;

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
		previousZoomValue = StaticVars.windowZoomFunction.apply(StaticVars.windowZoom.getLastValue());
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getHUD().hide();
		StaticVars.windowZoom.submit(1f);
	}
	
	@Override
	public void graphicInitialized() throws InterruptedException {}

	@Override
	public void beforeRender(final float dt) {
		loadingTextTranslation = GraphicUtils.sinDeg(endLoading * 90f) * 10f;

		endLoading += dt;
		if (!ended && loaded && (WarpPI.getPlatform().getSettings().isDebugEnabled() || endLoading >= 3.5f)) {
			ended = true;
			StaticVars.windowZoom.submit(previousZoomValue);
			WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().getHUD().show();
			WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().setScreen(new MathInputScreen());
		}
		mustRefresh = true;
	}

	@Override
	public void render() {
		DisplayOutputDevice display = d.display;
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().guiSkin.use(WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().display);
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor3i(255, 255, 255);
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glFillRect(display.getDisplaySize()[0] / 2f - 80, display.getDisplaySize()[1] / 2f - 64, 160, 48, 0, 32, 160, 48);
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glFillRect(display.getDisplaySize()[0] / 2f - 24, display.getDisplaySize()[1] / 2f - loadingTextTranslation, 48, 48, 160, 32, 48, 48);

		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glFillRect(display.getDisplaySize()[0] - 224, display.getDisplaySize()[1] - 48, 224, 48, 0, 80, 224, 48);
		WarpPI.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glFillRect(display.getDisplaySize()[0] - 160 - 24 - 224, display.getDisplaySize()[1] - 48, 160, 48, 224, 80, 160, 48);

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
