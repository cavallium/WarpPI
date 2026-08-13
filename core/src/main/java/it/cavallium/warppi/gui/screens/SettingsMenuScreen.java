package it.cavallium.warppi.gui.screens;

import java.util.Objects;

import it.cavallium.warppi.Platform.Settings;
import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.gui.RenderContext;
import it.cavallium.warppi.gui.ScreenContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;

/** Persistent calculator settings accessible from the apps launcher. */
public final class SettingsMenuScreen extends Screen {
	private static final int BACKGROUND_COLOR = 0xFFF1EFE5;
	private static final int HEADER_COLOR = 0xFF2D4059;
	private static final int SELECTED_COLOR = 0xFF4F81BD;
	private static final int ROW_TOP = 48;
	private static final int ROW_HEIGHT = 42;

	private final Settings settings;
	private int selectedIndex;
	private boolean mustRefresh = true;

	public SettingsMenuScreen() {
		this(WarpPI.getPlatform().getSettings());
	}

	SettingsMenuScreen(final Settings settings) {
		this.settings = Objects.requireNonNull(settings);
	}

	@Override
	public void created() {
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
		for (int index = 0; index < 2; index++) {
			final int y = ROW_TOP + index * ROW_HEIGHT;
			renderer.glColor(index == selectedIndex ? SELECTED_COLOR : 0xFFFFFFFF);
			renderer.glFillColor(10, y, ctx.getWidth() - 20, ROW_HEIGHT - 5);
		}

		d.fonts[0].use(d.display);
		renderer.glColor(0xFFFFFFFF);
		renderer.glDrawStringLeft(8, 8, "SETTINGS");
		renderer.glDrawStringRight(ctx.getWidth() - 8, 8, "UP/DOWN + LEFT/RIGHT");
		drawRow(renderer, ctx.getWidth(), 0, "DEBUG UI",
				settings.isDebugEnabled() ? "ENABLED" : "DISABLED");
		drawRow(renderer, ctx.getWidth(), 1, "BRIGHTNESS",
				Math.round(settings.getBrightness() * 100d) + "%");
		renderer.glColor(0xFF555555);
		renderer.glDrawStringCenter(ctx.getWidth() / 2f, ctx.getHeight() - 12,
				"APPS/BACK: CLOSE");
	}

	private void drawRow(final Renderer renderer, final int width, final int index,
			final String label, final String value) {
		final int y = ROW_TOP + index * ROW_HEIGHT + 12;
		renderer.glColor(index == selectedIndex ? 0xFFFFFFFF : 0xFF252525);
		renderer.glDrawStringLeft(20, y, label);
		renderer.glDrawStringRight(width - 20, y, value);
	}

	@Override
	public void beforeRender(final ScreenContext ctx, final float dt) {
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
				selectedIndex = Math.floorMod(selectedIndex - 1, 2);
				mustRefresh = true;
				return true;
			case DOWN:
				selectedIndex = (selectedIndex + 1) % 2;
				mustRefresh = true;
				return true;
			case LEFT:
				changeSelected(-1);
				return true;
			case RIGHT:
			case OK:
				changeSelected(1);
				return true;
			case BRIGHTNESS_CYCLE:
				setBrightness(settings.getBrightness() + 0.1d);
				return true;
			case BRIGHTNESS_CYCLE_REVERSE:
				setBrightness(settings.getBrightness() - 0.1d);
				return true;
			case SETTINGS:
			case BACK:
				d.closeScreen();
				return true;
			default:
				return false;
		}
	}

	private void changeSelected(final int direction) {
		if (selectedIndex == 0)
			settings.setDebugEnabled(!settings.isDebugEnabled());
		else
			setBrightness(settings.getBrightness() + direction * 0.1d);
		mustRefresh = true;
	}

	private void setBrightness(final double requestedBrightness) {
		final double brightness = Math.max(0d, Math.min(1d,
				Math.round(requestedBrightness * 10d) / 10d));
		if (d == null)
			settings.setBrightness(brightness);
		else
			d.setBrightness((float) brightness);
		mustRefresh = true;
	}

	@Override
	public boolean onKeyReleased(final KeyReleasedEvent event) {
		return false;
	}

	@Override
	public String getSessionTitle() {
		return "Settings";
	}
}
