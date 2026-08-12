package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class JOGLFontTest {

	@Test
	void singleCharacterAndStringLookupUseTheSameFontOffset() throws IOException {
		final JOGLFont font = new JOGLFont("smallest");
		font.minCharIndex = 32;
		font.intervals = new int[] { 0, 95, 96 };

		assertEquals(33, font.getCharIndex('A'));
		assertArrayEquals(new int[] { 33, 34 }, font.getCharIndexes("AB"));
	}
}
