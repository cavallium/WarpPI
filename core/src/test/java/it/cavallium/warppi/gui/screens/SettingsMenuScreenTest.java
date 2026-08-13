package it.cavallium.warppi.gui.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.Platform.Settings;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.gui.screens.SettingsMenuScreen.SettingItem;

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

	@Test
	void hidesAndDisablesBrightnessWhenTheDeviceDoesNotSupportIt() {
		final MutableSettings settings = new MutableSettings();
		final SettingsMenuScreen screen = new SettingsMenuScreen(settings, false);

		assertEquals(List.of(SettingItem.DEBUG_UI),
				SettingsMenuScreen.availableSettings(false));
		assertTrue(screen.mustBeRefreshed());
		assertFalse(screen.mustBeRefreshed());
		assertTrue(screen.onKeyPressed(new KeyPressedEvent(Key.DOWN)));
		assertFalse(screen.mustBeRefreshed());
		assertTrue(screen.onKeyPressed(new KeyPressedEvent(Key.BRIGHTNESS_CYCLE)));
		assertTrue(screen.onKeyPressed(new KeyPressedEvent(Key.BRIGHTNESS_CYCLE_REVERSE)));
		assertEquals(0.2d, settings.getBrightness());
		assertEquals(0, settings.brightnessWrites);

		assertTrue(screen.onKeyPressed(new KeyPressedEvent(Key.OK)));
		assertTrue(settings.isDebugEnabled());
	}

	@Test
	void exposesBrightnessOnlyForCapableDevices() {
		assertEquals(List.of(SettingItem.DEBUG_UI, SettingItem.BRIGHTNESS),
				SettingsMenuScreen.availableSettings(true));
	}

	@Test
	void computesStableBrightnessStepsAndDisplayValues() {
		assertEquals(0.3d, SettingsMenuScreen.adjustedBrightness(0.2d, 1));
		assertEquals(0.1d, SettingsMenuScreen.adjustedBrightness(0.2d, -1));
		assertEquals(1d, SettingsMenuScreen.adjustedBrightness(1d, 1));
		assertEquals(0d, SettingsMenuScreen.adjustedBrightness(0d, -1));
		assertEquals(20, SettingsMenuScreen.brightnessPercent(Double.NaN));
		assertEquals(0, SettingsMenuScreen.brightnessPercent(-1d));
		assertEquals(100, SettingsMenuScreen.brightnessPercent(2d));
	}

	private static final class MutableSettings implements Settings {
		private boolean debug;
		private double brightness = 0.2d;
		private int brightnessWrites;

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
			brightnessWrites++;
		}
	}
}
