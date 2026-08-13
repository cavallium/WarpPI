package it.cavallium.warppi.gui.screens;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.event.KeyReleasedEvent;

class HardwareKeyboardGuideScreenTest {

	@Test
	void refreshesAndConsumesKeyReleasesSoPressedHighlightsAreCleared() {
		final HardwareKeyboardGuideScreen screen = new HardwareKeyboardGuideScreen();
		assertTrue(screen.mustBeRefreshed());
		assertFalse(screen.mustBeRefreshed());

		assertTrue(screen.onKeyReleased(new KeyReleasedEvent(Key.NUM1)));
		assertTrue(screen.mustBeRefreshed());
	}
}
