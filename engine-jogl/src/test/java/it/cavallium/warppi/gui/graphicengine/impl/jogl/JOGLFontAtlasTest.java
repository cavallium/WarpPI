package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class JOGLFontAtlasTest {

	@ParameterizedTest
	@ValueSource(strings = { "smal", "smallest", "norm" })
	void generatedAtlasMatchesItsRftSource(final String name) throws IOException {
		assertGeneratedAtlasMatchesSource(name);
	}

	@ParameterizedTest
	@ValueSource(strings = { "gputest1", "gputest2", "gputest12" })
	void generatesIgnoredDiagnosticAtlasWhenItsPrivateSourceIsPresent(final String name) throws IOException {
		assumeTrue(JOGLFontAtlasTest.class.getResource("/font_" + name + ".rft") != null,
				"Private GPU-test source is not installed");
		assertGeneratedAtlasMatchesSource(name);
	}

	private static void assertGeneratedAtlasMatchesSource(final String name) throws IOException {
		final JOGLFontAtlas expected;
		try (InputStream input = resource("/font_" + name + ".rft")) {
			expected = JOGLFontAtlas.fromRft(input);
		}
		final JOGLFontAtlas generated;
		try (InputStream input = resource("/font-atlas/font_" + name + ".wfa")) {
			generated = JOGLFontAtlas.read(input);
		}
		assertEquals(expected, generated);
		assertEquals(generated.textureW * generated.textureH, generated.alphaPixels.length);
	}

	@Test
	void joglFontLoadsTheGeneratedAtlasWithoutAnOpenGlContext() throws IOException {
		final JOGLFont font = new JOGLFont("smal");
		assertEquals(6, font.getCharacterWidth());
		assertEquals(9, font.getCharacterHeight());
		assertEquals(64, font.textureW);
		assertEquals(128, font.textureH);
		assertFalse(font.isInitialized());
	}

	@Test
	void atlasFormatRoundTripsWithoutChangingPixels() throws IOException {
		final JOGLFontAtlas source;
		try (InputStream input = resource("/font_smal.rft")) {
			source = JOGLFontAtlas.fromRft(input);
		}
		final ByteArrayOutputStream encoded = new ByteArrayOutputStream();
		source.write(encoded);
		final JOGLFontAtlas decoded = JOGLFontAtlas.read(new ByteArrayInputStream(encoded.toByteArray()));
		assertEquals(source, decoded);
	}

	@Test
	void storesTopRftRowsAtTheTopOfTheOpenGlTexture() throws IOException {
		final ByteArrayOutputStream encoded = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(encoded)) {
			output.write(new byte[] { 'r', 'a', 'w', (byte) 0xFF });
			output.writeShort(1);
			output.writeShort(2);
			output.writeByte(0xFF);
			output.writeInt(0);
			output.writeByte(0xFF);
			output.writeInt(1);
			output.writeShort(0);
			output.writeByte(0x80); // Top pixel on, bottom pixel off.
		}

		final JOGLFontAtlas atlas = JOGLFontAtlas.fromRft(new ByteArrayInputStream(encoded.toByteArray()));
		assertEquals(1, atlas.textureW);
		assertEquals(2, atlas.textureH);
		assertArrayEquals(new byte[] { 0, (byte) 0xFF }, atlas.alphaPixels);
	}

	private static InputStream resource(final String path) {
		final InputStream input = JOGLFontAtlasTest.class.getResourceAsStream(path);
		assertNotNull(input, () -> "Missing test resource " + path);
		return input;
	}
}
