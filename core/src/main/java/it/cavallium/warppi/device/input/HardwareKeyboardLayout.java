package it.cavallium.warppi.device.input;

import java.util.List;
import java.util.Objects;

import it.cavallium.warppi.event.Key;

/**
 * Physical key placement for the calculator keypad.
 *
 * <p>The keypad is electrically addressed as an 8-by-8 matrix, but it is not
 * physically an 8-by-8 grid. In particular, its top rows contain gaps, its
 * lower rows are five keys wide, and the bottom row is connected sideways to
 * matrix column 7.</p>
 */
public final class HardwareKeyboardLayout {
	private static final int MATRIX_SIZE = Byte.SIZE;
	private static final int PHYSICAL_KEY_COUNT = 52;

	public enum Layer {
		NORMAL(0, "N"),
		SHIFT(1, "S"),
		ALT_ALPHA(2, "A");

		private final int mapIndex;
		private final String abbreviation;

		Layer(final int mapIndex, final String abbreviation) {
			this.mapIndex = mapIndex;
			this.abbreviation = abbreviation;
		}

		public String abbreviation() {
			return abbreviation;
		}
	}

	public record PhysicalKey(int slot, int matrixRow, int matrixColumn) {
		public PhysicalKey {
			if (slot < 0)
				throw new IllegalArgumentException("Negative physical key slot: " + slot);
			if (matrixRow < 0 || matrixRow >= MATRIX_SIZE
					|| matrixColumn < 0 || matrixColumn >= MATRIX_SIZE) {
				throw new IllegalArgumentException(
						"Invalid keyboard matrix position: " + matrixRow + "," + matrixColumn);
			}
		}

		public Key mapping(final Layer layer) {
			return Keyboard.keyMap[matrixRow][matrixColumn][Objects.requireNonNull(layer).mapIndex];
		}

		public String label(final Layer layer) {
			return labelFor(mapping(layer));
		}
	}

	public record PhysicalRow(int slotCount, List<PhysicalKey> keys) {
		public PhysicalRow {
			if (slotCount <= 0)
				throw new IllegalArgumentException("A physical keyboard row needs at least one slot");
			keys = List.copyOf(keys);
			final boolean[] occupiedSlots = new boolean[slotCount];
			for (final PhysicalKey key : keys) {
				if (key.slot() >= slotCount)
					throw new IllegalArgumentException("Key slot is outside its physical row: " + key.slot());
				if (occupiedSlots[key.slot()])
					throw new IllegalArgumentException("Duplicate physical key slot: " + key.slot());
				occupiedSlots[key.slot()] = true;
			}
		}
	}

	private static final List<PhysicalRow> ROWS = validate(List.of(
			row(7, key(0, 0, 0), key(1, 0, 1), key(3, 0, 3), key(5, 0, 5), key(6, 0, 6)),
			row(7, key(0, 1, 0), key(1, 1, 1), key(2, 1, 2), key(3, 1, 3),
					key(4, 1, 4), key(5, 1, 5), key(6, 1, 6)),
			row(7, key(0, 2, 0), key(1, 2, 1), key(3, 2, 3), key(4, 2, 4),
					key(5, 2, 5), key(6, 2, 6)),
			row(7, key(0, 3, 0), key(1, 3, 1), key(2, 3, 2), key(3, 3, 3),
					key(4, 3, 4), key(5, 3, 5), key(6, 3, 6)),
			row(7, key(0, 4, 0), key(1, 4, 1), key(2, 4, 2), key(3, 4, 3),
					key(4, 4, 4), key(5, 4, 5), key(6, 4, 6)),
			row(5, key(0, 5, 0), key(1, 5, 1), key(2, 5, 2), key(3, 5, 3), key(4, 5, 4)),
			row(5, key(0, 6, 0), key(1, 6, 1), key(2, 6, 2), key(3, 6, 3), key(4, 6, 4)),
			row(5, key(0, 7, 0), key(1, 7, 1), key(2, 7, 2), key(3, 7, 3), key(4, 7, 4)),
			row(5, key(0, 4, 7), key(1, 3, 7), key(2, 2, 7), key(3, 1, 7), key(4, 0, 7))));

