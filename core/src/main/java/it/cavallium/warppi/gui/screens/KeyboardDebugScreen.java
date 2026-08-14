package it.cavallium.warppi.gui.screens;

import it.cavallium.warppi.device.input.HardwareKeyboardLayout;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.Layer;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalKey;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalRow;
import it.cavallium.warppi.device.input.Keyboard;
import it.cavallium.warppi.device.input.KeyboardMatrixDiagnostics.Snapshot;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.gui.HistoryBehavior;
import it.cavallium.warppi.gui.RenderContext;
import it.cavallium.warppi.gui.ScreenContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;

/**
 * Live electrical and debounce diagnostic for the physical 8-by-8 key matrix.
 * Press every switch after clearing the counters: raw-only cells point to a
 * contact/debounce problem, while cells with no raw activity point upstream to
 * the switch, PCB trace, row input, column drive, or shift-register wiring.
 */
public final class KeyboardDebugScreen extends Screen {
	private static final int MATRIX_SIZE = Byte.SIZE;
	private static final int PHYSICAL_KEY_COUNT = 52;
	private static final long REFRESH_INTERVAL_NANOS = 80_000_000L;

	private static final int BACKGROUND_COLOR = 0xFFF1EFE5;
	private static final int HEADER_COLOR = 0xFF20252B;
	private static final int HEADER_TEXT_COLOR = 0xFFF7F4E8;
	private static final int PANEL_COLOR = 0xFFE3E0D5;
	private static final int GRID_COLOR = 0xFF625F58;
	private static final int UNUSED_COLOR = 0xFFD4D1C8;
	private static final int UNSEEN_COLOR = 0xFFFAF9F3;
	private static final int RAW_ONLY_COLOR = 0xFFFFD28A;
	private static final int ACCEPTED_COLOR = 0xFFC9DCF5;
	private static final int RAW_HELD_COLOR = 0xFFFFA63D;
	private static final int ACCEPTED_HELD_COLOR = 0xFF65C77A;
	private static final int TEXT_COLOR = 0xFF292B2D;
	private static final int MUTED_TEXT_COLOR = 0xFF6E6A62;
	private static final int ALERT_TEXT_COLOR = 0xFF9C3D16;
	private static final int OK_TEXT_COLOR = 0xFF17612B;

	private static final PhysicalKey[] PHYSICAL_KEYS = indexPhysicalKeys();

	private long nextRefreshNanos;
	private long rateBaselineNanos;
	private long rateBaselineScans;
	private int scanRate;

	public KeyboardDebugScreen() {
		historyBehavior = HistoryBehavior.DONT_KEEP_IN_HISTORY;
	}

	@Override
	public void created() throws InterruptedException {}

	@Override
	public void initialized() throws InterruptedException {}

	@Override
	public void graphicInitialized(final ScreenContext ctx) throws InterruptedException {}

	@Override
	public int getBackgroundColor() {
		return BACKGROUND_COLOR;
	}

	@Override
	public void render(final RenderContext ctx) {
		final Renderer renderer = ctx.getRenderer();
		d.fonts[0].use(d.display);
		final Snapshot snapshot = Keyboard.matrixDiagnosticsSnapshot();
		updateScanRate(snapshot);

		drawHeader(renderer, ctx.getWidth(), snapshot);

		final int footerY = ctx.getHeight() - 13;
		final int gridLeft = 18;
		final int sideWidth = Math.max(142, ctx.getWidth() * 3 / 10);
		final int gridRight = ctx.getWidth() - sideWidth - 8;
		final int gridTop = 36;
		final int gridBottom = footerY - 3;
		final int cellWidth = Math.max(20, (gridRight - gridLeft) / MATRIX_SIZE);
		final int cellHeight = Math.max(18, (gridBottom - gridTop) / MATRIX_SIZE);

		drawMatrix(renderer, snapshot, gridLeft, gridTop, cellWidth, cellHeight);
		drawHealthPanel(renderer, snapshot, gridRight + 5, 29,
				ctx.getWidth() - gridRight - 8, gridBottom - 29);
		drawFooter(renderer, ctx.getWidth(), footerY);
	}

