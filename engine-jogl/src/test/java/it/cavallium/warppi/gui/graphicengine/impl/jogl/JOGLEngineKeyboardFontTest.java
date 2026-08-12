package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class JOGLEngineKeyboardFontTest {

	@Test
	void usesTheReadableSmallFontForTheVirtualKeyboard() throws IOException {
		final JOGLEngine engine = new JOGLEngine(480, 320);

		engine.loadFont("smallest");
		assertNull(engine.getKeyboardFont());
		final JOGLFont keyboardFont = (JOGLFont) engine.loadFont("smal");
		assertSame(keyboardFont, engine.getKeyboardFont());
		engine.loadFont("norm");
		assertSame(keyboardFont, engine.getKeyboardFont());
	}
}
