package it.cavallium.warppi.gui.screens;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.device.HardwareDevice;
import it.cavallium.warppi.gui.GraphicUtils;

public class LoadingScreen extends Screen {

	public float endLoading;
	boolean mustRefresh = true;
	public float loadingTextTranslation = 0.0f;
	public boolean loaded = false;
	private float previousZoomValue = 1;

	public LoadingScreen() {
		super();
		canBeInHistory = false;
	}

	@Override
	public void created() throws InterruptedException {
		Engine.INSTANCE.isLoaded().subscribe((loaded) -> {
			this.loaded = loaded;
		});
		endLoading = 0;
	}

	@Override
	public void initialized() throws InterruptedException {
		previousZoomValue = StaticVars.windowZoomFunction.apply(StaticVars.windowZoom.getLastValue());
		StaticVars.windowZoom.onNext(1f);
	}

	@Override
	public void beforeRender(float dt) {
		loadingTextTranslation = GraphicUtils.sinDeg(endLoading * 90f) * 10f;

		endLoading += dt;
		if (loaded && (StaticVars.debugOn || endLoading >= 3.5f)) {
			StaticVars.windowZoom.onNext(previousZoomValue);
			HardwareDevice.INSTANCE.getDisplayManager().setScreen(new MathInputScreen());
		}
		mustRefresh = true;
	}

	@Override
	public void render() {
		HardwareDevice.INSTANCE.getDisplayManager().guiSkin.use(HardwareDevice.INSTANCE.getDisplayManager().engine);
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glColor3i(255, 255, 255);
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glFillRect(StaticVars.screenSize[0] / 2f - 80, StaticVars.screenSize[1] / 2f - 64, 160, 48, 0, 32, 160, 48);
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glFillRect(StaticVars.screenSize[0] / 2f - 24, StaticVars.screenSize[1] / 2f - loadingTextTranslation, 48, 48, 160, 32, 48, 48);

		HardwareDevice.INSTANCE.getDisplayManager().renderer.glFillRect(StaticVars.screenSize[0] - 224, StaticVars.screenSize[1] - 48, 224, 48, 0, 80, 224, 48);
		HardwareDevice.INSTANCE.getDisplayManager().renderer.glFillRect(StaticVars.screenSize[0] - 160 - 24 - 224, StaticVars.screenSize[1] - 48, 160, 48, 224, 80, 160, 48);

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

}
