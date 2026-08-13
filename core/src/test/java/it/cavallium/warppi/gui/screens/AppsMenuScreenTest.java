package it.cavallium.warppi.gui.screens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.extra.mario.MarioScreen;
import it.cavallium.warppi.extra.tetris.TetrisScreen;
import it.cavallium.warppi.gui.screens.AppsMenuScreen.App;

class AppsMenuScreenTest {

	@Test
	void alwaysOffersTheGuideGamesAndSettings() {
		assertEquals(List.of(App.KEYBOARD_GUIDE, App.TETRIS, App.MARIO, App.SETTINGS),
				AppsMenuScreen.availableApps(false));
	}

	@Test
	void addsDiagnosticUisOnlyWhenDebugIsEnabled() {
		assertEquals(List.of(App.KEYBOARD_GUIDE, App.TETRIS, App.MARIO, App.SETTINGS,
				App.KEYBOARD_DEBUG, App.UI_DEMO), AppsMenuScreen.availableApps(true));
	}

	@Test
	void usesGlyphsSupportedByTheCalculatorMenuFont() {
		for (final App app : App.values())
			assertEquals(app.label().toUpperCase(), app.label());
	}

	@Test
	void createsTheRequestedApplicationScreen() {
		assertInstanceOf(HardwareKeyboardGuideScreen.class,
				AppsMenuScreen.createScreen(App.KEYBOARD_GUIDE));
		assertInstanceOf(TetrisScreen.class, AppsMenuScreen.createScreen(App.TETRIS));
		assertInstanceOf(MarioScreen.class, AppsMenuScreen.createScreen(App.MARIO));
		assertInstanceOf(KeyboardDebugScreen.class,
				AppsMenuScreen.createScreen(App.KEYBOARD_DEBUG));
		assertInstanceOf(UIDemoScreen.class, AppsMenuScreen.createScreen(App.UI_DEMO));
	}
}
