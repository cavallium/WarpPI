package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

class JOGLFontTest {

	@Test
	void rebuildsMissingAtlasInMemoryOutsideProduction() throws IOException {
		final JOGLFontAtlas expected;
		try (InputStream input = JOGLFontTest.class.getResourceAsStream("/font_smal.rft")) {
			expected = JOGLFontAtlas.fromRft(input);
		}

		final JOGLFontAtlas actual = JOGLFont.loadAtlas("smal", true,
				resource -> {
					if (resource.endsWith(".wfa"))
						throw new AssertionError("Non-production must prefer the RFT source");
					return JOGLFontTest.class.getResourceAsStream(resource);
				});

		assertEquals(expected, actual);
	}

	@Test
	void requiresPrebuiltAtlasInProduction() {
		assertThrows(IOException.class, () -> JOGLFont.loadAtlas("smal", false, resource -> {
			if (resource.endsWith(".rft"))
				throw new AssertionError("Production must not load the RFT source");
			return null;
		}));
	}

	@Test
	void singleCharacterAndStringLookupUseTheSameFontOffset() throws IOException {
		final JOGLFont font = new JOGLFont("smallest");
		font.minCharIndex = 32;
		font.intervals = new int[] { 0, 95, 96 };

		assertEquals(33, font.getCharIndex('A'));
		assertArrayEquals(new int[] { 33, 34 }, font.getCharIndexes("AB"));
	}
}
