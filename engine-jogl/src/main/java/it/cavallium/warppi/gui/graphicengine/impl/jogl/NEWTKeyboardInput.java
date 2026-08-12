package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;

import it.cavallium.warppi.device.input.Keyboard;
import it.cavallium.warppi.event.Key;

final class NEWTKeyboardInput implements KeyListener {

	private final Consumer<Key> onPressed;
	private final Consumer<Key> onReleased;
	private final BooleanSupplier shiftActive;
	private final BooleanSupplier alphaActive;
	private final Consumer<Boolean> onEnterState;
	private final Map<Short, Key> pressedKeys = new HashMap<>();

	NEWTKeyboardInput() {
		this(
				Keyboard::keyPressed,
				Keyboard::keyReleased,
				() -> Keyboard.shift,
				() -> Keyboard.alpha,
				pressed -> Keyboard.debugKeysDown[1][0] = pressed
		);
	}

	NEWTKeyboardInput(Consumer<Key> onPressed,
			Consumer<Key> onReleased,
			BooleanSupplier shiftActive,
			BooleanSupplier alphaActive,
			Consumer<Boolean> onEnterState) {
		this.onPressed = Objects.requireNonNull(onPressed);
		this.onReleased = Objects.requireNonNull(onReleased);
		this.shiftActive = Objects.requireNonNull(shiftActive);
		this.alphaActive = Objects.requireNonNull(alphaActive);
		this.onEnterState = Objects.requireNonNull(onEnterState);
	}

	@Override
	public void keyPressed(KeyEvent event) {
		press(event.getKeyCode(), event.getKeyChar(), event.isControlDown());
	}

	@Override
	public void keyReleased(KeyEvent event) {
		release(event.getKeyCode());
	}

	synchronized void press(short keyCode, char keyChar, boolean controlDown) {
		Key key = pressedKeys.get(keyCode);
		if (key != null) {
			if (key != Key.SHIFT && key != Key.ALPHA)
				onPressed.accept(key);
			return;
		}

		key = mapKey(keyCode, keyChar, controlDown, shiftActive.getAsBoolean(), alphaActive.getAsBoolean());
		if (key == null)
			return;
		pressedKeys.put(keyCode, key);

		onPressed.accept(key);
		if (keyCode == KeyEvent.VK_ENTER)
			onEnterState.accept(true);
	}

	synchronized void release(short keyCode) {
		if (keyCode == KeyEvent.VK_ENTER)
			onEnterState.accept(false);

		final Key key = pressedKeys.remove(keyCode);
		if (key == null)
			return;

		onReleased.accept(key);
		resetMomentaryModifier(key);
	}

	synchronized void releaseAll() {
		final List<Key> keys = new ArrayList<>(pressedKeys.values());
		pressedKeys.clear();
		onEnterState.accept(false);

		for (Key key : keys)
			onReleased.accept(key);
		if (keys.contains(Key.SHIFT))
			resetMomentaryModifier(Key.SHIFT);
		if (keys.contains(Key.ALPHA))
			resetMomentaryModifier(Key.ALPHA);
	}

	private void resetMomentaryModifier(Key key) {
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

	static Key mapKey(short keyCode, char keyChar, boolean controlDown, boolean shift, boolean alpha) {
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
				return Key.DOT;
			case '(':
				return Key.PARENTHESIS_OPEN;
			case ')':
				return Key.PARENTHESIS_CLOSE;
			case 'd':
			case 'D':
				return Key.debug_DEG;
			case 'r':
			case 'R':
				return Key.debug_RAD;
			case 'g':
			case 'G':
				return Key.debug_GRA;
			case 'x':
			case 'X':
				return Key.LETTER_X;
			case 'y':
			case 'Y':
				return Key.LETTER_Y;
			case 'p':
			case 'P':
				return Key.PI;
			case 'b':
				return Key.BRIGHTNESS_CYCLE;
			case 'B':
				return Key.BRIGHTNESS_CYCLE_REVERSE;
			case '=':
				return Key.SIMPLIFY;
			case '0':
				return Key.NUM0;
			case '1':
				return Key.NUM1;
			case '2':
				return Key.NUM2;
			case '3':
				return Key.NUM3;
			case '4':
				return Key.NUM4;
			case '5':
				return Key.NUM5;
			case '6':
				return Key.NUM6;
			case '7':
				return Key.NUM7;
			case '8':
				return Key.NUM8;
			case '9':
				return Key.NUM9;
			case 'm':
			case 'M':
				return Key.SURD_MODE;
			default:
				break;
		}

		switch (keyCode) {
			case KeyEvent.VK_ESCAPE:
				return Key.POWEROFF;
			case KeyEvent.VK_ENTER:
				return !shift && !alpha ? Key.OK : Key.NONE;
			case KeyEvent.VK_F1:
				return Key.debug1;
			case KeyEvent.VK_F2:
				return Key.debug2;
			case KeyEvent.VK_F3:
				return Key.debug3;
			case KeyEvent.VK_F4:
				return Key.debug4;
			case KeyEvent.VK_F5:
				return Key.debug5;
			case 0x15:
			case KeyEvent.VK_SHIFT:
				return Key.SHIFT;
			case KeyEvent.VK_CONTROL:
				return Key.ALPHA;
			case KeyEvent.VK_LEFT:
				return alpha ? Key.HISTORY_BACK : Key.LEFT;
			case KeyEvent.VK_RIGHT:
				return alpha ? Key.HISTORY_FORWARD : Key.RIGHT;
			case KeyEvent.VK_UP:
				return Key.UP;
			case KeyEvent.VK_DOWN:
			case 12:
				return Key.DOWN;
			case KeyEvent.VK_BACK_SPACE:
				return Key.DELETE;
			case KeyEvent.VK_DELETE:
				return alpha ? Key.RESET : null;
			case KeyEvent.VK_NUMPAD0:
				return Key.NUM0;
			case KeyEvent.VK_NUMPAD1:
				return Key.NUM1;
			case KeyEvent.VK_NUMPAD2:
				return Key.NUM2;
			case KeyEvent.VK_NUMPAD3:
				return Key.NUM3;
			case KeyEvent.VK_NUMPAD4:
				return Key.NUM4;
			case KeyEvent.VK_NUMPAD5:
				return Key.NUM5;
			case KeyEvent.VK_NUMPAD6:
				return Key.NUM6;
			case KeyEvent.VK_NUMPAD7:
				return Key.NUM7;
			case KeyEvent.VK_NUMPAD8:
				return Key.NUM8;
			case KeyEvent.VK_NUMPAD9:
				return Key.NUM9;
			case KeyEvent.VK_ADD:
			case KeyEvent.VK_PLUS:
				return Key.PLUS;
			case KeyEvent.VK_SUBTRACT:
			case KeyEvent.VK_MINUS:
				return Key.MINUS;
			case KeyEvent.VK_MULTIPLY:
			case KeyEvent.VK_ASTERISK:
				return Key.MULTIPLY;
			case KeyEvent.VK_DIVIDE:
			case KeyEvent.VK_SLASH:
				return Key.DIVIDE;
			case KeyEvent.VK_DECIMAL:
			case KeyEvent.VK_PERIOD:
				return Key.DOT;
			case KeyEvent.VK_EQUALS:
				return Key.SIMPLIFY;
			default:
				return null;
		}
	}
}
