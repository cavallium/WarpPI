package it.cavallium.warppi.gui.screens;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.gui.RenderContext;
import it.cavallium.warppi.gui.ScreenContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;

/** Debug-only launcher for destructive tests and diagnostic screens. */
public final class DebugMenuScreen extends Screen {
	private static final int BACKGROUND_COLOR = 0xFFF1EFE5;
	private static final int HEADER_COLOR = 0xFF342746;
	private static final int CARD_COLOR = 0xFFFFFFFF;
	private static final int TEXT_COLOR = 0xFF202631;
	private static final int MUTED_TEXT_COLOR = 0xFF667080;
	private static final int PAGE_PADDING = 8;
	private static final int HEADER_HEIGHT = 25;
	private static final int SECTION_LABEL_TOP = 31;
	private static final int CARD_TOP = 43;
	private static final int CARD_HEIGHT = 49;
	private static final int CARD_GAP = 5;
	private static final int BACKGROUND_LOAD_MILLIS = 2_000;

	enum DebugAction {
		CRASH_RENDER("CRASH UI RENDER", "THROW ON THE NEXT FRAME", "ERR", 0xFFB3261E),
		BACKGROUND_LOAD("BACKGROUND LOAD", "SHOW STATUS ICON FOR 2 SECONDS", "LD", 0xFFC07824),
		KEYBOARD_DIAGNOSTICS("KEYBOARD MATRIX", "RAW GPIO AND DEBOUNCE", "KEY", 0xFF2D8B73),
		UI_GPU_DIAGNOSTICS("UI / GPU", "FRAMES PRIMITIVES AND ATLASES", "GPU", 0xFF6354B5);

		private final String label;
		private final String description;
		private final String badge;
		private final int accentColor;

		DebugAction(final String label, final String description,
				final String badge, final int accentColor) {
			this.label = label;
			this.description = description;
			this.badge = badge;
			this.accentColor = accentColor;
		}

		String label() {
			return label;
		}

		String description() {
			return description;
		}

		String badge() {
			return badge;
		}

		int accentColor() {
			return accentColor;
		}
	}

	private static final List<DebugAction> ACTIONS = List.of(DebugAction.values());

	private final int backgroundLoadMillis;
	private final Consumer<Thread> workerStarter;
	private volatile boolean mustRefresh = true;
	private volatile boolean crashOnNextRender;
	private volatile boolean backgroundLoadRunning;
	private int selectedIndex;

	public DebugMenuScreen() {
		this(BACKGROUND_LOAD_MILLIS, DebugMenuScreen::startWorker);
	}

	DebugMenuScreen(final int backgroundLoadMillis, final Consumer<Thread> workerStarter) {
		if (backgroundLoadMillis < 0)
			throw new IllegalArgumentException("Background load duration must not be negative");
		this.backgroundLoadMillis = backgroundLoadMillis;
		this.workerStarter = Objects.requireNonNull(workerStarter);
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
		if (crashOnNextRender) {
			crashOnNextRender = false;
			throw new AssertionError("Deliberate UI render-loop crash from Debug Tools");
		}

		final Renderer renderer = ctx.getRenderer();
		renderer.glClearSkin();
		renderer.glColor(HEADER_COLOR);
		renderer.glFillColor(0, 0, ctx.getWidth(), HEADER_HEIGHT);

		d.fonts[0].use(d.display);
		renderer.glColor(0xFFFFFFFF);
		renderer.glDrawStringLeft(8, 8, "DEBUG TOOLS");
		d.fonts[1].use(d.display);
		renderer.glColor(0xFFE7DDF2);
		renderer.glDrawStringRight(ctx.getWidth() - 8, 9, ACTIONS.size() + " ACTIONS");

		renderer.glColor(MUTED_TEXT_COLOR);
		renderer.glDrawStringLeft(PAGE_PADDING, SECTION_LABEL_TOP,
				"DESTRUCTIVE TESTS AND DIAGNOSTICS");
		for (int index = 0; index < ACTIONS.size(); index++)
			drawActionCard(renderer, ctx.getWidth(), index, ACTIONS.get(index));

		renderer.glColor(backgroundLoadRunning ? 0xFF9A6418 : 0xFF555555);
		renderer.glDrawStringCenter(ctx.getWidth() / 2f, ctx.getHeight() - 10,
				backgroundLoadRunning
						? "BACKGROUND LOAD ACTIVE   STATUS ICON VISIBLE"
						: "1-4 QUICK RUN   UP/DOWN SELECT   BACK CLOSE");
	}

