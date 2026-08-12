package jogamp.newt.driver.x11;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jogamp.newt.driver.KeyTracker;
import jogamp.newt.driver.MouseTracker;

class X11UnderlayTrackerTest {
	@Test
	void disablesX11AndPreservesBcmFallbackContract() {
		assertTrue(MouseTracker.class.isAssignableFrom(X11UnderlayTracker.class));
		assertTrue(KeyTracker.class.isAssignableFrom(X11UnderlayTracker.class));

		final ExceptionInInitializerError failure = assertThrows(
				ExceptionInInitializerError.class,
				X11UnderlayTracker::getSingleton);
		assertEquals("X11 underlay disabled for native Raspberry VideoCore mode", failure.getMessage());
	}
}
