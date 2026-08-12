package it.cavallium.warppi.device.input;

import java.util.List;
import java.util.Objects;

import it.cavallium.warppi.event.Key;

/**
 * Physical placement, electrical coordinates, and logical mappings of the
 * calculator keypad.
 *
 * <p>The keypad is electrically addressed as an 8-by-8 matrix, but it is not
 * physically an 8-by-8 grid. Its top rows contain gaps, its lower rows are five
 * keys wide, and its bottom row is connected sideways to matrix column 7.</p>
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

	public record PhysicalKey(int slot, int matrixRow, int matrixColumn,
			Key normal, Key shift, Key altAlpha) {
		public PhysicalKey {
			if (slot < 0)
				throw new IllegalArgumentException("Negative physical key slot: " + slot);
			if (matrixRow < 0 || matrixRow >= MATRIX_SIZE
					|| matrixColumn < 0 || matrixColumn >= MATRIX_SIZE) {
				throw new IllegalArgumentException(
						"Invalid keyboard matrix position: " + matrixRow + "," + matrixColumn);
			}
			Objects.requireNonNull(normal, "normal");
			Objects.requireNonNull(shift, "shift");
			Objects.requireNonNull(altAlpha, "altAlpha");
		}

		public Key mapping(final Layer layer) {
			return switch (Objects.requireNonNull(layer, "layer")) {
				case NORMAL -> normal;
				case SHIFT -> shift;
				case ALT_ALPHA -> altAlpha;
			};
		}

		public String label(final Layer layer) {
			return labelFor(mapping(layer));
		}

		public String compactLabel(final Layer layer) {
			return compactLabelFor(mapping(layer));
		}

		/** Returns whether this key is the modifier selecting the displayed layer. */
		public boolean isActiveModifier(final Layer layer) {
			return switch (Objects.requireNonNull(layer, "layer")) {
				case NORMAL -> false;
				case SHIFT -> normal == Key.SHIFT;
				case ALT_ALPHA -> normal == Key.ALPHA;
			};
		}

		/** PCB switch identifier, for example {@code S11} or {@code S58}. */
		public String switchLabel() {
			return "S" + (matrixRow + 1) + (matrixColumn + 1);
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

		/** Returns the key in a visual slot, or {@code null} for an intentional gap. */
		public PhysicalKey keyAtSlot(final int slot) {
			if (slot < 0 || slot >= slotCount)
				throw new IllegalArgumentException("Slot is outside its physical row: " + slot);
			for (final PhysicalKey key : keys)
				if (key.slot() == slot)
					return key;
			return null;
		}
	}

	private static final List<PhysicalRow> ROWS = validate(List.of(
			row(7,
					persistentKey(0, 0, 0, Key.SHIFT),
					persistentKey(1, 0, 1, Key.ALPHA),
					key(3, 0, 3, Key.UP),
					persistentKey(5, 0, 5, Key.SETTINGS),
					key(6, 0, 6, Key.BRIGHTNESS_CYCLE, Key.BRIGHTNESS_CYCLE_REVERSE, Key.ZOOM_MODE)),
			row(7,
					persistentKey(0, 1, 0, Key.F4),
					key(1, 1, 1, Key.NONE),
					key(2, 1, 2, Key.LEFT),
					key(3, 1, 3, Key.OK),
					key(4, 1, 4, Key.RIGHT),
					key(5, 1, 5, Key.HISTORY_BACK),
					key(6, 1, 6, Key.HISTORY_FORWARD)),
			row(7,
					persistentKey(0, 2, 0, Key.F3),
					key(1, 2, 1, Key.SQRT, Key.ROOT, Key.NONE),
					key(3, 2, 3, Key.DOWN),
					key(4, 2, 4, Key.BACK),
					key(5, 2, 5, Key.NONE),
					key(6, 2, 6, Key.NONE)),
			row(7,
					persistentKey(0, 3, 0, Key.F2),
					key(1, 3, 1, Key.NONE),
					key(2, 3, 2, Key.POWER_OF_x),
					key(3, 3, 3, Key.POWER_OF_2),
					key(4, 3, 4, Key.NONE),
					key(5, 3, 5, Key.NONE),
					key(6, 3, 6, Key.NONE)),
			row(7,
					persistentKey(0, 4, 0, Key.F1),
					key(1, 4, 1, Key.NONE),
					key(2, 4, 2, Key.PARENTHESIS_OPEN),
					key(3, 4, 3, Key.PARENTHESIS_CLOSE),
					key(4, 4, 4, Key.NONE),
					key(5, 4, 5, Key.SURD_MODE),
					key(6, 4, 6, Key.NONE)),
			row(5,
					key(0, 5, 0, Key.NUM7),
					key(1, 5, 1, Key.NUM8),
					key(2, 5, 2, Key.NUM9),
					key(3, 5, 3, Key.DELETE),
					key(4, 5, 4, Key.RESET)),
			row(5,
					key(0, 6, 0, Key.NUM4),
					key(1, 6, 1, Key.NUM5),
					key(2, 6, 2, Key.NUM6),
					key(3, 6, 3, Key.MULTIPLY),
					key(4, 6, 4, Key.DIVIDE)),
			row(5,
					key(0, 7, 0, Key.NUM1),
					key(1, 7, 1, Key.NUM2),
					key(2, 7, 2, Key.NUM3),
					key(3, 7, 3, Key.PLUS, Key.PLUS_MINUS, Key.NONE),
					key(4, 7, 4, Key.MINUS)),
			row(5,
					key(0, 4, 7, Key.NUM0, Key.NONE, Key.LETTER_X),
					key(1, 3, 7, Key.DOT, Key.NONE, Key.LETTER_Y),
					key(2, 2, 7, Key.NONE, Key.NONE, Key.LETTER_Z),
					key(3, 1, 7, Key.NONE, Key.PI, Key.DRG_CYCLE),
					key(4, 0, 7, Key.SIMPLIFY, Key.STEP, Key.NONE))));

	private static final Key[][][] MATRIX_MAPPINGS = createMatrixMappings(ROWS);

	private HardwareKeyboardLayout() {}

	public static List<PhysicalRow> rows() {
		return ROWS;
	}

	public static Layer activeLayer() {
		return activeLayer(Keyboard.shift, Keyboard.alpha);
	}

	public static Layer activeLayer(final boolean shift, final boolean alpha) {
		if (shift)
			return Layer.SHIFT;
		if (alpha)
			return Layer.ALT_ALPHA;
		return Layer.NORMAL;
	}

	public static Key mappingAt(final int matrixRow, final int matrixColumn,
			final boolean shift, final boolean alpha) {
		return mappingAt(matrixRow, matrixColumn, activeLayer(shift, alpha));
	}

	public static Key mappingAt(final int matrixRow, final int matrixColumn, final Layer layer) {
		validateMatrixPosition(matrixRow, matrixColumn);
		return MATRIX_MAPPINGS[matrixRow][matrixColumn][Objects.requireNonNull(layer, "layer").mapIndex];
	}

	/** Labels used by the desktop and web keyboards. */
	public static String labelFor(final Key key) {
		return switch (Objects.requireNonNull(key, "key")) {
			case NONE -> "";
			case SHIFT -> "⇪";
			case ALPHA -> "A";
			case HISTORY_BACK -> "≪";
			case HISTORY_FORWARD -> "≫";
			case SURD_MODE -> "S⇔D";
			case DRG_CYCLE -> "D/R/G";
			case STEP -> "STEP";
			case SIMPLIFY -> "↩";
			case BRIGHTNESS_CYCLE, BRIGHTNESS_CYCLE_REVERSE -> "☼";
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
			case PLUS_MINUS -> "±";
			case MULTIPLY -> "*";
			case DIVIDE -> "/";
			case DELETE -> "⌫";
			case RESET -> "RESET";
			case LEFT -> "⇦";
			case RIGHT -> "⇨";
			case UP -> "⇧";
			case DOWN -> "⇩";
			case OK -> "OK";
			case SQRT -> "√";
			case ROOT -> "ⁿ√";
			case POWER_OF_2 -> "^2";
			case POWER_OF_x -> "^x";
			case PI -> "π";
			case SETTINGS -> "GUIDE";
			case F1 -> "F1";
			case F2 -> "F2";
			case F3 -> "F3";
			case F4 -> "F4";
			case BACK -> "↶";
			case ZOOM_MODE -> "ZOOM";
			case LETTER_X -> "x";
			case LETTER_Y -> "y";
			case LETTER_Z -> "z";
			default -> key.name();
		};
	}

	/** Short ASCII labels used by the fixed-width on-device guide. */
	public static String compactLabelFor(final Key key) {
		return switch (Objects.requireNonNull(key, "key")) {
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
			case PARENTHESIS_OPEN -> "(";
			case PARENTHESIS_CLOSE -> ")";
			case PLUS_MINUS -> "+/-";
			case DELETE -> "DEL";
			case RESET -> "CLEAR";
			case LEFT -> "LEFT";
			case RIGHT -> "RIGHT";
			case UP -> "UP";
			case DOWN -> "DOWN";
			case SQRT -> "SQRT";
			case ROOT -> "ROOT";
			case POWER_OF_2 -> "x^2";
			case POWER_OF_x -> "x^y";
			case PI -> "PI";
			case SETTINGS -> "GUIDE";
			case BACK -> "BACK";
			case ZOOM_MODE -> "ZOOM";
			case LETTER_X -> "X";
			case LETTER_Y -> "Y";
			case LETTER_Z -> "Z";
			default -> labelFor(key);
		};
	}

	private static PhysicalRow row(final int slotCount, final PhysicalKey... keys) {
		return new PhysicalRow(slotCount, List.of(keys));
	}

	private static PhysicalKey key(final int slot, final int matrixRow, final int matrixColumn, final Key normal) {
		return key(slot, matrixRow, matrixColumn, normal, Key.NONE, Key.NONE);
	}

	private static PhysicalKey key(final int slot, final int matrixRow, final int matrixColumn,
			final Key normal, final Key shift, final Key altAlpha) {
		return new PhysicalKey(slot, matrixRow, matrixColumn, normal, shift, altAlpha);
	}

	private static PhysicalKey persistentKey(final int slot, final int matrixRow, final int matrixColumn,
			final Key mapping) {
		return key(slot, matrixRow, matrixColumn, mapping, mapping, mapping);
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

	private static Key[][][] createMatrixMappings(final List<PhysicalRow> rows) {
		final Key[][][] mappings = new Key[MATRIX_SIZE][MATRIX_SIZE][Layer.values().length];
		for (int row = 0; row < MATRIX_SIZE; row++)
			for (int column = 0; column < MATRIX_SIZE; column++)
				for (final Layer layer : Layer.values())
					mappings[row][column][layer.mapIndex] = Key.NONE;

		for (final PhysicalRow row : rows)
			for (final PhysicalKey key : row.keys())
				for (final Layer layer : Layer.values())
					mappings[key.matrixRow()][key.matrixColumn()][layer.mapIndex] = key.mapping(layer);
		return mappings;
	}

	private static void validateMatrixPosition(final int matrixRow, final int matrixColumn) {
		if (matrixRow < 0 || matrixRow >= MATRIX_SIZE || matrixColumn < 0 || matrixColumn >= MATRIX_SIZE)
			throw new IllegalArgumentException("Keyboard coordinates must be between 0 and 7");
	}
}
