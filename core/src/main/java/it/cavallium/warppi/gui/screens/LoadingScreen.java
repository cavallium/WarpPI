package it.cavallium.warppi.gui.screens;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.StaticVars;
import it.cavallium.warppi.gui.GraphicUtils;

public class LoadingScreen extends Screen {

	public float endLoading;
	boolean mustRefresh = true;
	public float loadingTextTranslation = 0.0f;
	public boolean loaded = false;
	private float previousZoomValue = 1;
	private volatile boolean ended = false;

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
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().getHUD().hide();
		StaticVars.windowZoom.onNext(1f);
	}

	@Override
	public void beforeRender(final float dt) {
		loadingTextTranslation = GraphicUtils.sinDeg(endLoading * 90f) * 10f;

		endLoading += dt;
		if (!ended && loaded && (Engine.getPlatform().getSettings().isDebugEnabled() || endLoading >= 3.5f)) {
			ended = true;
			StaticVars.windowZoom.onNext(previousZoomValue);
			Engine.INSTANCE.getHardwareDevice().getDisplayManager().getHUD().show();
			Engine.INSTANCE.getHardwareDevice().getDisplayManager().setScreen(new MathInputScreen());
		}
		mustRefresh = true;
	}

	@Override
	public void render() {
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().guiSkin.use(Engine.INSTANCE.getHardwareDevice().getDisplayManager().engine);
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glColor3i(255, 255, 255);
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glFillRect(StaticVars.screenSize[0] / 2f - 80, StaticVars.screenSize[1] / 2f - 64, 160, 48, 0, 32, 160, 48);
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glFillRect(StaticVars.screenSize[0] / 2f - 24, StaticVars.screenSize[1] / 2f - loadingTextTranslation, 48, 48, 160, 32, 48, 48);

		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glFillRect(StaticVars.screenSize[0] - 224, StaticVars.screenSize[1] - 48, 224, 48, 0, 80, 224, 48);
		Engine.INSTANCE.getHardwareDevice().getDisplayManager().renderer.glFillRect(StaticVars.screenSize[0] - 160 - 24 - 224, StaticVars.screenSize[1] - 48, 160, 48, 224, 80, 160, 48);

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
