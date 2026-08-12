package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.util.Objects;

import it.cavallium.warppi.device.input.HardwareKeyboardLayout;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.Layer;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalKey;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalRow;
import it.cavallium.warppi.device.input.Keyboard;
import it.cavallium.warppi.event.Key;

/** Mouse-driven keypad rendered below a non-production JOGL display. */
final class JOGLKeyboardPanel {
	static final int LOGICAL_HEIGHT = 264;

	private static final int TOP_INSET = 4;
	private static final int KEY_HEIGHT = 25;
	private static final int ROW_STEP = 28;
	private static final int SECTION_GAP = 3;
	private static final float PREFERRED_SLOT_WIDTH = 52f;
	private static final float PREFERRED_KEY_INSET = 2f;

	@FunctionalInterface
	interface KeyTransition {
		void accept(int matrixRow, int matrixColumn, boolean released);
	}

	private final KeyTransition keyTransition;
	private volatile Layer layer;
	private volatile PhysicalKey pressedKey;

	JOGLKeyboardPanel(final Layer initialLayer) {
		this(initialLayer, Keyboard::keyRaw);
	}

	JOGLKeyboardPanel(final Layer initialLayer, final KeyTransition keyTransition) {
		layer = Objects.requireNonNull(initialLayer, "initialLayer");
		this.keyTransition = Objects.requireNonNull(keyTransition, "keyTransition");
	}

	void setLayer(final Layer layer) {
		this.layer = Objects.requireNonNull(layer, "layer");
	}

	synchronized void press(final float x, final float y, final int width) {
		if (pressedKey != null)
			return;
		final PhysicalKey key = keyAt(x, y, width);
		if (key == null)
			return;
		pressedKey = key;
		try {
			keyTransition.accept(key.matrixRow(), key.matrixColumn(), false);
		} catch (final RuntimeException | Error exception) {
			pressedKey = null;
			throw exception;
		}
	}

	synchronized void release() {
		final PhysicalKey key = pressedKey;
		if (key == null)
			return;
		pressedKey = null;
		keyTransition.accept(key.matrixRow(), key.matrixColumn(), true);
	}

	void render(final JOGLRenderer renderer, final int width, final JOGLFont font) {
		renderer.initDrawCycle();
		renderer.glColor3i(18, 18, 18);
		renderer.glFillColor(0, 0, width, LOGICAL_HEIGHT);

		for (int rowIndex = 0; rowIndex < HardwareKeyboardLayout.rows().size(); rowIndex++) {
			final PhysicalRow row = HardwareKeyboardLayout.rows().get(rowIndex);
			final float y = rowTop(rowIndex);
			for (final PhysicalKey key : row.keys()) {
				final float x = keyLeft(width, row, key.slot());
				final float keyWidth = keyWidth(width);
				final float border = Math.min(1f, keyWidth / 4f);
				setBorderColor(renderer, key);
				renderer.glFillColor(x, y, keyWidth, KEY_HEIGHT);
				setKeyColor(renderer, key);
				renderer.glFillColor(x + border, y + border,
						keyWidth - 2 * border, KEY_HEIGHT - 2 * border);
			}
		}
		renderer.endDrawCycle();

		if (font == null || !font.isInitialized() || font.texture == null)
			return;

		renderer.initDrawCycle();
		renderer.currentFont = font;
		renderer.useTexture(font.texture, font.textureW, font.textureH);
		for (int rowIndex = 0; rowIndex < HardwareKeyboardLayout.rows().size(); rowIndex++) {
			final PhysicalRow row = HardwareKeyboardLayout.rows().get(rowIndex);
			final float y = rowTop(rowIndex) + (KEY_HEIGHT - font.charH) / 2f;
			for (final PhysicalKey key : row.keys()) {
				final Key mapping = key.mapping(layer);
				if (mapping == Key.NONE)
					continue;
				setTextColor(renderer);
				final float centerX = keyLeft(width, row, key.slot()) + keyWidth(width) / 2f;
				renderer.glDrawStringCenter(centerX, y, panelLabelFor(mapping));
			}
		}
		renderer.endDrawCycle();
	}