	private void drawHeader(final Renderer renderer, final int width, final Snapshot snapshot) {
		renderer.glColor(HEADER_COLOR);
		renderer.glFillColor(0, 0, width, 23);
		renderer.glColor(HEADER_TEXT_COLOR);
		renderer.glDrawStringLeft(4, 3, "KEYBOARD MATRIX DIAGNOSTIC");
		renderer.glDrawStringRight(width - 4, 3,
				snapshot.scannerRunning() ? "GPIO LIVE " + scanRate + " Hz" : "GPIO SCANNER IDLE");
		renderer.glColor(MUTED_TEXT_COLOR);
		renderer.glDrawStringLeft(4, 24, "RAW INPUT -> 3-SAMPLE DEBOUNCE -> LOGICAL KEY");
	}

	private void drawMatrix(final Renderer renderer, final Snapshot snapshot, final int left,
			final int top, final int cellWidth, final int cellHeight) {
		for (int column = 0; column < MATRIX_SIZE; column++) {
			renderer.glColor(MUTED_TEXT_COLOR);
			renderer.glDrawStringCenter(left + column * cellWidth + cellWidth / 2, top - 10,
					"C" + (column + 1));
		}

		for (int row = 0; row < MATRIX_SIZE; row++) {
			renderer.glColor(MUTED_TEXT_COLOR);
			renderer.glDrawStringRight(left - 2, top + row * cellHeight + Math.max(1, cellHeight / 2 - 4),
					"R" + (row + 1));
			for (int column = 0; column < MATRIX_SIZE; column++) {
				final int x = left + column * cellWidth;
				final int y = top + row * cellHeight;
				drawMatrixCell(renderer, snapshot, x, y, cellWidth, cellHeight, row, column);
			}
		}
	}

	private void drawMatrixCell(final Renderer renderer, final Snapshot snapshot, final int x, final int y,
			final int width, final int height, final int row, final int column) {
		final PhysicalKey key = physicalKeyAt(row, column);
		final SignalState state = signalState(snapshot, row, column);
		renderer.glColor(colorFor(state));
		renderer.glFillColor(x + 1, y + 1, width - 2, height - 2);
		renderer.glColor(GRID_COLOR);
		drawBorder(renderer, x, y, width, height);

		if (key == null) {
			renderer.glColor(MUTED_TEXT_COLOR);
			renderer.glDrawStringCenter(x + width / 2, y + Math.max(2, height / 2 - 4), "--");
			return;
		}

		final boolean lastRaw = snapshot.lastRawRow() == row && snapshot.lastRawColumn() == column;
		if (lastRaw) {
			renderer.glColor(TEXT_COLOR);
			drawBorder(renderer, x + 2, y + 2, width - 4, height - 4);
		}

		renderer.glColor(state == SignalState.RAW_ONLY ? ALERT_TEXT_COLOR : TEXT_COLOR);
		renderer.glDrawStringCenter(x + width / 2, y + 2, key.switchLabel());
		final int accepted = snapshot.acceptedPressCount(row, column);
		final int raw = snapshot.rawPressCount(row, column);
		renderer.glColor(state == SignalState.ACCEPTED || state == SignalState.ACCEPTED_HELD
				? OK_TEXT_COLOR : MUTED_TEXT_COLOR);
		renderer.glDrawStringCenter(x + width / 2, y + Math.max(11, height - 10),
				accepted > 0 ? "OK" + compactCount(accepted) : raw > 0 ? "R" + compactCount(raw) : "-");
	}

