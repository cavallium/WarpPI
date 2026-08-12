package it.cavallium.warppi.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class HardwareGpioTest {
	@Test
	void mapsKeyboardHeaderPinsToBcmNumbers() {
		assertEquals(13, HardwareGpio.physicalToBcm(33));
		assertEquals(19, HardwareGpio.physicalToBcm(35));
		assertEquals(16, HardwareGpio.physicalToBcm(36));
		assertEquals(26, HardwareGpio.physicalToBcm(37));
		assertEquals(20, HardwareGpio.physicalToBcm(38));
		assertEquals(21, HardwareGpio.physicalToBcm(40));
	}

	@Test
	void rejectsPowerAndOutOfRangePins() {
		assertThrows(IllegalArgumentException.class, () -> HardwareGpio.physicalToBcm(1));
		assertThrows(IllegalArgumentException.class, () -> HardwareGpio.physicalToBcm(41));
	}

	@Test
	void remainsSafeBeforeInitialization() {
		try (HardwareGpio gpio = new HardwareGpio()) {
			assertEquals(gpio.valueUnknownBoardType(), gpio.getBoardType());
			assertThrows(IllegalStateException.class, () -> gpio.digitalWrite(33, false));
		}
	}
}
