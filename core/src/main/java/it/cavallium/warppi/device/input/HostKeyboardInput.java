package it.cavallium.warppi.device.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import it.cavallium.warppi.event.Key;

/**
 * Translates a computer keyboard into calculator keys.
 *
 * <p>The platform adapters normalize non-printable key codes to
 * {@link KeyboardAWTValues}; printable keys are selected from their character.
 * Keeping the pressed-key mapping here makes releases deterministic even when a
 * calculator modifier changes after the press.</p>
 */
public final class HostKeyboardInput {
	private final Consumer<Key> onPressed;
	private final Consumer<Key> onReleased;
	private final BooleanSupplier shiftActive;
	private final BooleanSupplier alphaActive;
	private final Consumer<Boolean> onEnterState;
	private final Map<Integer, Key> pressedKeys = new HashMap<>();

	public HostKeyboardInput(final Consumer<Key> onPressed,
			final Consumer<Key> onReleased,
			final BooleanSupplier shiftActive,
			final BooleanSupplier alphaActive,
			final Consumer<Boolean> onEnterState) {
		this.onPressed = Objects.requireNonNull(onPressed);
		this.onReleased = Objects.requireNonNull(onReleased);
		this.shiftActive = Objects.requireNonNull(shiftActive);
		this.alphaActive = Objects.requireNonNull(alphaActive);
		this.onEnterState = Objects.requireNonNull(onEnterState);
	}

	/** Returns whether the host key is a calculator key. */
	public synchronized boolean press(final int keyCode, final char keyChar, final boolean controlDown) {
		Key key = pressedKeys.get(keyCode);
		if (key != null) {
			if (key != Key.SHIFT && key != Key.ALPHA)
				onPressed.accept(key);
			return true;
		}

		key = mapKey(keyCode, keyChar, controlDown,
				shiftActive.getAsBoolean(), alphaActive.getAsBoolean());
		if (key == null)
			return false;

		pressedKeys.put(keyCode, key);
		onPressed.accept(key);
		if (keyCode == KeyboardAWTValues.VK_ENTER)
			onEnterState.accept(true);
		return true;
	}

	/** Returns whether this releases a calculator key pressed through this adapter. */
	public synchronized boolean release(final int keyCode) {
		if (keyCode == KeyboardAWTValues.VK_ENTER)
			onEnterState.accept(false);

		final Key key = pressedKeys.remove(keyCode);
		if (key == null)
			return false;

		onReleased.accept(key);
		resetMomentaryModifier(key);
		return true;
	}

	/** Releases all host keys, for example when a window loses focus. */
	public synchronized void releaseAll() {
		final List<Key> keys = new ArrayList<>(pressedKeys.values());
		pressedKeys.clear();
		onEnterState.accept(false);

		for (final Key key : keys)
			onReleased.accept(key);
		if (keys.contains(Key.SHIFT))
			resetMomentaryModifier(Key.SHIFT);
		if (keys.contains(Key.ALPHA))
			resetMomentaryModifier(Key.ALPHA);
	}

	private void resetMomentaryModifier(final Key key) {
		final boolean active;
		if (key == Key.SHIFT)
			active = shiftActive.getAsBoolean();
		else if (key == Key.ALPHA)
			active = alphaActive.getAsBoolean();
		else
			return;

		if (active) {
			onPressed.accept(key);
			onReleased.accept(key);
		}
	}

