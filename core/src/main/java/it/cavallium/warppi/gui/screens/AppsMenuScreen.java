package it.cavallium.warppi.gui.screens;

import java.util.List;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.extra.mario.MarioScreen;
import it.cavallium.warppi.extra.tetris.TetrisScreen;
import it.cavallium.warppi.gui.RenderContext;
import it.cavallium.warppi.gui.ScreenContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;

/** Launcher opened by the calculator's APPS key. */
public final class AppsMenuScreen extends Screen {
	private static final int BACKGROUND_COLOR = 0xFFF1EFE5;
	private static final int HEADER_COLOR = 0xFF2D4059;
	private static final int SELECTED_COLOR = 0xFF4F81BD;
	private static final int ROW_HEIGHT = 32;
	private static final int ROW_TOP = 34;

	enum App {
		KEYBOARD_GUIDE("KEYBOARD GUIDE"),
		TETRIS("TETRIS"),
		MARIO("MARIO"),
		SETTINGS("SETTINGS"),
		KEYBOARD_DEBUG("KEYBOARD DIAGNOSTICS"),
		UI_DEMO("UI / GPU DEMO");

		private final String label;

		App(final String label) {
			this.label = label;
		}

		String label() {
			return label;
		}
	}

	private boolean mustRefresh = true;
	private boolean debugEnabled;
	private int selectedIndex;
	private List<App> apps = List.of();

	@Override
	public void created() {
		refreshApps();
	}

	@Override
	public void initialized() {
	}

	@Override
	public void graphicInitialized(final ScreenContext ctx) {
	}

	@Override
	public int getBackgroundColor() {
		return BACKGROUND_COLOR;
	}

	@Override
	public void render(final RenderContext ctx) {
		final Renderer renderer = ctx.getRenderer();
		renderer.glClearSkin();
		renderer.glColor(HEADER_COLOR);
		renderer.glFillColor(0, 0, ctx.getWidth(), 25);
		for (int index = 0; index < apps.size(); index++) {
			final int y = ROW_TOP + index * ROW_HEIGHT;
			renderer.glColor(index == selectedIndex ? SELECTED_COLOR : 0xFFFFFFFF);
			renderer.glFillColor(10, y, ctx.getWidth() - 20, ROW_HEIGHT - 4);
		}

		d.fonts[0].use(d.display);
		renderer.glColor(0xFFFFFFFF);
		renderer.glDrawStringLeft(8, 8, "APPS");
		renderer.glDrawStringRight(ctx.getWidth() - 8, 8, "UP/DOWN + OK");
		for (int index = 0; index < apps.size(); index++) {
			final int y = ROW_TOP + index * ROW_HEIGHT + 9;
			renderer.glColor(index == selectedIndex ? 0xFFFFFFFF : 0xFF252525);
			renderer.glDrawStringLeft(20, y, apps.get(index).label());
		}
		renderer.glColor(0xFF555555);
		renderer.glDrawStringCenter(ctx.getWidth() / 2f, ctx.getHeight() - 12,
				"APPS/BACK: CLOSE");
	}

	@Override
	public void beforeRender(final ScreenContext ctx, final float dt) {
		if (debugEnabled != WarpPI.getPlatform().getSettings().isDebugEnabled())
			refreshApps();
	}

	@Override
	public boolean mustBeRefreshed() {
		if (!mustRefresh)
			return false;
		mustRefresh = false;
		return true;
	}

	@Override
	public boolean onKeyPressed(final KeyPressedEvent event) {
		switch (event.getKey()) {
			case UP:
				moveSelection(-1);
				return true;
			case DOWN:
				moveSelection(1);
				return true;
			case OK:
			case STEP:
			case SIMPLIFY:
				d.setScreen(createScreen(apps.get(selectedIndex)));
				return true;
			case SETTINGS:
			case BACK:
				d.closeScreen();
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean onKeyReleased(final KeyReleasedEvent event) {
		return false;
	}

	private void moveSelection(final int delta) {
		selectedIndex = Math.floorMod(selectedIndex + delta, apps.size());
		mustRefresh = true;
	}

	private void refreshApps() {
		debugEnabled = WarpPI.getPlatform().getSettings().isDebugEnabled();
		apps = availableApps(debugEnabled);
		selectedIndex = Math.min(selectedIndex, apps.size() - 1);
		mustRefresh = true;
	}

	static List<App> availableApps(final boolean debugEnabled) {
		return debugEnabled
				? List.of(App.values())
				: List.of(App.KEYBOARD_GUIDE, App.TETRIS, App.MARIO, App.SETTINGS);
	}

	static Screen createScreen(final App app) {
		return switch (app) {
			case KEYBOARD_GUIDE -> new HardwareKeyboardGuideScreen();
			case TETRIS -> new TetrisScreen();
			case MARIO -> new MarioScreen();
			case SETTINGS -> new SettingsMenuScreen();
			case KEYBOARD_DEBUG -> new KeyboardDebugScreen();
			case UI_DEMO -> new UIDemoScreen();
		};
	}

	@Override
	public String getSessionTitle() {
		return "Apps";
	}
}
