package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jogamp.newt.event.KeyEvent;
import org.junit.jupiter.api.Test;

import it.cavallium.warppi.event.Key;

class NEWTKeyboardInputTest {

	@Test
	void mapsPrintableCalculatorKeys() {
		assertEquals(Key.NUM7, map(KeyEvent.VK_7, '7'));
		assertEquals(Key.PLUS, map(KeyEvent.VK_PLUS, '+'));
		assertEquals(Key.DIVIDE, map(KeyEvent.VK_SLASH, '/'));
		assertEquals(Key.DOT, map(KeyEvent.VK_PERIOD, '.'));
		assertEquals(Key.PARENTHESIS_OPEN, map(KeyEvent.VK_LEFT_PARENTHESIS, '('));
		assertEquals(Key.LETTER_X, map(KeyEvent.VK_X, 'x'));
		assertEquals(Key.SIMPLIFY, map(KeyEvent.VK_EQUALS, '='));
	}

	@Test
	void mapsControlPlusToZoom() {
		assertEquals(Key.ZOOM_MODE, NEWTKeyboardInput.mapKey(KeyEvent.VK_PLUS, '+', true, false, false));
	}

	@Test
	void mapsAltRatherThanControlToCalculatorAlpha() {
		assertEquals(Key.ALPHA, NEWTKeyboardInput.mapKey(
				KeyEvent.VK_ALT, KeyEvent.NULL_CHAR, false, false, false));
		assertEquals(Key.ALPHA, NEWTKeyboardInput.mapKey(
				KeyEvent.VK_ALT_GRAPH, KeyEvent.NULL_CHAR, false, false, false));
		assertNull(NEWTKeyboardInput.mapKey(
				KeyEvent.VK_CONTROL, KeyEvent.NULL_CHAR, true, false, false));
	}

	@Test
	void mapsAlphaArrowsToHistory() {
		assertEquals(Key.HISTORY_BACK, NEWTKeyboardInput.mapKey(KeyEvent.VK_LEFT, KeyEvent.NULL_CHAR, false, false, true));
		assertEquals(Key.HISTORY_FORWARD, NEWTKeyboardInput.mapKey(KeyEvent.VK_RIGHT, KeyEvent.NULL_CHAR, false, false, true));
	}

	@Test
	void releasesTheKeyChosenAtPressTime() {
		final AtomicBoolean alpha = new AtomicBoolean(true);
		final List<Key> pressed = new ArrayList<>();
		final List<Key> released = new ArrayList<>();
		final NEWTKeyboardInput input = new NEWTKeyboardInput(
				key -> {
					pressed.add(key);
					alpha.set(false);
				},
				released::add,
				() -> false,
				alpha::get,
				ignored -> { }
		);

		input.press(KeyEvent.VK_LEFT, KeyEvent.NULL_CHAR, false);
		input.release(KeyEvent.VK_LEFT);

		assertEquals(List.of(Key.HISTORY_BACK), pressed);
		assertEquals(List.of(Key.HISTORY_BACK), released);
	}

	@Test
	void releasesMomentaryShift() {
		final AtomicBoolean shift = new AtomicBoolean(false);
		final List<Key> released = new ArrayList<>();
		final NEWTKeyboardInput input = new NEWTKeyboardInput(
				key -> {
					if (key == Key.SHIFT)
						shift.set(!shift.get());
				},
				released::add,
				shift::get,
				() -> false,
				ignored -> { }
		);

		input.press(KeyEvent.VK_SHIFT, KeyEvent.NULL_CHAR, false);
		assertTrue(shift.get());

		input.release(KeyEvent.VK_SHIFT);

		assertFalse(shift.get());
		assertEquals(List.of(Key.SHIFT, Key.SHIFT), released);
	}

	@Test
	void ignoresModifierAutoRepeat() {
		final AtomicBoolean shift = new AtomicBoolean(false);
		final List<Key> pressed = new ArrayList<>();
		final NEWTKeyboardInput input = new NEWTKeyboardInput(
				key -> {
					pressed.add(key);
					if (key == Key.SHIFT)
						shift.set(!shift.get());
				},
				ignored -> { },
				shift::get,
				() -> false,
				ignored -> { }
		);

		input.press(KeyEvent.VK_SHIFT, KeyEvent.NULL_CHAR, false);
		input.press(KeyEvent.VK_SHIFT, KeyEvent.NULL_CHAR, false);

		assertTrue(shift.get());
		assertEquals(List.of(Key.SHIFT), pressed);
	}

	@Test
	void releasesHeldKeysWhenFocusIsLost() {
		final List<Key> released = new ArrayList<>();
		final List<Boolean> enterStates = new ArrayList<>();
		final NEWTKeyboardInput input = new NEWTKeyboardInput(
				ignored -> { },
				released::add,
				() -> false,
				() -> false,
				enterStates::add
		);

		input.press(KeyEvent.VK_ENTER, KeyEvent.NULL_CHAR, false);
		input.press(KeyEvent.VK_1, '1', false);
		input.releaseAll();

		assertTrue(released.containsAll(List.of(Key.OK, Key.NUM1)));
		assertEquals(2, released.size());
		assertEquals(List.of(true, false), enterStates);
	}

	private static Key map(short keyCode, char keyChar) {
		return NEWTKeyboardInput.mapKey(keyCode, keyChar, false, false, false);
	}
}
