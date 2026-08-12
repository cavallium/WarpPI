package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.device.input.HardwareKeyboardLayout;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.Layer;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalKey;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalRow;

class JOGLKeyboardPanelTest {
	private static final int WIDTH = 480;

	@Test
	void hitTestingPreservesPhysicalGapsAndBottomRowWiring() {
		final PhysicalRow top = HardwareKeyboardLayout.rows().getFirst();
		final PhysicalRow bottom = HardwareKeyboardLayout.rows().getLast();

		assertEquals(find(0, 0), at(top, 0, 0));
		assertNull(at(top, 2, 0));
		assertEquals(find(0, 3), at(top, 3, 0));
		assertEquals(find(4, 7), at(bottom, 0, 8));
		assertEquals(find(0, 7), at(bottom, 4, 8));
	}

	@Test
	void releasesExactlyThePhysicalKeyThatWasPressed() {
		final List<String> transitions = new ArrayList<>();
		final JOGLKeyboardPanel panel = new JOGLKeyboardPanel(Layer.NORMAL,
				(row, column, released) -> transitions.add(row + ":" + column + ":" + released));
		final PhysicalRow top = HardwareKeyboardLayout.rows().getFirst();

		panel.press(JOGLKeyboardPanel.slotCenterX(WIDTH, top, 0), JOGLKeyboardPanel.rowCenterY(0), WIDTH);
		panel.press(JOGLKeyboardPanel.slotCenterX(WIDTH, top, 1), JOGLKeyboardPanel.rowCenterY(0), WIDTH);
		panel.release();
		panel.release();

		assertEquals(List.of("0:0:false", "0:0:true"), transitions);
	}

	@Test
	void keyboardViewportKeepsTheDisplayUsableWhenWindowShrinks() {
		assertEquals(JOGLKeyboardPanel.LOGICAL_HEIGHT, JOGLKeyboardPanel.surfaceHeight(584));
		assertEquals(138, JOGLKeyboardPanel.surfaceHeight(300));
		assertEquals(1, JOGLKeyboardPanel.surfaceHeight(2));
		assertEquals(0, JOGLKeyboardPanel.surfaceHeight(1));
	}

	@Test
	void joglEngineRetainsLayerChangesBeforeItsWindowExists() {
		final JOGLEngine engine = new JOGLEngine(480, 320);

		engine.keyboardLayerChanged(Layer.ALT_ALPHA);

		assertEquals(Layer.ALT_ALPHA, engine.getKeyboardLayer());
		assertThrows(NullPointerException.class, () -> engine.keyboardLayerChanged(null));
		assertEquals(Layer.ALT_ALPHA, engine.getKeyboardLayer());
	}

	private static PhysicalKey at(final PhysicalRow row, final int slot, final int rowIndex) {
		return JOGLKeyboardPanel.keyAt(
				JOGLKeyboardPanel.slotCenterX(WIDTH, row, slot),
				JOGLKeyboardPanel.rowCenterY(rowIndex), WIDTH);
	}

	private static PhysicalKey find(final int matrixRow, final int matrixColumn) {
		return HardwareKeyboardLayout.rows().stream()
				.flatMap(row -> row.keys().stream())
				.filter(key -> key.matrixRow() == matrixRow && key.matrixColumn() == matrixColumn)
				.findFirst()
				.orElseThrow();
	}
}
