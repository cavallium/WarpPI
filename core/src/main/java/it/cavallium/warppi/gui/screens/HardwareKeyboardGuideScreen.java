package it.cavallium.warppi.gui.screens;

import it.cavallium.warppi.device.input.HardwareKeyboardLayout;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.Layer;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalKey;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalRow;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.gui.RenderContext;
import it.cavallium.warppi.gui.ScreenContext;
import it.cavallium.warppi.gui.graphicengine.Renderer;

/** Displays the active meaning of every key in the calculator's physical layout. */
public final class HardwareKeyboardGuideScreen extends Screen {
	private static final int BACKGROUND_COLOR = 0xFFF1EFE5;
	private static final int HEADER_HEIGHT = 23;
	private static final int ROW_HEIGHT = 28;
	private static final int KEY_HEIGHT = 25;

	private boolean mustRefresh = true;
	private Layer activeLayer = HardwareKeyboardLayout.activeLayer();

	@Override
	public void created() throws InterruptedException {}

	@Override
	public void initialized() throws InterruptedException {}

	@Override
	public void graphicInitialized(final ScreenContext ctx) throws InterruptedException {
		ctx.getGraphicEngine().getRenderer().glClearColor(BACKGROUND_COLOR);
	}

	@Override
	public void render(final RenderContext ctx) {
		final Renderer renderer = ctx.getRenderer();
		d.fonts[0].use(d.display);
		drawHeader(renderer, ctx.getWidth());

		int y = HEADER_HEIGHT;
		for (int rowIndex = 0; rowIndex < HardwareKeyboardLayout.rows().size(); rowIndex++) {
			if (rowIndex == 5 || rowIndex == 8)
				y += 2;
			drawRow(renderer, ctx.getWidth(), y, HardwareKeyboardLayout.rows().get(rowIndex));
			y += ROW_HEIGHT;
		}
	}

	private void drawHeader(final Renderer renderer, final int width) {
		renderer.glColor3i(35, 35, 32);
		renderer.glDrawStringLeft(3, 2, "KEYBOARD GUIDE");
		renderer.glDrawStringRight(width - 3, 2, "GUIDE/BACK: CLOSE");

		drawLegend(renderer, width / 4, Layer.NORMAL, "N NORMAL");
		drawLegend(renderer, width / 2, Layer.SHIFT, "S SHIFT");
		drawLegend(renderer, width * 3 / 4, Layer.ALT_ALPHA, "A ALT/ALPHA");
	}

	private void drawLegend(final Renderer renderer, final int centerX, final Layer layer, final String text) {
		if (layer == activeLayer) {
			setLayerBackgroundColor(renderer, layer);
			renderer.glFillColor(centerX - 40, 10, 80, 11);
		}
		setLayerTextColor(renderer, layer, true);
		renderer.glDrawStringCenter(centerX, 12, text);
	}

	private void drawRow(final Renderer renderer, final int width, final int y, final PhysicalRow row) {
		final int inset = row.slotCount() == 5 ? 6 : 3;
		for (int keyIndex = 0; keyIndex < row.keys().size(); keyIndex++) {
			final PhysicalKey key = row.keys().get(keyIndex);
			final int slotLeft = key.slot() * width / row.slotCount();
			final int slotRight = (key.slot() + 1) * width / row.slotCount();
			final int x = slotLeft + inset;
			final int keyWidth = slotRight - slotLeft - 2 * inset;
			drawKey(renderer, x, y, keyWidth, key);
		}
	}

	private void drawKey(final Renderer renderer, final int x, final int y, final int width, final PhysicalKey key) {
		final boolean activeModifier = key.isActiveModifier(activeLayer);
		if (key.mapping(Layer.NORMAL) == Key.NONE
				&& key.mapping(Layer.SHIFT) == Key.NONE
				&& key.mapping(Layer.ALT_ALPHA) == Key.NONE) {
			renderer.glColor3i(218, 216, 207);
		} else if (activeModifier) {
			setActiveModifierBackgroundColor(renderer, activeLayer);
		} else {
			setLayerBackgroundColor(renderer, activeLayer);
		}
		renderer.glFillColor(x + 1, y + 1, width - 2, KEY_HEIGHT - 2);

		if (activeModifier)
			setLayerTextColor(renderer, activeLayer, true);
		else
			renderer.glColor3i(65, 65, 60);
		renderer.glDrawLine(x, y, x + width, y);
		renderer.glDrawLine(x + width, y, x + width, y + KEY_HEIGHT);
		renderer.glDrawLine(x + width, y + KEY_HEIGHT, x, y + KEY_HEIGHT);
		renderer.glDrawLine(x, y + KEY_HEIGHT, x, y);

		renderer.glColor3i(95, 95, 88);
		renderer.glDrawStringLeft(x + 3, y + 2, key.switchLabel());
		setLayerTextColor(renderer, activeLayer, true);
		renderer.glDrawStringCenter(x + width / 2, y + 14, key.compactLabel(activeLayer));
	}

	private static void setLayerBackgroundColor(final Renderer renderer, final Layer layer) {
		switch (layer) {
			case NORMAL -> renderer.glColor3i(250, 249, 242);
			case SHIFT -> renderer.glColor3i(222, 234, 250);
			case ALT_ALPHA -> renderer.glColor3i(250, 226, 230);
		}
	}

	private static void setActiveModifierBackgroundColor(final Renderer renderer, final Layer layer) {
		switch (layer) {
			case NORMAL -> renderer.glColor3i(250, 249, 242);
			case SHIFT -> renderer.glColor3i(151, 190, 238);
			case ALT_ALPHA -> renderer.glColor3i(239, 166, 177);
		}
	}

	private static void setLayerTextColor(final Renderer renderer, final Layer layer, final boolean active) {
		switch (layer) {
			case NORMAL -> renderer.glColor3i(active ? 20 : 100, active ? 20 : 100, active ? 18 : 95);
			case SHIFT -> renderer.glColor3i(active ? 15 : 85, active ? 60 : 105, active ? 155 : 145);
			case ALT_ALPHA -> renderer.glColor3i(active ? 155 : 150, active ? 25 : 90, active ? 45 : 100);
		}
	}

	@Override
	public void beforeRender(final ScreenContext ctx, final float dt) {
		ctx.getGraphicEngine().getRenderer().glClearColor(BACKGROUND_COLOR);
		final Layer newActiveLayer = HardwareKeyboardLayout.activeLayer();
		if (activeLayer != newActiveLayer) {
			activeLayer = newActiveLayer;
			mustRefresh = true;
		}
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
		if (event.getKey() == Key.SETTINGS || event.getKey() == Key.BACK)
			d.closeScreen();
		mustRefresh = true;
		return true;
	}

	@Override
	public boolean onKeyReleased(final KeyReleasedEvent event) {
		return false;
	}

	@Override
	public String getSessionTitle() {
		return "Keyboard guide";
	}
}
