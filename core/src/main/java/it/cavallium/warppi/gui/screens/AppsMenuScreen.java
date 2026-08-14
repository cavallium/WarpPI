package it.cavallium.warppi.gui.screens;

import java.util.List;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.event.Key;
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
	private static final int HEADER_COLOR = 0xFF223247;
	private static final int CARD_COLOR = 0xFFFFFFFF;
	private static final int TEXT_COLOR = 0xFF202631;
	private static final int MUTED_TEXT_COLOR = 0xFF667080;
	private static final int PAGE_PADDING = 8;
	private static final int HEADER_HEIGHT = 25;
	private static final int HERO_TOP = 31;
	private static final int HERO_HEIGHT = 44;
	private static final int GRID_TOP = 84;
	private static final int GRID_GAP = 6;
	private static final int GRID_ROW_GAP = 5;
	private static final int FOOTER_HEIGHT = 24;

	enum App {
		CALCULATOR("CALCULATOR", "MAIN APP  MATH & EQUATIONS", "123", 0xFF2F78C4),
		TETRIS("TETRIS", "GAME", "TET", 0xFF6354B5),
		MARIO("MARIO", "GAME", "M", 0xFFD45B45),
		KEYBOARD_GUIDE("KEYBOARD", "GUIDE", "KEY", 0xFF2D8B73),
		SETTINGS("SETTINGS", "SYSTEM", "SET", 0xFF64748B),
		DEBUG_TOOLS("DEBUG TOOLS", "TESTS & DIAGNOSTICS", "DBG", 0xFF8B4E9F);

		private final String title;
		private final String subtitle;
		private final String badge;
		private final int accentColor;

		App(final String title, final String subtitle, final String badge,
				final int accentColor) {
			this.title = title;
			this.subtitle = subtitle;
			this.badge = badge;
			this.accentColor = accentColor;
		}

		String label() {
			return title;
		}

		String subtitle() {
			return subtitle;
		}

		String badge() {
			return badge;
		}

		int accentColor() {
			return accentColor;
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
		renderer.glFillColor(0, 0, ctx.getWidth(), HEADER_HEIGHT);

		drawHeroCard(renderer, apps.get(0), selectedIndex == 0,
				ctx.getWidth() - PAGE_PADDING * 2, 1);
		final int gridItemCount = apps.size() - 1;
		final int rowCount = (gridItemCount + 1) / 2;
		final int availableGridHeight = ctx.getHeight() - GRID_TOP - FOOTER_HEIGHT;
		final int cardHeight = Math.min(50,
				(availableGridHeight - GRID_ROW_GAP * (rowCount - 1)) / rowCount);
		final int cardWidth = (ctx.getWidth() - PAGE_PADDING * 2 - GRID_GAP) / 2;
		for (int index = 1; index < apps.size(); index++) {
			final int gridIndex = index - 1;
			final int column = gridIndex % 2;
			final int row = gridIndex / 2;
			final int x = PAGE_PADDING + column * (cardWidth + GRID_GAP);
			final int y = GRID_TOP + row * (cardHeight + GRID_ROW_GAP);
			drawGridCard(renderer, apps.get(index), index == selectedIndex,
					x, y, cardWidth, cardHeight, index + 1);
		}

		d.fonts[0].use(d.display);
		renderer.glColor(0xFFFFFFFF);
		renderer.glDrawStringLeft(8, 8, "APPS");
		d.fonts[1].use(d.display);
		renderer.glColor(0xFFD9E3EF);
		renderer.glDrawStringRight(ctx.getWidth() - 8, 9,
				apps.size() + " APPS  1-" + apps.size() + " OPEN"
						+ (debugEnabled ? "  DEBUG" : ""));
		renderer.glColor(0xFF555555);
		renderer.glDrawStringCenter(ctx.getWidth() / 2f, ctx.getHeight() - 10,
				"1-" + apps.size() + " QUICK OPEN   ARROWS MOVE   BACK CLOSE");
	}

	private void drawHeroCard(final Renderer renderer, final App app,
			final boolean selected, final int width, final int shortcutNumber) {
		final int x = PAGE_PADDING;
		final int textColor = selected ? 0xFFFFFFFF : TEXT_COLOR;
		renderer.glColor(selected ? app.accentColor() : CARD_COLOR);
		renderer.glFillColor(x, HERO_TOP, width, HERO_HEIGHT);
		renderer.glColor(app.accentColor());
		renderer.glFillColor(x, HERO_TOP, 5, HERO_HEIGHT);

		final int badgeX = x + 12;
		final int badgeY = HERO_TOP + 7;
		renderer.glColor(selected ? 0xFFFFFFFF : app.accentColor());
		renderer.glFillColor(badgeX, badgeY, 30, 30);
		d.fonts[0].use(d.display);
		renderer.glColor(selected ? app.accentColor() : 0xFFFFFFFF);
		renderer.glDrawStringCenter(badgeX + 15, badgeY + 10, app.badge());

		renderer.glColor(textColor);
		renderer.glDrawStringLeft(x + 52, HERO_TOP + 8, app.label());
		d.fonts[1].use(d.display);
		renderer.glColor(selected ? 0xFFE8F3FF : MUTED_TEXT_COLOR);
		renderer.glDrawStringLeft(x + 52, HERO_TOP + 25, app.subtitle());
		drawShortcutTag(renderer, app, selected,
				x + width - 20, HERO_TOP + 6, shortcutNumber);
	}

	private void drawGridCard(final Renderer renderer, final App app,
			final boolean selected, final int x, final int y,
			final int width, final int height, final int shortcutNumber) {
		renderer.glColor(selected ? app.accentColor() : CARD_COLOR);
		renderer.glFillColor(x, y, width, height);
		renderer.glColor(app.accentColor());
		renderer.glFillColor(x, y, 4, height);

		final int badgeSize = 22;
		final int badgeX = x + 9;
		final int badgeY = y + (height - badgeSize) / 2;
		renderer.glColor(selected ? 0xFFFFFFFF : app.accentColor());
		renderer.glFillColor(badgeX, badgeY, badgeSize, badgeSize);
		d.fonts[1].use(d.display);
		renderer.glColor(selected ? app.accentColor() : 0xFFFFFFFF);
		renderer.glDrawStringCenter(badgeX + badgeSize / 2f, badgeY + 8, app.badge());

		d.fonts[0].use(d.display);
		renderer.glColor(selected ? 0xFFFFFFFF : TEXT_COLOR);
		renderer.glDrawStringLeft(x + 38, y + height / 2f - 9, app.label());
		d.fonts[1].use(d.display);
		renderer.glColor(selected ? 0xFFF4F7FB : MUTED_TEXT_COLOR);
		renderer.glDrawStringLeft(x + 38, y + height / 2f + 5, app.subtitle());
		drawShortcutTag(renderer, app, selected,
				x + width - 19, y + 5, shortcutNumber);
	}

	private void drawShortcutTag(final Renderer renderer, final App app,
			final boolean selected, final int x, final int y,
			final int shortcutNumber) {
		renderer.glColor(selected ? 0xFFFFFFFF : 0xFFE8EDF3);
		renderer.glFillColor(x, y, 14, 14);
		d.fonts[1].use(d.display);
		renderer.glColor(selected ? app.accentColor() : TEXT_COLOR);
		renderer.glDrawStringCenter(x + 7, y + 4, Integer.toString(shortcutNumber));
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
			case DOWN:
			case LEFT:
			case RIGHT:
				moveSelection(event.getKey());
				return true;
			case OK:
			case STEP:
			case SIMPLIFY:
				launchSelectedApp();
				return true;
			case NUM1:
			case NUM2:
			case NUM3:
			case NUM4:
			case NUM5:
			case NUM6:
				final int shortcutIndex = shortcutIndex(event.getKey());
				if (shortcutIndex < apps.size()) {
					selectedIndex = shortcutIndex;
					launchSelectedApp();
				}
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

	private void moveSelection(final Key direction) {
		final int nextIndex = nextSelection(selectedIndex, apps.size(), direction);
		if (selectedIndex != nextIndex) {
			selectedIndex = nextIndex;
			mustRefresh = true;
		}
	}

	private void launchSelectedApp() {
		final App app = apps.get(selectedIndex);
		if (app == App.CALCULATOR && d.currentSession >= 0
				&& d.currentSession + 1 < d.sessions.length
				&& d.sessions[d.currentSession] == this
				&& d.sessions[d.currentSession + 1] instanceof MathInputScreen) {
			d.closeScreen();
			return;
		}
		d.setScreen(createScreen(app));
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
				: List.of(App.CALCULATOR, App.TETRIS, App.MARIO,
						App.KEYBOARD_GUIDE, App.SETTINGS);
	}

	static int nextSelection(final int selectedIndex, final int appCount,
			final Key direction) {
		if (appCount <= 1)
			return 0;
		return switch (direction) {
			case UP -> selectedIndex == 0 ? appCount - 1
					: selectedIndex <= 2 ? 0 : selectedIndex - 2;
			case DOWN -> selectedIndex == 0 ? 1
					: selectedIndex + 2 < appCount ? selectedIndex + 2 : 0;
			case LEFT -> selectedIndex > 0 && selectedIndex % 2 == 0
					? selectedIndex - 1 : selectedIndex;
			case RIGHT -> selectedIndex > 0 && selectedIndex % 2 == 1
						&& selectedIndex + 1 < appCount
					? selectedIndex + 1 : selectedIndex;
			default -> selectedIndex;
		};
	}

	static int shortcutIndex(final Key key) {
		return switch (key) {
			case NUM1 -> 0;
			case NUM2 -> 1;
			case NUM3 -> 2;
			case NUM4 -> 3;
			case NUM5 -> 4;
			case NUM6 -> 5;
			default -> -1;
		};
	}

	static Screen createScreen(final App app) {
		return switch (app) {
			case CALCULATOR -> new MathInputScreen();
			case KEYBOARD_GUIDE -> new HardwareKeyboardGuideScreen();
			case TETRIS -> new TetrisScreen();
			case MARIO -> new MarioScreen();
			case SETTINGS -> new SettingsMenuScreen();
			case DEBUG_TOOLS -> new DebugMenuScreen();
		};
	}

	@Override
	public String getSessionTitle() {
		return "Apps";
	}
}
