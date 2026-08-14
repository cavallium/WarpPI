package it.cavallium.warppi.gui.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.event.Key;
import it.cavallium.warppi.extra.mario.MarioScreen;
import it.cavallium.warppi.extra.tetris.TetrisScreen;
import it.cavallium.warppi.gui.screens.AppsMenuScreen.App;

class AppsMenuScreenTest {

	@Test
	void alwaysOffersTheCalculatorGamesGuideAndSettings() {
		assertEquals(List.of(App.CALCULATOR, App.TETRIS, App.MARIO,
				App.KEYBOARD_GUIDE, App.SETTINGS),
				AppsMenuScreen.availableApps(false));
	}

	@Test
	void addsDiagnosticUisOnlyWhenDebugIsEnabled() {
		assertEquals(List.of(App.CALCULATOR, App.TETRIS, App.MARIO,
				App.KEYBOARD_GUIDE, App.SETTINGS,
				App.DEBUG_TOOLS), AppsMenuScreen.availableApps(true));
	}

	@Test
	void usesGlyphsSupportedByTheCalculatorMenuFont() {
		for (final App app : App.values()) {
			assertEquals(app.label().toUpperCase(), app.label());
			assertEquals(app.subtitle().toUpperCase(), app.subtitle());
			assertEquals(app.badge().toUpperCase(), app.badge());
		}
	}

	@Test
	void navigatesTheHeroAndTwoColumnGridSpatially() {
		final int normalAppCount = AppsMenuScreen.availableApps(false).size();
		assertEquals(1, AppsMenuScreen.nextSelection(0, normalAppCount, Key.DOWN));
		assertEquals(2, AppsMenuScreen.nextSelection(1, normalAppCount, Key.RIGHT));
		assertEquals(3, AppsMenuScreen.nextSelection(1, normalAppCount, Key.DOWN));
		assertEquals(4, AppsMenuScreen.nextSelection(3, normalAppCount, Key.RIGHT));
		assertEquals(0, AppsMenuScreen.nextSelection(4, normalAppCount, Key.DOWN));
		assertEquals(4, AppsMenuScreen.nextSelection(0, normalAppCount, Key.UP));
		assertEquals(0, AppsMenuScreen.nextSelection(2, normalAppCount, Key.UP));

		final int debugAppCount = AppsMenuScreen.availableApps(true).size();
		assertEquals(5, AppsMenuScreen.nextSelection(3, debugAppCount, Key.DOWN));
		assertEquals(5, AppsMenuScreen.nextSelection(5, debugAppCount, Key.RIGHT));
	}

	@Test
	void mapsNumberKeysToTheVisibleAppOrder() {
		assertEquals(0, AppsMenuScreen.shortcutIndex(Key.NUM1));
		assertEquals(1, AppsMenuScreen.shortcutIndex(Key.NUM2));
		assertEquals(2, AppsMenuScreen.shortcutIndex(Key.NUM3));
		assertEquals(3, AppsMenuScreen.shortcutIndex(Key.NUM4));
		assertEquals(4, AppsMenuScreen.shortcutIndex(Key.NUM5));
		assertEquals(5, AppsMenuScreen.shortcutIndex(Key.NUM6));
		assertEquals(-1, AppsMenuScreen.shortcutIndex(Key.NUM7));
		assertEquals(-1, AppsMenuScreen.shortcutIndex(Key.NUM0));
		assertEquals(-1, AppsMenuScreen.shortcutIndex(Key.OK));
	}

	@Test
	void createsTheRequestedApplicationScreen() {
		assertInstanceOf(HardwareKeyboardGuideScreen.class,
				AppsMenuScreen.createScreen(App.KEYBOARD_GUIDE));
		assertInstanceOf(TetrisScreen.class, AppsMenuScreen.createScreen(App.TETRIS));
		assertInstanceOf(MarioScreen.class, AppsMenuScreen.createScreen(App.MARIO));
		assertInstanceOf(DebugMenuScreen.class,
				AppsMenuScreen.createScreen(App.DEBUG_TOOLS));
	}
}