	public static Key mapKey(final int keyCode, final char keyChar, final boolean controlDown,
			final boolean shift, final boolean alpha) {
		switch (keyCode) {
			case KeyboardAWTValues.VK_SHIFT:
				return Key.SHIFT;
			case KeyboardAWTValues.VK_ALT:
			case KeyboardAWTValues.VK_ALT_GRAPH:
				return Key.ALPHA;
			case KeyboardAWTValues.VK_CONTROL:
				return null;
			default:
				break;
		}

		final Key printable = mapPrintableKey(keyChar, controlDown, shift, alpha);
		if (printable != null)
			return printable;

		switch (keyCode) {
			case KeyboardAWTValues.VK_ESCAPE:
				return Key.BACK;
			case KeyboardAWTValues.VK_ENTER:
				return Key.OK;
			case KeyboardAWTValues.VK_F1:
				return Key.debug1;
			case KeyboardAWTValues.VK_F2:
				return Key.debug2;
			case KeyboardAWTValues.VK_F3:
				return Key.debug3;
			case KeyboardAWTValues.VK_F4:
				return Key.debug4;
			case KeyboardAWTValues.VK_F5:
				return Key.debug5;
			case KeyboardAWTValues.VK_LEFT:
			case KeyboardAWTValues.VK_KP_LEFT:
				return alpha ? Key.HISTORY_BACK : Key.LEFT;
			case KeyboardAWTValues.VK_RIGHT:
			case KeyboardAWTValues.VK_KP_RIGHT:
				return alpha ? Key.HISTORY_FORWARD : Key.RIGHT;
			case KeyboardAWTValues.VK_UP:
			case KeyboardAWTValues.VK_KP_UP:
				return Key.UP;
			case KeyboardAWTValues.VK_DOWN:
			case KeyboardAWTValues.VK_KP_DOWN:
				return Key.DOWN;
			case KeyboardAWTValues.VK_BACK_SPACE:
				return Key.DELETE;
			case KeyboardAWTValues.VK_DELETE:
				return !shift && !alpha ? Key.RESET : null;
			case KeyboardAWTValues.VK_NUMPAD0:
				return Key.NUM0;
			case KeyboardAWTValues.VK_NUMPAD1:
				return Key.NUM1;
			case KeyboardAWTValues.VK_NUMPAD2:
				return Key.NUM2;
			case KeyboardAWTValues.VK_NUMPAD3:
				return Key.NUM3;
			case KeyboardAWTValues.VK_NUMPAD4:
				return Key.NUM4;
			case KeyboardAWTValues.VK_NUMPAD5:
				return Key.NUM5;
			case KeyboardAWTValues.VK_NUMPAD6:
				return Key.NUM6;
			case KeyboardAWTValues.VK_NUMPAD7:
				return Key.NUM7;
			case KeyboardAWTValues.VK_NUMPAD8:
				return Key.NUM8;
			case KeyboardAWTValues.VK_NUMPAD9:
				return Key.NUM9;
			case KeyboardAWTValues.VK_ADD:
			case KeyboardAWTValues.VK_PLUS:
				return controlDown ? Key.ZOOM_MODE : Key.PLUS;
			case KeyboardAWTValues.VK_SUBTRACT:
			case KeyboardAWTValues.VK_MINUS:
				return Key.MINUS;
			case KeyboardAWTValues.VK_MULTIPLY:
			case KeyboardAWTValues.VK_ASTERISK:
				return Key.MULTIPLY;
			case KeyboardAWTValues.VK_DIVIDE:
			case KeyboardAWTValues.VK_SLASH:
				return Key.DIVIDE;
			case KeyboardAWTValues.VK_DECIMAL:
			case KeyboardAWTValues.VK_PERIOD:
				return Key.DOT;
			case KeyboardAWTValues.VK_EQUALS:
				return Key.SIMPLIFY;
			case KeyboardAWTValues.VK_LEFT_PARENTHESIS:
				return Key.PARENTHESIS_OPEN;
			case KeyboardAWTValues.VK_RIGHT_PARENTHESIS:
				return Key.PARENTHESIS_CLOSE;
			default:
				break;
		}

		if (keyCode >= KeyboardAWTValues.VK_0 && keyCode <= KeyboardAWTValues.VK_9)
			return !shift && !alpha ? digitKey((char) keyCode) : null;
		if (keyCode >= KeyboardAWTValues.VK_A && keyCode <= KeyboardAWTValues.VK_Z)
			return mapLetter((char) keyCode, shift, alpha);
		return null;
	}

