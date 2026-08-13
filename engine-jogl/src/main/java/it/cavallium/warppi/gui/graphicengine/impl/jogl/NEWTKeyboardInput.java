package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;

import it.cavallium.warppi.device.input.HostKeyboardInput;
import it.cavallium.warppi.device.input.Keyboard;
import it.cavallium.warppi.device.input.KeyboardAWTValues;
import it.cavallium.warppi.event.Key;

final class NEWTKeyboardInput implements KeyListener {

	private final HostKeyboardInput input;

	NEWTKeyboardInput() {
		input = Keyboard.createHostKeyboardInput();
	}

	NEWTKeyboardInput(Consumer<Key> onPressed,
			Consumer<Key> onReleased,
			BooleanSupplier shiftActive,
			BooleanSupplier alphaActive,
			Consumer<Boolean> onEnterState) {
		input = new HostKeyboardInput(onPressed, onReleased, shiftActive, alphaActive, onEnterState);
	}

	@Override
	public void keyPressed(KeyEvent event) {
		press(event.getKeyCode(), event.getKeyChar(), event.isControlDown());
	}

	@Override
	public void keyReleased(KeyEvent event) {
		release(event.getKeyCode());
	}

	void press(final short keyCode, final char keyChar, final boolean controlDown) {
		input.press(normalizeKeyCode(keyCode), keyChar, controlDown);
	}

	void release(final short keyCode) {
		input.release(normalizeKeyCode(keyCode));
	}

	void releaseAll() {
		input.releaseAll();
	}

	static Key mapKey(final short keyCode, final char keyChar, final boolean controlDown,
			final boolean shift, final boolean alpha) {
		return HostKeyboardInput.mapKey(
				normalizeKeyCode(keyCode), keyChar, controlDown, shift, alpha);
	}

	private static int normalizeKeyCode(final short keyCode) {
		switch (keyCode) {
			case KeyEvent.VK_ENTER:
				return KeyboardAWTValues.VK_ENTER;
			case KeyEvent.VK_F1:
				return KeyboardAWTValues.VK_F1;
			case KeyEvent.VK_F2:
				return KeyboardAWTValues.VK_F2;
			case KeyEvent.VK_F3:
				return KeyboardAWTValues.VK_F3;
			case KeyEvent.VK_F4:
				return KeyboardAWTValues.VK_F4;
			case KeyEvent.VK_F5:
				return KeyboardAWTValues.VK_F5;
			case 0x15:
			case KeyEvent.VK_SHIFT:
				return KeyboardAWTValues.VK_SHIFT;
			case KeyEvent.VK_CONTROL:
				return KeyboardAWTValues.VK_CONTROL;
			case KeyEvent.VK_ALT:
				return KeyboardAWTValues.VK_ALT;
			case KeyEvent.VK_ALT_GRAPH:
				return KeyboardAWTValues.VK_ALT_GRAPH;
			case KeyEvent.VK_LEFT:
				return KeyboardAWTValues.VK_LEFT;
			case KeyEvent.VK_RIGHT:
				return KeyboardAWTValues.VK_RIGHT;
			case KeyEvent.VK_UP:
				return KeyboardAWTValues.VK_UP;
			case KeyEvent.VK_DOWN:
			case 12:
				return KeyboardAWTValues.VK_DOWN;
			case KeyEvent.VK_DELETE:
				return KeyboardAWTValues.VK_DELETE;
			case KeyEvent.VK_NUMPAD0:
				return KeyboardAWTValues.VK_NUMPAD0;
			case KeyEvent.VK_NUMPAD1:
				return KeyboardAWTValues.VK_NUMPAD1;
			case KeyEvent.VK_NUMPAD2:
				return KeyboardAWTValues.VK_NUMPAD2;
			case KeyEvent.VK_NUMPAD3:
				return KeyboardAWTValues.VK_NUMPAD3;
			case KeyEvent.VK_NUMPAD4:
				return KeyboardAWTValues.VK_NUMPAD4;
			case KeyEvent.VK_NUMPAD5:
				return KeyboardAWTValues.VK_NUMPAD5;
			case KeyEvent.VK_NUMPAD6:
				return KeyboardAWTValues.VK_NUMPAD6;
			case KeyEvent.VK_NUMPAD7:
				return KeyboardAWTValues.VK_NUMPAD7;
			case KeyEvent.VK_NUMPAD8:
				return KeyboardAWTValues.VK_NUMPAD8;
			case KeyEvent.VK_NUMPAD9:
				return KeyboardAWTValues.VK_NUMPAD9;
			case KeyEvent.VK_ADD:
				return KeyboardAWTValues.VK_ADD;
			case KeyEvent.VK_PLUS:
				return KeyboardAWTValues.VK_PLUS;
			case KeyEvent.VK_SUBTRACT:
				return KeyboardAWTValues.VK_SUBTRACT;
			case KeyEvent.VK_MULTIPLY:
				return KeyboardAWTValues.VK_MULTIPLY;
			case KeyEvent.VK_ASTERISK:
				return KeyboardAWTValues.VK_ASTERISK;
			case KeyEvent.VK_DIVIDE:
				return KeyboardAWTValues.VK_DIVIDE;
			case KeyEvent.VK_DECIMAL:
				return KeyboardAWTValues.VK_DECIMAL;
			case KeyEvent.VK_LEFT_PARENTHESIS:
				return KeyboardAWTValues.VK_LEFT_PARENTHESIS;
			case KeyEvent.VK_RIGHT_PARENTHESIS:
				return KeyboardAWTValues.VK_RIGHT_PARENTHESIS;
			default:
				return keyCode;
		}
	}
}