	private void drawHealthPanel(final Renderer renderer, final Snapshot snapshot, final int x, final int y,
			final int width, final int height) {
		renderer.glColor(PANEL_COLOR);
		renderer.glFillColor(x, y, width, height);
		renderer.glColor(GRID_COLOR);
		drawBorder(renderer, x, y, width, height);

		int textY = y + 5;
		renderer.glColor(TEXT_COLOR);
		renderer.glDrawStringLeft(x + 5, textY, "SWEEP HEALTH");
		textY += 14;

		final int accepted = acceptedPhysicalSwitchCount(snapshot);
		final int rawOnly = rawOnlyPhysicalSwitchCount(snapshot);
		renderer.glColor(accepted == PHYSICAL_KEY_COUNT ? OK_TEXT_COLOR : TEXT_COLOR);
		renderer.glDrawStringLeft(x + 5, textY, "ACCEPTED " + accepted + "/" + PHYSICAL_KEY_COUNT);
		textY += 12;
		renderer.glColor(rawOnly == 0 ? MUTED_TEXT_COLOR : ALERT_TEXT_COLOR);
		renderer.glDrawStringLeft(x + 5, textY, "RAW ONLY " + rawOnly);
		textY += 17;

		renderer.glColor(TEXT_COLOR);
		renderer.glDrawStringLeft(x + 5, textY, "LAST RAW SIGNAL");
		textY += 13;
		if (snapshot.lastRawChange() < 0) {
			renderer.glColor(MUTED_TEXT_COLOR);
			renderer.glDrawStringLeft(x + 5, textY, "PRESS A SWITCH");
			drawLegend(renderer, x + 5, y + height - 57);
			return;
		}

		final int row = snapshot.lastRawRow();
		final int column = snapshot.lastRawColumn();
		final PhysicalKey key = physicalKeyAt(row, column);
		final String switchLabel = key == null ? switchLabel(row, column) : key.switchLabel();
		renderer.glColor(snapshot.lastRawPressed() ? OK_TEXT_COLOR : MUTED_TEXT_COLOR);
		renderer.glDrawStringLeft(x + 5, textY,
				switchLabel + " " + (snapshot.lastRawPressed() ? "DOWN" : "UP"));
		textY += 12;
		renderer.glColor(TEXT_COLOR);
		renderer.glDrawStringLeft(x + 5, textY, "ROW " + (row + 1) + "  COL " + (column + 1));
		textY += 12;
		if (key != null) {
			renderer.glDrawStringLeft(x + 5, textY, "MAP " + key.compactLabel(Layer.NORMAL));
			textY += 12;
		}
		final int rawCount = snapshot.rawPressCount(row, column);
		final int acceptedCount = snapshot.acceptedPressCount(row, column);
		renderer.glDrawStringLeft(x + 5, textY, "RAW " + rawCount + "  OK " + acceptedCount);
		textY += 14;
		if (acceptedCount > 0) {
			renderer.glColor(OK_TEXT_COLOR);
			renderer.glDrawStringLeft(x + 5, textY, "DEBOUNCE PASS");
		} else {
			renderer.glColor(ALERT_TEXT_COLOR);
			renderer.glDrawStringLeft(x + 5, textY, "RAW / CONTACT");
		}

		drawLegend(renderer, x + 5, y + height - 57);
	}

	private void drawLegend(final Renderer renderer, final int x, final int y) {
		renderer.glColor(MUTED_TEXT_COLOR);
		renderer.glDrawStringLeft(x, y, "COLOR KEY");
		drawLegendItem(renderer, x, y + 13, RAW_ONLY_COLOR, "RAW ONLY");
		drawLegendItem(renderer, x, y + 26, ACCEPTED_COLOR, "ACCEPTED");
		drawLegendItem(renderer, x, y + 39, ACCEPTED_HELD_COLOR, "HELD");
	}

	private void drawLegendItem(final Renderer renderer, final int x, final int y, final int color,
			final String label) {
		renderer.glColor(color);
		renderer.glFillColor(x, y + 1, 9, 8);
		renderer.glColor(TEXT_COLOR);
		renderer.glDrawStringLeft(x + 13, y, label);
	}

	private void drawFooter(final Renderer renderer, final int width, final int y) {
		renderer.glColor(HEADER_COLOR);
		renderer.glFillColor(0, y, width, 13);
		renderer.glColor(HEADER_TEXT_COLOR);
		renderer.glDrawStringLeft(4, y + 2, "F1: CLEAR COUNTERS");
		renderer.glDrawStringCenter(width / 2, y + 2, "PRESS EVERY SWITCH ONCE");
		renderer.glDrawStringRight(width - 4, y + 2, "APPS/BACK: CLOSE");
	}

	private void updateScanRate(final Snapshot snapshot) {
		if (rateBaselineNanos == 0 || snapshot.scanCount() < rateBaselineScans) {
			rateBaselineNanos = snapshot.capturedNanos();
			rateBaselineScans = snapshot.scanCount();
			scanRate = 0;
			return;
		}
		final long elapsed = snapshot.capturedNanos() - rateBaselineNanos;
		if (elapsed < 250_000_000L)
			return;
		final long scans = snapshot.scanCount() - rateBaselineScans;
		scanRate = (int) Math.min(Integer.MAX_VALUE, Math.round(scans * 1_000_000_000d / elapsed));
		rateBaselineNanos = snapshot.capturedNanos();
		rateBaselineScans = snapshot.scanCount();
	}

	@Override
	public void beforeRender(final ScreenContext ctx, final float dt) {}

	@Override
	public boolean mustBeRefreshed() {
		final long now = System.nanoTime();
		if (now < nextRefreshNanos)
			return false;
		nextRefreshNanos = now + REFRESH_INTERVAL_NANOS;
		return true;
	}

