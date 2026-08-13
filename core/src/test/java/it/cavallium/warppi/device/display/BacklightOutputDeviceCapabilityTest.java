package it.cavallium.warppi.device.display;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class BacklightOutputDeviceCapabilityTest {

	@Test
	void defaultsToUnsupportedForUnknownAndNoOpBacklights() {
		final BacklightOutputDevice unknownBacklight = new BacklightOutputDevice() {
			@Override
			public void setBrightness(final double value) {
			}

			@Override
			public void setPower(final boolean value) {
			}
		};

		assertFalse(unknownBacklight.supportsBrightnessControl());
		assertFalse(new NullBacklightOutputDevice().supportsBrightnessControl());
	}
}
