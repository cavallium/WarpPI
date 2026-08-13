package it.cavallium.warppi.gui.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.Platform.Settings;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.event.KeyPressedEvent;

class SettingsMenuScreenTest {

	@Test
	void togglesDebugAndAdjustsBrightness() {
		final MutableSettings settings = new MutableSettings();
		final SettingsMenuScreen screen = new SettingsMenuScreen(settings);

		assertFalse(settings.isDebugEnabled());
		assertTrue(screen.onKeyPressed(new KeyPressedEvent(Key.OK)));
		assertTrue(settings.isDebugEnabled());

		assertTrue(screen.onKeyPressed(new KeyPressedEvent(Key.DOWN)));
		assertTrue(screen.onKeyPressed(new KeyPressedEvent(Key.RIGHT)));
		assertEquals(0.3d, settings.getBrightness(), 0.0001d);
		assertTrue(screen.onKeyPressed(new KeyPressedEvent(Key.LEFT)));
		assertEquals(0.2d, settings.getBrightness(), 0.0001d);
	}

	@Test
	void clampsBrightnessAtBothEnds() {
		final MutableSettings settings = new MutableSettings();
		final SettingsMenuScreen screen = new SettingsMenuScreen(settings);

		settings.setBrightness(1d);
		screen.onKeyPressed(new KeyPressedEvent(Key.BRIGHTNESS_CYCLE));
		assertEquals(1d, settings.getBrightness());
		settings.setBrightness(0d);
		screen.onKeyPressed(new KeyPressedEvent(Key.BRIGHTNESS_CYCLE_REVERSE));
		assertEquals(0d, settings.getBrightness());
	}

	private static final class MutableSettings implements Settings {
		private boolean debug;
		private double brightness = 0.2d;

		@Override
		public boolean isDebugEnabled() {
			return debug;
		}

		@Override
		public void setDebugEnabled(final boolean debug) {
			this.debug = debug;
		}

		@Override
		public double getBrightness() {
			return brightness;
		}

		@Override
		public void setBrightness(final double brightness) {
			this.brightness = brightness;
		}
	}
}
