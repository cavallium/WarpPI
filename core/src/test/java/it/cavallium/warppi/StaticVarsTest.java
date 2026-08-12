package it.cavallium.warppi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class StaticVarsTest {

	@AfterEach
	void resetZoom() {
		StaticVars.initializeWindowZoom(false);
	}

	@Test
	void initializesNormalWindowZoom() {
		StaticVars.initializeWindowZoom(false);

		assertEquals(1F, StaticVars.windowZoom.getLastValue());
	}

	@Test
	void initializesDoubleWindowZoom() {
		StaticVars.initializeWindowZoom(true);

		assertEquals(2F, StaticVars.windowZoom.getLastValue());
	}
}
