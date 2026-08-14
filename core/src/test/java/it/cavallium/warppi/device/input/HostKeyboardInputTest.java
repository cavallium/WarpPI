package it.cavallium.warppi.device.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.event.Key;

class HostKeyboardInputTest {

	@Test
	void dispatchesTwoPlusTwoAndEnterWithoutPolling() {
		final List<Key> pressed = new ArrayList<>();
		final List<Key> released = new ArrayList<>();
		final List<Boolean> enterStates = new ArrayList<>();
		final HostKeyboardInput input = new HostKeyboardInput(
				pressed::add, released::add, () -> false, () -> false, enterStates::add);

		pressAndRelease(input, KeyboardAWTValues.VK_2, '2');
		pressAndRelease(input, KeyboardAWTValues.VK_PLUS, '+');
		pressAndRelease(input, KeyboardAWTValues.VK_2, '2');
		pressAndRelease(input, KeyboardAWTValues.VK_ENTER, KeyboardAWTValues.CHAR_UNDEFINED);

		assertEquals(List.of(Key.NUM2, Key.PLUS, Key.NUM2, Key.OK), pressed);
		assertEquals(pressed, released);
		assertEquals(List.of(true, false), enterStates);
	}

	@Test
	void usesAltForCalculatorAlphaAndLeavesControlAvailableForShortcuts() {
		assertEquals(Key.ALPHA, HostKeyboardInput.mapKey(
				KeyboardAWTValues.VK_ALT, KeyboardAWTValues.CHAR_UNDEFINED,
				false, false, false));
		assertEquals(Key.ALPHA, HostKeyboardInput.mapKey(
				KeyboardAWTValues.VK_ALT_GRAPH, KeyboardAWTValues.CHAR_UNDEFINED,
				false, false, false));
		assertNull(HostKeyboardInput.mapKey(
				KeyboardAWTValues.VK_CONTROL, KeyboardAWTValues.CHAR_UNDEFINED,
				true, false, false));
		assertEquals(Key.ZOOM_MODE, HostKeyboardInput.mapKey(
				KeyboardAWTValues.VK_PLUS, '+', true, false, false));
	}

	@Test
	void alphaIsMomentaryAndSelectsLetterLayer() {
		final AtomicBoolean alpha = new AtomicBoolean(false);
		final List<Key> pressed = new ArrayList<>();
		final HostKeyboardInput input = new HostKeyboardInput(
				key -> {
					pressed.add(key);
					if (key == Key.ALPHA)
						alpha.set(!alpha.get());
					else if (key != Key.NONE)
						alpha.set(false);
				},
				ignored -> { },
				() -> false,
				alpha::get,
				ignored -> { });

		assertTrue(input.press(KeyboardAWTValues.VK_ALT,
				KeyboardAWTValues.CHAR_UNDEFINED, false));
		assertTrue(alpha.get());
		assertTrue(input.press(KeyboardAWTValues.VK_A, 'a', false));
		input.release(KeyboardAWTValues.VK_A);
		input.release(KeyboardAWTValues.VK_ALT);

		assertEquals(List.of(Key.ALPHA, Key.LETTER_A), pressed);
		assertFalse(alpha.get());
	}

	@Test
	void releasesTheMappingChosenAtPressTime() {
		final AtomicBoolean alpha = new AtomicBoolean(true);
		final List<Key> pressed = new ArrayList<>();
		final List<Key> released = new ArrayList<>();
		final HostKeyboardInput input = new HostKeyboardInput(
				key -> {
					pressed.add(key);
					alpha.set(false);
				},
				released::add,
				() -> false,
				alpha::get,
				ignored -> { });

		input.press(KeyboardAWTValues.VK_LEFT, KeyboardAWTValues.CHAR_UNDEFINED, false);
		input.release(KeyboardAWTValues.VK_LEFT);

		assertEquals(List.of(Key.HISTORY_BACK), pressed);
		assertEquals(List.of(Key.HISTORY_BACK), released);
	}

	@Test
	void modifierAutoRepeatDoesNotToggleTheLayerOff() {
		final AtomicBoolean shift = new AtomicBoolean(false);
		final List<Key> pressed = new ArrayList<>();
		final HostKeyboardInput input = new HostKeyboardInput(
				key -> {
					pressed.add(key);
					if (key == Key.SHIFT)
						shift.set(!shift.get());
				},
				ignored -> { },
				shift::get,
				() -> false,
				ignored -> { });

		assertTrue(input.press(KeyboardAWTValues.VK_SHIFT,
				KeyboardAWTValues.CHAR_UNDEFINED, false));
		assertTrue(input.press(KeyboardAWTValues.VK_SHIFT,
				KeyboardAWTValues.CHAR_UNDEFINED, false));

		assertTrue(shift.get());
		assertEquals(List.of(Key.SHIFT), pressed);
	}

	@Test
	void mapsDirectScientificPunctuation() {
		assertEquals(Key.LESS_THAN, HostKeyboardInput.mapKey(0, '<', false, false, false));
		assertEquals(Key.GREATER_THAN, HostKeyboardInput.mapKey(0, '>', false, false, false));
		assertEquals(Key.PERCENT, HostKeyboardInput.mapKey(0, '%', false, false, false));
		assertEquals(Key.FACTORIAL, HostKeyboardInput.mapKey(0, '!', false, false, false));
	}

	private static void pressAndRelease(final HostKeyboardInput input,
			final int keyCode, final char keyChar) {
		assertTrue(input.press(keyCode, keyChar, false));
		assertTrue(input.release(keyCode));
	}
}