	private static Key mapPrintableKey(final char keyChar, final boolean controlDown,
			final boolean shift, final boolean alpha) {
		switch (keyChar) {
			case '+':
				return controlDown ? Key.ZOOM_MODE : Key.PLUS;
			case '-':
				return Key.MINUS;
			case '*':
				return Key.MULTIPLY;
			case '/':
				return Key.DIVIDE;
			case '.':
			case ',':
				return Key.DOT;
			case '(':
				return Key.PARENTHESIS_OPEN;
			case ')':
				return Key.PARENTHESIS_CLOSE;
			case '=':
				return Key.SIMPLIFY;
			case '<':
				return Key.LESS_THAN;
			case '>':
				return Key.GREATER_THAN;
			case '%':
				return Key.PERCENT;
			case '!':
				return Key.FACTORIAL;
			default:
				break;
		}

		if (keyChar >= '0' && keyChar <= '9')
			return !shift && !alpha ? digitKey(keyChar) : null;
		if (keyChar >= 'a' && keyChar <= 'z')
			return mapLetter((char) (keyChar - ('a' - 'A')), shift, alpha);
		if (keyChar >= 'A' && keyChar <= 'Z')
			return mapLetter(keyChar, shift, alpha);
		return null;
	}

	private static Key mapLetter(final char letter, final boolean shift, final boolean alpha) {
		if (alpha)
			return alphaLetter(letter);
		if (shift) {
			return switch (letter) {
				case 'S' -> Key.ARCSINE;
				case 'C' -> Key.ARCCOSINE;
				case 'T' -> Key.ARCTANGENT;
				case 'B' -> Key.BRIGHTNESS_CYCLE_REVERSE;
				case 'Z' -> Key.ZOOM_MODE;
				case 'L' -> Key.NATURAL_LOGARITHM;
				default -> null;
			};
		}
		return switch (letter) {
			case 'S' -> Key.SINE;
			case 'C' -> Key.COSINE;
			case 'T' -> Key.TANGENT;
			case 'D' -> Key.debug_DEG;
			case 'R' -> Key.debug_RAD;
			case 'G' -> Key.debug_GRA;
			case 'X' -> Key.LETTER_X;
			case 'Y' -> Key.LETTER_Y;
			case 'P' -> Key.PI;
			case 'B' -> Key.BRIGHTNESS_CYCLE;
			case 'E' -> Key.EULER_NUMBER;
			case 'L' -> Key.LOGARITHM;
			case 'M' -> Key.SURD_MODE;
			default -> null;
		};
	}

	private static Key alphaLetter(final char letter) {
		return switch (letter) {
			case 'A' -> Key.LETTER_A;
			case 'B' -> Key.LETTER_B;
			case 'C' -> Key.LETTER_C;
			case 'D' -> Key.LETTER_D;
			case 'E' -> Key.LETTER_E;
			case 'F' -> Key.LETTER_F;
			case 'G' -> Key.LETTER_G;
			case 'H' -> Key.LETTER_H;
			case 'I' -> Key.LETTER_I;
			case 'J' -> Key.LETTER_J;
			case 'K' -> Key.LETTER_K;
			case 'L' -> Key.LETTER_L;
			case 'M' -> Key.LETTER_M;
			case 'N' -> Key.LETTER_N;
			case 'O' -> Key.LETTER_O;
			case 'P' -> Key.LETTER_P;
			case 'Q' -> Key.LETTER_Q;
			case 'R' -> Key.LETTER_R;
			case 'S' -> Key.LETTER_S;
			case 'T' -> Key.LETTER_T;
			case 'U' -> Key.LETTER_U;
			case 'V' -> Key.LETTER_V;
			case 'W' -> Key.LETTER_W;
			case 'X' -> Key.LETTER_X;
			case 'Y' -> Key.LETTER_Y;
			case 'Z' -> Key.LETTER_Z;
			default -> null;
		};
	}

	private static Key digitKey(final char digit) {
		return switch (digit) {
			case '0' -> Key.NUM0;
			case '1' -> Key.NUM1;
			case '2' -> Key.NUM2;
			case '3' -> Key.NUM3;
			case '4' -> Key.NUM4;
			case '5' -> Key.NUM5;
			case '6' -> Key.NUM6;
			case '7' -> Key.NUM7;
			case '8' -> Key.NUM8;
			case '9' -> Key.NUM9;
			default -> null;
		};
	}
}
