package it.cavallium.warppi.device.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.device.input.HardwareKeyboardLayout.Layer;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalKey;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalRow;

class HardwareKeyboardLayoutTest {

	@Test
	void modelsPhysicalRowsRatherThanAnElectricalGrid() {
		final List<PhysicalRow> rows = HardwareKeyboardLayout.rows();

		assertEquals(List.of(7, 7, 7, 7, 7, 5, 5, 5, 5),
				rows.stream().map(PhysicalRow::slotCount).toList());
		assertEquals(List.of(5, 7, 6, 7, 7, 5, 5, 5, 5),
				rows.stream().map(row -> row.keys().size()).toList());
		assertEquals(List.of("0:0", "0:1", "0:3", "0:5", "0:6"), positions(rows.getFirst()));
		assertEquals(List.of("4:7", "3:7", "2:7", "1:7", "0:7"), positions(rows.getLast()));
	}

	@Test
	void everyPhysicalSwitchHasOneUniqueMatrixPosition() {
		final Set<String> positions = new HashSet<>();
		for (final PhysicalRow row : HardwareKeyboardLayout.rows()) {
			for (final PhysicalKey key : row.keys()) {
				assertTrue(positions.add(key.matrixRow() + ":" + key.matrixColumn()));
			}
		}

		assertEquals(52, positions.size());
		assertFalse(positions.contains("0:2"));
		assertFalse(positions.contains("2:2"));
		assertFalse(positions.contains("5:7"));
	}

	@Test
	void exposesNormalShiftAndAltAlphaMeaningsWithoutFallback() {
		final PhysicalKey brightness = find(0, 6);
		assertEquals("BRI+", brightness.label(Layer.NORMAL));
		assertEquals("BRI-", brightness.label(Layer.SHIFT));
		assertEquals("ZOOM", brightness.label(Layer.ALT_ALPHA));

		final PhysicalKey bottomZero = find(4, 7);
		assertEquals("0", bottomZero.label(Layer.NORMAL));
		assertEquals("-", bottomZero.label(Layer.SHIFT));
		assertEquals("X", bottomZero.label(Layer.ALT_ALPHA));
	}

	@Test
	void allPhysicalKeyLabelsFitTheCompactGuide() {
		for (final PhysicalRow row : HardwareKeyboardLayout.rows()) {
			for (final PhysicalKey key : row.keys()) {
				for (final Layer layer : Layer.values()) {
					assertTrue(key.label(layer).length() <= 8,
							() -> key + " has an oversized " + layer + " label: " + key.label(layer));
				}
			}
		}
	}

	private static PhysicalKey find(final int matrixRow, final int matrixColumn) {
		return HardwareKeyboardLayout.rows().stream()
				.flatMap(row -> row.keys().stream())
				.filter(key -> key.matrixRow() == matrixRow && key.matrixColumn() == matrixColumn)
				.findFirst()
				.orElseThrow();
	}

	private static List<String> positions(final PhysicalRow row) {
		return row.keys().stream()
				.map(key -> key.matrixRow() + ":" + key.matrixColumn())
				.toList();
	}
}