	private HardwareKeyboardLayout() {}

	public static List<PhysicalRow> rows() {
		return ROWS;
	}

	public static Layer activeLayer() {
		if (Keyboard.shift)
			return Layer.SHIFT;
		if (Keyboard.alpha)
			return Layer.ALT_ALPHA;
		return Layer.NORMAL;
	}

	public static String labelFor(final Key key) {
		return switch (Objects.requireNonNull(key)) {
			case NONE -> "-";
			case SHIFT -> "SHIFT";
			case ALPHA -> "ALT";
			case HISTORY_BACK -> "<HIST";
			case HISTORY_FORWARD -> "HIST>";
			case SURD_MODE -> "S<->D";
			case DRG_CYCLE -> "D/R/G";
			case STEP -> "STEP";
			case SIMPLIFY -> "SIMPLIFY";
			case BRIGHTNESS_CYCLE -> "BRI+";
			case BRIGHTNESS_CYCLE_REVERSE -> "BRI-";
			case DOT -> ".";
			case NUM0 -> "0";
			case NUM1 -> "1";
			case NUM2 -> "2";
			case NUM3 -> "3";
			case NUM4 -> "4";
			case NUM5 -> "5";
			case NUM6 -> "6";
			case NUM7 -> "7";
			case NUM8 -> "8";
			case NUM9 -> "9";
			case PARENTHESIS_OPEN -> "(";
			case PARENTHESIS_CLOSE -> ")";
			case PLUS -> "+";
			case MINUS -> "-";
			case PLUS_MINUS -> "+/-";
			case MULTIPLY -> "*";
			case DIVIDE -> "/";
			case DELETE -> "DEL";
			case RESET -> "CLEAR";
			case LEFT -> "LEFT";
			case RIGHT -> "RIGHT";
			case UP -> "UP";
			case DOWN -> "DOWN";
			case OK -> "OK";
			case SQRT -> "SQRT";
			case ROOT -> "ROOT";
			case POWER_OF_2 -> "x^2";
			case POWER_OF_x -> "x^y";
			case PI -> "PI";
			case SETTINGS -> "GUIDE";
			case F1 -> "F1";
			case F2 -> "F2";
			case F3 -> "F3";
			case F4 -> "F4";
			case BACK -> "BACK";
			case ZOOM_MODE -> "ZOOM";
			case LETTER_X -> "X";
			case LETTER_Y -> "Y";
			case LETTER_Z -> "Z";
			default -> key.name();
		};
	}

	private static PhysicalRow row(final int slotCount, final PhysicalKey... keys) {
		return new PhysicalRow(slotCount, List.of(keys));
	}

	private static PhysicalKey key(final int slot, final int matrixRow, final int matrixColumn) {
		return new PhysicalKey(slot, matrixRow, matrixColumn);
	}

	private static List<PhysicalRow> validate(final List<PhysicalRow> rows) {
		final boolean[][] matrixPositions = new boolean[MATRIX_SIZE][MATRIX_SIZE];
		int keyCount = 0;
		for (final PhysicalRow row : rows) {
			for (final PhysicalKey key : row.keys()) {
				if (matrixPositions[key.matrixRow()][key.matrixColumn()]) {
					throw new IllegalStateException("Duplicate physical keyboard matrix position: "
							+ key.matrixRow() + "," + key.matrixColumn());
				}
				matrixPositions[key.matrixRow()][key.matrixColumn()] = true;
				keyCount++;
			}
		}
		if (keyCount != PHYSICAL_KEY_COUNT)
			throw new IllegalStateException("Expected " + PHYSICAL_KEY_COUNT + " physical keys, got " + keyCount);
		return List.copyOf(rows);
	}
}