	@Override
	public boolean onKeyPressed(final KeyPressedEvent event) {
		if (event.getKey() == Key.SETTINGS || event.getKey() == Key.BACK) {
			d.closeScreen();
			return true;
		}
		if (event.getKey() == Key.F1) {
			Keyboard.clearMatrixDiagnostics();
			rateBaselineNanos = 0;
			nextRefreshNanos = 0;
		}
		return true;
	}

	@Override
	public boolean onKeyReleased(final KeyReleasedEvent event) {
		return true;
	}

	@Override
	public String getSessionTitle() {
		return "Keyboard matrix diagnostic";
	}

	static SignalState signalState(final Snapshot snapshot, final int row, final int column) {
		if (physicalKeyAt(row, column) == null)
			return SignalState.UNUSED;
		if (snapshot.isAcceptedPressed(row, column))
			return SignalState.ACCEPTED_HELD;
		if (snapshot.isRawPressed(row, column))
			return SignalState.RAW_HELD;
		if (snapshot.hasAcceptedActivity(row, column))
			return SignalState.ACCEPTED;
		if (snapshot.hasRawActivity(row, column))
			return SignalState.RAW_ONLY;
		return SignalState.UNSEEN;
	}

	static int acceptedPhysicalSwitchCount(final Snapshot snapshot) {
		int count = 0;
		for (int row = 0; row < MATRIX_SIZE; row++)
			for (int column = 0; column < MATRIX_SIZE; column++)
				if (physicalKeyAt(row, column) != null && snapshot.hasAcceptedActivity(row, column))
					count++;
		return count;
	}

	static int rawOnlyPhysicalSwitchCount(final Snapshot snapshot) {
		int count = 0;
		for (int row = 0; row < MATRIX_SIZE; row++)
			for (int column = 0; column < MATRIX_SIZE; column++)
				if (physicalKeyAt(row, column) != null
						&& snapshot.hasRawActivity(row, column)
						&& !snapshot.hasAcceptedActivity(row, column))
					count++;
		return count;
	}

	static PhysicalKey physicalKeyAt(final int row, final int column) {
		if (row < 0 || row >= MATRIX_SIZE || column < 0 || column >= MATRIX_SIZE)
			throw new IllegalArgumentException("Matrix coordinates must be between 0 and 7");
		return PHYSICAL_KEYS[row * MATRIX_SIZE + column];
	}

	private static int colorFor(final SignalState state) {
		return switch (state) {
			case UNUSED -> UNUSED_COLOR;
			case UNSEEN -> UNSEEN_COLOR;
			case RAW_ONLY -> RAW_ONLY_COLOR;
			case ACCEPTED -> ACCEPTED_COLOR;
			case RAW_HELD -> RAW_HELD_COLOR;
			case ACCEPTED_HELD -> ACCEPTED_HELD_COLOR;
		};
	}

	private static void drawBorder(final Renderer renderer, final int x, final int y, final int width,
			final int height) {
		renderer.glDrawLine(x, y, x + width, y);
		renderer.glDrawLine(x + width, y, x + width, y + height);
		renderer.glDrawLine(x + width, y + height, x, y + height);
		renderer.glDrawLine(x, y + height, x, y);
	}

	private static String compactCount(final int count) {
		return count > 99 ? "+" : Integer.toString(count);
	}

	private static String switchLabel(final int row, final int column) {
		return "S" + (row + 1) + (column + 1);
	}

	private static PhysicalKey[] indexPhysicalKeys() {
		final PhysicalKey[] keys = new PhysicalKey[MATRIX_SIZE * MATRIX_SIZE];
		int count = 0;
		for (final PhysicalRow row : HardwareKeyboardLayout.rows()) {
			for (final PhysicalKey key : row.keys()) {
				final int index = key.matrixRow() * MATRIX_SIZE + key.matrixColumn();
				if (keys[index] != null)
					throw new IllegalStateException("Duplicate physical key at " + key.switchLabel());
				keys[index] = key;
				count++;
			}
		}
		if (count != PHYSICAL_KEY_COUNT)
			throw new IllegalStateException("Expected " + PHYSICAL_KEY_COUNT + " physical keys, found " + count);
		return keys;
	}

	enum SignalState {
		UNUSED,
		UNSEEN,
		RAW_ONLY,
		ACCEPTED,
		RAW_HELD,
		ACCEPTED_HELD
	}
}
