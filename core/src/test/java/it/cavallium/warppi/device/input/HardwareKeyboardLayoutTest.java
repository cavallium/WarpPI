package it.cavallium.warppi.device.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.device.input.HardwareKeyboardLayout.Layer;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalKey;
import it.cavallium.warppi.device.input.HardwareKeyboardLayout.PhysicalRow;
import it.cavallium.warppi.event.Key;

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
	void exposesIntentionalPhysicalGapsBySlot() {
		final PhysicalRow topRow = HardwareKeyboardLayout.rows().getFirst();
		assertEquals(find(0, 0), topRow.keyAtSlot(0));
		assertNull(topRow.keyAtSlot(2));
		assertNull(topRow.keyAtSlot(4));
		assertThrows(IllegalArgumentException.class, () -> topRow.keyAtSlot(-1));
		assertThrows(IllegalArgumentException.class, () -> topRow.keyAtSlot(topRow.slotCount()));
	}

	@Test
	void exposesNormalShiftAndAltAlphaMeaningsWithoutFallback() {
		final PhysicalKey brightness = find(0, 6);
		assertEquals("BRI+", brightness.compactLabel(Layer.NORMAL));
		assertEquals("BRI-", brightness.compactLabel(Layer.SHIFT));
		assertEquals("ZOOM", brightness.label(Layer.ALT_ALPHA));

		final PhysicalKey bottomZero = find(4, 7);
		assertEquals("0", bottomZero.label(Layer.NORMAL));
		assertEquals("Rnd", bottomZero.label(Layer.SHIFT));
		assertEquals("x", bottomZero.label(Layer.ALT_ALPHA));
		assertEquals(Key.ROUND, bottomZero.mapping(Layer.SHIFT));
	}

	@Test
	void derivesConstantTimeMatrixLookupFromPhysicalKeys() {
		assertEquals(Key.PLUS, HardwareKeyboardLayout.mappingAt(7, 3, false, false));
		assertEquals(Key.PLUS_MINUS, HardwareKeyboardLayout.mappingAt(7, 3, true, false));
		assertEquals(Key.NONE, HardwareKeyboardLayout.mappingAt(7, 3, false, true));
		assertEquals(Key.EULER_NUMBER, HardwareKeyboardLayout.mappingAt(2, 7, false, true));
		assertEquals(Key.NONE, HardwareKeyboardLayout.mappingAt(0, 2, false, false));
		assertThrows(IllegalArgumentException.class,
				() -> HardwareKeyboardLayout.mappingAt(8, 0, false, false));
	}

	@Test
	void exposesCasioInspiredScientificLayers() {
		assertMappings(find(1, 5), Key.HISTORY_BACK, Key.LESS_THAN, Key.LESS_OR_EQUAL);
		assertMappings(find(1, 6), Key.HISTORY_FORWARD, Key.GREATER_THAN, Key.GREATER_OR_EQUAL);
		assertMappings(find(2, 5), Key.LOGARITHM, Key.POWER_OF_10, Key.NONE);
		assertMappings(find(2, 6), Key.COMMON_LOGARITHM, Key.POWER_OF_E, Key.NATURAL_LOGARITHM);
		assertMappings(find(3, 1), Key.FRACTION, Key.MIXED_FRACTION, Key.LETTER_A);
		assertEquals("□\n─\n□", find(3, 1).label(Layer.NORMAL));
		assertEquals("a □\n  ─\n  □", find(3, 1).label(Layer.SHIFT));
		assertMappings(find(3, 2), Key.POWER_OF_x, Key.RECIPROCAL, Key.LETTER_B);
		assertMappings(find(3, 3), Key.POWER_OF_2, Key.POWER_OF_3, Key.LETTER_C);
		assertMappings(find(3, 4), Key.SINE, Key.ARCSINE, Key.LETTER_D);
		assertMappings(find(3, 5), Key.COSINE, Key.ARCCOSINE, Key.LETTER_E);
		assertMappings(find(3, 6), Key.TANGENT, Key.ARCTANGENT, Key.LETTER_F);
		assertMappings(find(4, 1), Key.NEGATIVE, Key.NONE, Key.LETTER_Z);
		assertMappings(find(4, 4), Key.DEGREE_MINUTE_SECOND, Key.NONE, Key.NONE);
		assertMappings(find(4, 6), Key.ABSOLUTE_VALUE, Key.FACTORIAL, Key.FACTORIZE);
		assertMappings(find(4, 7), Key.NUM0, Key.ROUND, Key.LETTER_X);
		assertMappings(find(3, 7), Key.DOT, Key.RANDOM_INTEGER, Key.LETTER_Y);
		assertMappings(find(2, 7), Key.EXPONENTIAL_NOTATION, Key.PI, Key.EULER_NUMBER);
		assertMappings(find(1, 7), Key.ANSWER, Key.PERCENT, Key.DRG_CYCLE);
		assertMappings(find(0, 7), Key.SIMPLIFY, Key.STEP, Key.EQUAL);
	}

	@Test
	void appsKeyRemainsAvailableInEveryLayer() {
		final PhysicalKey apps = find(0, 5);
		for (final Layer layer : Layer.values())
			assertEquals("APPS", apps.label(layer));
		assertEquals("APPS", Keyboard.getKeyName(0, 5, false, false));
		assertEquals("APPS", Keyboard.getKeyName(0, 5, true, false));
		assertEquals("APPS", Keyboard.getKeyName(0, 5, false, true));
	}

	@Test
	void identifiesPcbSwitchesAndOnlyTheActiveModifier() {
		final PhysicalKey shift = find(0, 0);
		final PhysicalKey alpha = find(0, 1);

		assertEquals("S11", shift.switchLabel());
		assertEquals("S12", alpha.switchLabel());
		assertEquals("S58", find(4, 7).switchLabel());
		assertTrue(shift.isActiveModifier(Layer.SHIFT));
		assertFalse(shift.isActiveModifier(Layer.NORMAL));
		assertFalse(shift.isActiveModifier(Layer.ALT_ALPHA));
		assertTrue(alpha.isActiveModifier(Layer.ALT_ALPHA));
		assertFalse(alpha.isActiveModifier(Layer.SHIFT));
	}

	@Test
	void allPhysicalKeyLabelsFitTheCompactGuide() {
		for (final PhysicalRow row : HardwareKeyboardLayout.rows()) {
			for (final PhysicalKey key : row.keys()) {
				for (final Layer layer : Layer.values()) {
					assertTrue(key.compactLabel(layer).length() <= 8,
							() -> key + " has an oversized " + layer + " label: " + key.compactLabel(layer));
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

	private static void assertMappings(final PhysicalKey key, final Key normal,
			final Key shift, final Key altAlpha) {
		assertEquals(normal, key.mapping(Layer.NORMAL));
		assertEquals(shift, key.mapping(Layer.SHIFT));
		assertEquals(altAlpha, key.mapping(Layer.ALT_ALPHA));
	}

	private static List<String> positions(final PhysicalRow row) {
		return row.keys().stream()
				.map(key -> key.matrixRow() + ":" + key.matrixColumn())
				.toList();
	}
}