	/**
	 * Readable, fixed-width ASCII captions for the on-screen keypad. Captions are
	 * kept short so they remain clear inside the narrow physical-key layout.
	 */
	static String panelLabelFor(final Key key) {
		return switch (Objects.requireNonNull(key, "key")) {
			case SHIFT -> "SHF";
			case HISTORY_BACK -> "H-";
			case HISTORY_FORWARD -> "H+";
			case SURD_MODE -> "S/D";
			case SIMPLIFY -> "SIMP";
			case RESET -> "CLR";
			case LEFT -> "<";
			case RIGHT -> ">";
			case UP -> "^";
			case DOWN -> "v";
			case SETTINGS -> "MENU";
			default -> {
				final String compact = HardwareKeyboardLayout.compactLabelFor(key);
				yield compact.length() <= 4 ? compact : compact.substring(0, 4);
			}
		};
	}

	private void setBorderColor(final JOGLRenderer renderer, final PhysicalKey key) {
		if (key == pressedKey) {
			renderer.glColor3i(245, 245, 240);
		} else if (key.isActiveModifier(layer)) {
			if (layer == Layer.SHIFT)
				renderer.glColor3i(33, 150, 243);
			else
				renderer.glColor3i(244, 67, 54);
		} else {
			renderer.glColor3i(82, 82, 78);
		}
	}

	private void setKeyColor(final JOGLRenderer renderer, final PhysicalKey key) {
		if (key == pressedKey) {
			renderer.glColor3i(174, 174, 168);
		} else if (key.isActiveModifier(layer)) {
			if (layer == Layer.SHIFT)
				renderer.glColor3i(151, 190, 238);
			else
				renderer.glColor3i(239, 166, 177);
		} else {
			switch (layer) {
				case NORMAL -> renderer.glColor3i(232, 230, 220);
				case SHIFT -> renderer.glColor3i(218, 232, 249);
				case ALT_ALPHA -> renderer.glColor3i(249, 222, 227);
			}
		}
	}

	private void setTextColor(final JOGLRenderer renderer) {
		switch (layer) {
			case NORMAL -> renderer.glColor3i(25, 25, 23);
			case SHIFT -> renderer.glColor3i(20, 82, 170);
			case ALT_ALPHA -> renderer.glColor3i(175, 35, 50);
		}
	}

	static int surfaceHeight(final int totalSurfaceHeight) {
		if (totalSurfaceHeight <= 1)
			return 0;
		return Math.min(LOGICAL_HEIGHT,
				Math.min(totalSurfaceHeight - 1, Math.max(1, Math.round(totalSurfaceHeight * 0.46f))));
	}

	static PhysicalKey keyAt(final float x, final float y, final int width) {
		if (x < 0 || x >= width || y < 0 || y >= LOGICAL_HEIGHT)
			return null;
		for (int rowIndex = 0; rowIndex < HardwareKeyboardLayout.rows().size(); rowIndex++) {
			final float rowTop = rowTop(rowIndex);
			if (y < rowTop || y >= rowTop + KEY_HEIGHT)
				continue;
			final PhysicalRow row = HardwareKeyboardLayout.rows().get(rowIndex);
			final float slotWidth = slotWidth(width);
			final float relativeX = x - rowLeft(width, row);
			final int slot = (int) Math.floor(relativeX / slotWidth);
			if (slot < 0 || slot >= row.slotCount())
				return null;
			final float keyLeft = keyLeft(width, row, slot);
			if (x < keyLeft || x >= keyLeft + keyWidth(width))
				return null;
			return row.keyAtSlot(slot);
		}
		return null;
	}

	static float slotCenterX(final int width, final PhysicalRow row, final int slot) {
		return rowLeft(width, row) + (slot + 0.5f) * slotWidth(width);
	}

	static float rowCenterY(final int rowIndex) {
		return rowTop(rowIndex) + KEY_HEIGHT / 2f;
	}

	private static float rowTop(final int rowIndex) {
		return TOP_INSET + rowIndex * ROW_STEP
				+ (rowIndex >= 5 ? SECTION_GAP : 0)
				+ (rowIndex >= 8 ? SECTION_GAP : 0);
	}

	private static float rowLeft(final int width, final PhysicalRow row) {
		return (width - row.slotCount() * slotWidth(width)) / 2f;
	}

	private static float keyLeft(final int width, final PhysicalRow row, final int slot) {
		return rowLeft(width, row) + slot * slotWidth(width) + keyInset(width);
	}

	private static float keyWidth(final int width) {
		return slotWidth(width) - 2 * keyInset(width);
	}

	private static float slotWidth(final int width) {
		return Math.min(PREFERRED_SLOT_WIDTH, Math.max(1, width) / 7f);
	}

	private static float keyInset(final int width) {
		return Math.min(PREFERRED_KEY_INSET, slotWidth(width) / 8f);
	}
}
