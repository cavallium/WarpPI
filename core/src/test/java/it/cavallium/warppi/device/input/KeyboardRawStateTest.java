package it.cavallium.warppi.device.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.cavallium.warppi.event.Key;

class KeyboardRawStateTest {

	@BeforeEach
	@AfterEach
	void resetState() {
		Keyboard.resetRawKeyState();
	}

	@Test
	void releasesTheKeySelectedWhenTheMatrixButtonWasPressed() {
		assertEquals(Key.PLUS_MINUS, Keyboard.updateRawKeyState(7, 3, false, true, false));

		assertEquals(Key.PLUS_MINUS, Keyboard.updateRawKeyState(7, 3, true, false, false));
	}

	@Test
	void ignoresDuplicateEdges() {
		assertEquals(Key.NUM1, Keyboard.updateRawKeyState(7, 0, false, false, false));
		assertNull(Keyboard.updateRawKeyState(7, 0, false, false, false));
		assertEquals(Key.NUM1, Keyboard.updateRawKeyState(7, 0, true, false, false));
		assertNull(Keyboard.updateRawKeyState(7, 0, true, false, false));
	}

	@Test
	void rejectsInvalidCoordinates() {
		assertThrows(IllegalArgumentException.class,
				() -> Keyboard.updateRawKeyState(8, 0, false, false, false));
	}
}
