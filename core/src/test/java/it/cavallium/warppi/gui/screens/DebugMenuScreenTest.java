package it.cavallium.warppi.gui.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.gui.screens.DebugMenuScreen.DebugAction;

class DebugMenuScreenTest {

	@Test
	void exposesTheDestructiveAndDiagnosticActionsInAStableOrder() {
		assertEquals(List.of(DebugAction.CRASH_RENDER, DebugAction.BACKGROUND_LOAD,
				DebugAction.KEYBOARD_DIAGNOSTICS, DebugAction.UI_GPU_DIAGNOSTICS),
				DebugMenuScreen.actions());
		for (final DebugAction action : DebugMenuScreen.actions()) {
			assertEquals(action.label().toUpperCase(), action.label());
			assertEquals(action.description().toUpperCase(), action.description());
			assertEquals(action.badge().toUpperCase(), action.badge());
		}
	}

	@Test
	void wrapsSelectionAndMapsQuickActionKeys() {
		assertEquals(3, DebugMenuScreen.nextSelection(0, -1));
		assertEquals(0, DebugMenuScreen.nextSelection(3, 1));
		assertEquals(2, DebugMenuScreen.nextSelection(1, 1));
		assertEquals(0, DebugMenuScreen.shortcutIndex(Key.NUM1));
		assertEquals(1, DebugMenuScreen.shortcutIndex(Key.NUM2));
		assertEquals(2, DebugMenuScreen.shortcutIndex(Key.NUM3));
		assertEquals(3, DebugMenuScreen.shortcutIndex(Key.NUM4));
		assertEquals(-1, DebugMenuScreen.shortcutIndex(Key.NUM5));
	}

	@Test
	void opensTheExistingDiagnosticScreens() {
		assertInstanceOf(KeyboardDebugScreen.class,
				DebugMenuScreen.createScreen(DebugAction.KEYBOARD_DIAGNOSTICS));
		assertInstanceOf(UIDemoScreen.class,
				DebugMenuScreen.createScreen(DebugAction.UI_GPU_DIAGNOSTICS));
		assertThrows(IllegalArgumentException.class,
				() -> DebugMenuScreen.createScreen(DebugAction.CRASH_RENDER));
	}

	@Test
	void deliberateCrashIsThrownFromTheNextRenderCallOnly() {
		final DebugMenuScreen screen = new DebugMenuScreen();
		screen.armRenderCrash();

		final AssertionError failure = assertThrows(AssertionError.class,
				() -> screen.render(null));

		assertEquals("Deliberate UI render-loop crash from Debug Tools", failure.getMessage());
	}

	@Test
	void backgroundLoadKeepsTheGlobalIndicatorBalancedAndRejectsDuplicates()
			throws InterruptedException {
		final DebugMenuScreen screen = new DebugMenuScreen(100, worker -> {
			worker.setDaemon(true);
			worker.start();
		});
		assertFalse(WarpPI.INSTANCE.isLoading());

		screen.triggerBackgroundLoad();
		screen.triggerBackgroundLoad();

		assertTrue(screen.isBackgroundLoadRunning());
		assertTrue(WarpPI.INSTANCE.isLoading());
		final long deadline = System.nanoTime() + 2_000_000_000L;
		while (screen.isBackgroundLoadRunning() && System.nanoTime() < deadline)
			Thread.sleep(5);

		assertFalse(screen.isBackgroundLoadRunning());
		assertFalse(WarpPI.INSTANCE.isLoading());
	}
}