	private void drawActionCard(final Renderer renderer, final int screenWidth,
			final int index, final DebugAction action) {
		final boolean selected = index == selectedIndex;
		final int x = PAGE_PADDING;
		final int y = CARD_TOP + index * (CARD_HEIGHT + CARD_GAP);
		final int width = screenWidth - PAGE_PADDING * 2;
		renderer.glColor(selected ? action.accentColor() : CARD_COLOR);
		renderer.glFillColor(x, y, width, CARD_HEIGHT);
		renderer.glColor(action.accentColor());
		renderer.glFillColor(x, y, 4, CARD_HEIGHT);

		final int badgeX = x + 10;
		final int badgeY = y + 10;
		renderer.glColor(selected ? 0xFFFFFFFF : action.accentColor());
		renderer.glFillColor(badgeX, badgeY, 29, 29);
		d.fonts[1].use(d.display);
		renderer.glColor(selected ? action.accentColor() : 0xFFFFFFFF);
		renderer.glDrawStringCenter(badgeX + 14.5f, badgeY + 10, action.badge());

		d.fonts[0].use(d.display);
		renderer.glColor(selected ? 0xFFFFFFFF : TEXT_COLOR);
		renderer.glDrawStringLeft(x + 48, y + 7, action.label());
		d.fonts[1].use(d.display);
		renderer.glColor(selected ? 0xFFF4F0F8 : MUTED_TEXT_COLOR);
		final String description = action == DebugAction.BACKGROUND_LOAD && backgroundLoadRunning
				? "RUNNING   STATUS ICON VISIBLE" : action.description();
		renderer.glDrawStringLeft(x + 48, y + 27, description);

		final int shortcutX = x + width - 24;
		renderer.glColor(selected ? 0xFFFFFFFF : 0xFFE8EDF3);
		renderer.glFillColor(shortcutX, y + 7, 16, 16);
		renderer.glColor(selected ? action.accentColor() : TEXT_COLOR);
		renderer.glDrawStringCenter(shortcutX + 8, y + 12, Integer.toString(index + 1));
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
			case LEFT:
				moveSelection(-1);
				return true;
			case DOWN:
			case RIGHT:
				moveSelection(1);
				return true;
			case OK:
			case STEP:
			case SIMPLIFY:
				activateSelected();
				return true;
			case NUM1:
			case NUM2:
			case NUM3:
			case NUM4:
				selectedIndex = shortcutIndex(event.getKey());
				activateSelected();
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

	private void moveSelection(final int direction) {
		selectedIndex = nextSelection(selectedIndex, direction);
		mustRefresh = true;
	}

	private void activateSelected() {
		final DebugAction action = ACTIONS.get(selectedIndex);
		switch (action) {
			case CRASH_RENDER -> armRenderCrash();
			case BACKGROUND_LOAD -> triggerBackgroundLoad();
			case KEYBOARD_DIAGNOSTICS, UI_GPU_DIAGNOSTICS -> d.setScreen(createScreen(action));
		}
	}

	void armRenderCrash() {
		crashOnNextRender = true;
		mustRefresh = true;
		if (d != null)
			d.requestRefresh();
	}

	synchronized void triggerBackgroundLoad() {
		if (backgroundLoadRunning)
			return;
		final Thread worker = new Thread(this::runBackgroundLoad);
		WarpPI.INSTANCE.beginLoadingOperation();
		backgroundLoadRunning = true;
		mustRefresh = true;
		try {
			workerStarter.accept(worker);
		} catch (final RuntimeException | Error failure) {
			finishBackgroundLoad();
			throw failure;
		}
	}

	private void runBackgroundLoad() {
		try {
			Thread.sleep(backgroundLoadMillis);
		} catch (final InterruptedException interrupted) {
			Thread.currentThread().interrupt();
		} finally {
			finishBackgroundLoad();
		}
	}

	private static void startWorker(final Thread worker) {
		WarpPI.getPlatform().setThreadName(worker, "Debug background load");
		WarpPI.getPlatform().setThreadDaemon(worker, true);
		worker.start();
	}

	private synchronized void finishBackgroundLoad() {
		if (!backgroundLoadRunning)
			return;
		backgroundLoadRunning = false;
		mustRefresh = true;
		WarpPI.INSTANCE.endLoadingOperation();
		if (d != null)
			d.requestRefresh();
	}

	static List<DebugAction> actions() {
		return ACTIONS;
	}

	boolean isBackgroundLoadRunning() {
		return backgroundLoadRunning;
	}

	static int nextSelection(final int selectedIndex, final int direction) {
		return Math.floorMod(selectedIndex + Integer.signum(direction), ACTIONS.size());
	}

	static int shortcutIndex(final Key key) {
		return switch (key) {
			case NUM1 -> 0;
			case NUM2 -> 1;
			case NUM3 -> 2;
			case NUM4 -> 3;
			default -> -1;
		};
	}

	static Screen createScreen(final DebugAction action) {
		return switch (action) {
			case KEYBOARD_DIAGNOSTICS -> new KeyboardDebugScreen();
			case UI_GPU_DIAGNOSTICS -> new UIDemoScreen();
			case CRASH_RENDER, BACKGROUND_LOAD ->
					throw new IllegalArgumentException(action + " does not open a screen");
		};
	}

	@Override
	public String getSessionTitle() {
		return "Debug tools";
	}
}
