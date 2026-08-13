package it.cavallium.warppi.device.input;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.event.KeyPressedEvent;
import it.cavallium.warppi.event.KeyReleasedEvent;
import it.cavallium.warppi.event.KeyboardEventListener;
import it.cavallium.warppi.gui.screens.AppsMenuScreen;
import it.cavallium.warppi.gui.screens.LoadingScreen;

class KeyboardEventDispatchTest {

	@Test
	void opensTheAppsLauncherOnlyFromTheAppsKey() {
		assertInstanceOf(AppsMenuScreen.class, Keyboard.screenOpenedByGlobalKey(Key.SETTINGS));
		assertNull(Keyboard.screenOpenedByGlobalKey(Key.BRIGHTNESS_CYCLE));
		assertNull(Keyboard.screenOpenedByGlobalKey(Key.BRIGHTNESS_CYCLE_REVERSE));
	}

	@Test
	void blocksEveryInputFallbackWhileLoading() {
		assertTrue(Keyboard.isUserInputBlocked(new LoadingScreen()));
		final AppsMenuScreen readyScreen = new AppsMenuScreen();
		assertTrue(Keyboard.isUserInputBlocked(readyScreen));
		readyScreen.initialized = true;
		readyScreen.graphicInitialized = true;
		assertFalse(Keyboard.isUserInputBlocked(readyScreen));
		assertFalse(Keyboard.isUserInputBlocked(null));
	}

	@Test
	void preservesListenerHandledResults() {
		final KeyboardEventListener listener = new KeyboardEventListener() {
			@Override
			public boolean onKeyPressed(final KeyPressedEvent event) {
				return true;
			}

			@Override
			public boolean onKeyReleased(final KeyReleasedEvent event) {
				return true;
			}
		};

		assertTrue(Keyboard.dispatchKeyPressed(listener, new KeyPressedEvent(Key.NUM1)));
		assertTrue(Keyboard.dispatchKeyReleased(listener, new KeyReleasedEvent(Key.NUM1)));
	}

	@Test
	@ResourceLock(Resources.SYSTEM_ERR)
	void containsListenerFailuresForBothKeyEdges() {
		final KeyboardEventListener listener = new KeyboardEventListener() {
			@Override
			public boolean onKeyPressed(final KeyPressedEvent event) {
				throw new IllegalStateException("press failed");
			}

			@Override
			public boolean onKeyReleased(final KeyReleasedEvent event) {
				throw new IllegalStateException("release failed");
			}
		};

		final PrintStream previousErrorStream = System.err;
		final ByteArrayOutputStream errors = new ByteArrayOutputStream();
		try (PrintStream capturedErrors = new PrintStream(errors, true, StandardCharsets.UTF_8)) {
			System.setErr(capturedErrors);
			assertFalse(Keyboard.dispatchKeyPressed(listener, new KeyPressedEvent(Key.NUM1)));
			assertFalse(Keyboard.dispatchKeyReleased(listener, new KeyReleasedEvent(Key.NUM1)));
		} finally {
			System.setErr(previousErrorStream);
		}
		final String errorOutput = errors.toString(StandardCharsets.UTF_8);
		assertTrue(errorOutput.contains("press failed"));
		assertTrue(errorOutput.contains("release failed"));
	}
}
