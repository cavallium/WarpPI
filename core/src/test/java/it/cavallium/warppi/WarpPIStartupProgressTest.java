package it.cavallium.warppi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WarpPIStartupProgressTest {

	@Test
	void interpolatesCompletedWorkWithinAMilestoneRange() {
		assertEquals(44, WarpPI.interpolateLoadingProgress(44, 68, 0, 6));
		assertEquals(56, WarpPI.interpolateLoadingProgress(44, 68, 3, 6));
		assertEquals(68, WarpPI.interpolateLoadingProgress(44, 68, 6, 6));
	}

	@Test
	void clampsWorkAndCompletesAnEmptyRange() {
		assertEquals(44, WarpPI.interpolateLoadingProgress(44, 68, -1, 6));
		assertEquals(68, WarpPI.interpolateLoadingProgress(44, 68, 7, 6));
		assertEquals(68, WarpPI.interpolateLoadingProgress(44, 68, 0, 0));
	}

	@Test
	void keepsTheLoadingIndicatorVisibleUntilNestedWorkCompletes() {
		assertFalse(WarpPI.INSTANCE.isLoading());
		WarpPI.INSTANCE.beginLoadingOperation();
		try {
			assertTrue(WarpPI.INSTANCE.isLoading());
			WarpPI.INSTANCE.beginLoadingOperation();
			try {
				assertTrue(WarpPI.INSTANCE.isLoading());
			} finally {
				WarpPI.INSTANCE.endLoadingOperation();
			}
			assertTrue(WarpPI.INSTANCE.isLoading());
		} finally {
			WarpPI.INSTANCE.endLoadingOperation();
		}
		assertFalse(WarpPI.INSTANCE.isLoading());
	}

	@Test
	void rejectsAnUnbalancedLoadingOperationEnd() {
		assertFalse(WarpPI.INSTANCE.isLoading());
		assertThrows(IllegalStateException.class, WarpPI.INSTANCE::endLoadingOperation);
		assertFalse(WarpPI.INSTANCE.isLoading());
	}
}
