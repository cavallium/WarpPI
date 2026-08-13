package it.cavallium.warppi.gui.screens;

import java.util.List;
import java.util.Objects;

import it.cavallium.warppi.Platform;
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
	private static final int HEADER_COLOR = 0xFF223247;
	private static final int CARD_COLOR = 0xFFFFFFFF;
	private static final int TEXT_COLOR = 0xFF202631;
	private static final int MUTED_TEXT_COLOR = 0xFF667080;
	private static final int PAGE_PADDING = 8;
	private static final int HEADER_HEIGHT = 25;
	private static final int SECTION_LABEL_TOP = 34;
	private static final int CARD_TOP = 47;
	private static final int CARD_HEIGHT = 58;
	private static final int CARD_GAP = 7;
	private static final double DEFAULT_BRIGHTNESS = 0.2d;

	enum SettingItem {
		DEBUG_UI("DEBUG UI", "DIAGNOSTIC TOOLS & OVERLAYS", "DBG", 0xFF2D8B73),
		BRIGHTNESS("BRIGHTNESS", "DISPLAY BACKLIGHT", "SUN", 0xFFC07824);

		private final String label;
		private final String description;
		private final String badge;
		private final int accentColor;

		SettingItem(final String label, final String description,
				final String badge, final int accentColor) {
			this.label = label;
			this.description = description;
			this.badge = badge;
			this.accentColor = accentColor;
		}
	}

	private final Settings settings;
	private final boolean brightnessSupported;
	private final List<SettingItem> items;
	private int selectedIndex;
	private boolean mustRefresh = true;

	public SettingsMenuScreen() {
		this(WarpPI.getPlatform());
	}

	private SettingsMenuScreen(final Platform platform) {
		this(platform.getSettings(),
				platform.getBacklightOutputDevice().supportsBrightnessControl());
	}

	SettingsMenuScreen(final Settings settings) {
		this(settings, true);
	}

	SettingsMenuScreen(final Settings settings, final boolean brightnessSupported) {
		this.settings = Objects.requireNonNull(settings);
		this.brightnessSupported = brightnessSupported;
		items = availableSettings(brightnessSupported);
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
		renderer.glFillColor(0, 0, ctx.getWidth(), HEADER_HEIGHT);

		d.fonts[0].use(d.display);
		renderer.glColor(0xFFFFFFFF);
		renderer.glDrawStringLeft(8, 8, "SETTINGS");
		d.fonts[1].use(d.display);
		renderer.glColor(0xFFD9E3EF);
		renderer.glDrawStringRight(ctx.getWidth() - 8, 9,
				items.size() + (items.size() == 1 ? " SETTING" : " SETTINGS"));

		renderer.glColor(MUTED_TEXT_COLOR);
		renderer.glDrawStringLeft(PAGE_PADDING, SECTION_LABEL_TOP,
				"DEVICE PREFERENCES");
		for (int index = 0; index < items.size(); index++)
			drawCard(renderer, ctx.getWidth(), index, items.get(index));

		renderer.glColor(0xFF555555);
		renderer.glDrawStringCenter(ctx.getWidth() / 2f, ctx.getHeight() - 10,
				"UP/DOWN SELECT   LEFT/RIGHT CHANGE   BACK CLOSE");
	}

	private void drawCard(final Renderer renderer, final int screenWidth,
			final int index, final SettingItem item) {
		final boolean selected = index == selectedIndex;
		final int x = PAGE_PADDING;
		final int y = CARD_TOP + index * (CARD_HEIGHT + CARD_GAP);
		final int width = screenWidth - PAGE_PADDING * 2;
		final int textColor = selected ? 0xFFFFFFFF : TEXT_COLOR;

		renderer.glColor(selected ? item.accentColor : CARD_COLOR);
		renderer.glFillColor(x, y, width, CARD_HEIGHT);
		renderer.glColor(item.accentColor);
		renderer.glFillColor(x, y, 4, CARD_HEIGHT);

		final int badgeX = x + 10;
		final int badgeY = y + 15;
		renderer.glColor(selected ? 0xFFFFFFFF : item.accentColor);
		renderer.glFillColor(badgeX, badgeY, 26, 26);
		d.fonts[1].use(d.display);
		renderer.glColor(selected ? item.accentColor : 0xFFFFFFFF);
		renderer.glDrawStringCenter(badgeX + 13, badgeY + 9, item.badge);

		d.fonts[0].use(d.display);
		renderer.glColor(textColor);
		renderer.glDrawStringLeft(x + 46, y + 9, item.label);
		d.fonts[1].use(d.display);
		renderer.glColor(selected ? 0xFFF4F7FB : MUTED_TEXT_COLOR);
		renderer.glDrawStringLeft(x + 46, y + 28, item.description);
		drawValueTag(renderer, x + width, y, item, selected);
		if (item == SettingItem.BRIGHTNESS)
			drawBrightnessBar(renderer, x, y, width, selected, item.accentColor);
	}

	private void drawValueTag(final Renderer renderer, final int right, final int y,
			final SettingItem item, final boolean selected) {
		final String value = switch (item) {
			case DEBUG_UI -> settings.isDebugEnabled() ? "ON" : "OFF";
			case BRIGHTNESS -> brightnessPercent(settings.getBrightness()) + "%";
		};
		final int tagWidth = item == SettingItem.BRIGHTNESS ? 43 : 32;
		final int tagX = right - tagWidth - 10;
		renderer.glColor(selected ? 0xFFFFFFFF : 0xFFE8EDF3);
		renderer.glFillColor(tagX, y + 8, tagWidth, 16);
		d.fonts[1].use(d.display);
		renderer.glColor(selected ? item.accentColor : TEXT_COLOR);
		renderer.glDrawStringCenter(tagX + tagWidth / 2f, y + 13, value);
	}

	private void drawBrightnessBar(final Renderer renderer, final int x, final int y,
			final int width, final boolean selected, final int accentColor) {
		final int barX = x + 46;
		final int barY = y + 45;
		final int barWidth = width - 58;
		final int filledWidth = (barWidth * brightnessPercent(settings.getBrightness()) + 50) / 100;
		renderer.glColor(selected ? 0xFF8C672B : 0xFFD8DEE6);
		renderer.glFillColor(barX, barY, barWidth, 5);
		if (filledWidth > 0) {
			renderer.glColor(selected ? 0xFFFFFFFF : accentColor);
			renderer.glFillColor(barX, barY, filledWidth, 5);
		}
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
				moveSelection(-1);
				return true;
			case DOWN:
				moveSelection(1);
				return true;
			case LEFT:
				changeSelected(-1);
				return true;
			case RIGHT:
			case OK:
				changeSelected(1);
				return true;
			case BRIGHTNESS_CYCLE:
				adjustBrightness(1);
				return true;
			case BRIGHTNESS_CYCLE_REVERSE:
				adjustBrightness(-1);
				return true;
			case SETTINGS:
			case BACK:
				d.closeScreen();
				return true;
			default:
				return false;
		}
	}

	private void moveSelection(final int direction) {
		final int nextIndex = Math.floorMod(selectedIndex + direction, items.size());
		if (nextIndex == selectedIndex)
			return;
		selectedIndex = nextIndex;
		mustRefresh = true;
	}

	private void changeSelected(final int direction) {
		switch (items.get(selectedIndex)) {
			case DEBUG_UI:
				settings.setDebugEnabled(!settings.isDebugEnabled());
				mustRefresh = true;
				break;
			case BRIGHTNESS:
				adjustBrightness(direction);
				break;
		}
	}

	private void adjustBrightness(final int direction) {
		if (!brightnessSupported)
			return;
		final double currentBrightness = settings.getBrightness();
		final double brightness = adjustedBrightness(currentBrightness, direction);
		if (Double.compare(currentBrightness, brightness) == 0)
			return;
		if (d == null)
			settings.setBrightness(brightness);
		else
			d.setBrightness((float) brightness);
		mustRefresh = true;
	}

	static List<SettingItem> availableSettings(final boolean brightnessSupported) {
		return brightnessSupported
				? List.of(SettingItem.DEBUG_UI, SettingItem.BRIGHTNESS)
				: List.of(SettingItem.DEBUG_UI);
	}

	static double adjustedBrightness(final double currentBrightness, final int direction) {
		final double normalized = normalizedBrightness(currentBrightness);
		final int currentStep = (int) Math.round(normalized * 10d);
		final int requestedStep = currentStep + Integer.signum(direction);
		return Math.max(0, Math.min(10, requestedStep)) / 10d;
	}

	static int brightnessPercent(final double brightness) {
		return (int) Math.round(normalizedBrightness(brightness) * 100d);
	}

	private static double normalizedBrightness(final double brightness) {
		if (!Double.isFinite(brightness))
			return DEFAULT_BRIGHTNESS;
		return Math.max(0d, Math.min(1d, brightness));
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
