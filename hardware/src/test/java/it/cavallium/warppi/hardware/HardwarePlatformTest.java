package it.cavallium.warppi.hardware;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.boot.Boot;

class HardwarePlatformTest {
	@Test
	void configuresEveryRequiredDevice() {
		final HardwarePlatform platform = new HardwarePlatform();

		platform.setArguments(Boot.parseStartupArguments(new String[0]));

		assertNotNull(platform.getDisplayOutputDevice());
		assertNotNull(platform.getBacklightOutputDevice());
		assertNotNull(platform.getKeyboardInputDevice());
		assertNotNull(platform.getTouchInputDevice());
		assertNotNull(platform.getDeviceStateDevice());
	}
}
