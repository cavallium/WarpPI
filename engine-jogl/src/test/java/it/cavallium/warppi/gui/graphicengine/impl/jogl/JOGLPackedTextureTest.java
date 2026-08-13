package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.util.texture.Texture;
import org.junit.jupiter.api.Test;

class JOGLPackedTextureTest {

	@Test
	void mapsOnlyPngResourcesIntoTheHardwareNamespace() {
		assertEquals("/texture-atlas/skin.wpt", JOGLPackedTexture.resourcePath("skin.png"));
		assertEquals("/texture-atlas/nested/skin.wpt",
				JOGLPackedTexture.resourcePath("nested\\skin.PNG"));
		assertNull(JOGLPackedTexture.resourcePath("skin.jpg"));
		assertNull(JOGLPackedTexture.resourcePath(null));
	}

	@Test
	void selectsPackedAssetsOnlyForRaspberryProduction() {
		assertTrue(JOGLSkin.shouldUsePackedTexture(true, true));
		assertFalse(JOGLSkin.shouldUsePackedTexture(false, true));
		assertFalse(JOGLSkin.shouldUsePackedTexture(true, false));
		assertFalse(JOGLSkin.shouldUsePackedTexture(false, false));
	}

	@Test
	void preservesTheNativePalettePayloadWithoutDecoding() throws Exception {
		final byte[] packed = palette4Texture(false);
		final JOGLPackedTexture.Decoded decoded = JOGLPackedTexture.read(
				new ByteArrayInputStream(packed), true);

		assertEquals(2, decoded.width());
		assertEquals(2, decoded.height());
		assertEquals(JOGLPackedTexture.supportsNativePalettedUpload(), decoded.compressed());
		assertEquals(JOGLPackedTexture.PixelFormat.RGBA4, decoded.pixelFormat());
		if (decoded.compressed()) {
			assertEquals(16 * Short.BYTES + 2, decoded.pixels().remaining());
			assertEquals(0x01, decoded.pixels().get(16 * Short.BYTES) & 0xFF);
			assertEquals(0x10, decoded.pixels().get(16 * Short.BYTES + 1) & 0xFF);
		}
	}

	@Test
	void expandsTheSamePaletteLosslesslyWhenTheExtensionIsUnavailable() throws Exception {
		final JOGLPackedTexture.Decoded decoded = JOGLPackedTexture.read(
				new ByteArrayInputStream(palette4Texture(false)), false);

		assertFalse(decoded.compressed());
		assertEquals(2 * 2 * Short.BYTES, decoded.pixels().remaining());
		final ByteBuffer pixels = decoded.pixels().duplicate().order(ByteOrder.nativeOrder());
		assertEquals(0x1234, pixels.getShort(0) & 0xFFFF);
		assertEquals(0xABCD, pixels.getShort(2) & 0xFFFF);
		assertEquals(0xABCD, pixels.getShort(4) & 0xFFFF);
		assertEquals(0x1234, pixels.getShort(6) & 0xFFFF);
	}

	@Test
	void uploadsRgba4FallbackDirectlyWithoutJoglsUnsupportedPixelMapper() throws Exception {
		final JOGLPackedTexture.Decoded decoded = JOGLPackedTexture.read(
				new ByteArrayInputStream(palette4Texture(false)), false);
		final Object[][] texImageCall = new Object[1][];
		final List<Integer> unpackAlignments = new ArrayList<>();
		final GL gl = (GL) Proxy.newProxyInstance(GL.class.getClassLoader(),
				new Class<?>[] { GL.class }, (proxy, method, arguments) -> {
					switch (method.getName()) {
						case "glGenTextures" -> {
							final int[] ids = (int[]) arguments[1];
							ids[(int) arguments[2]] = 17;
							return null;
						}
						case "glTexImage2D" -> {
							texImageCall[0] = arguments.clone();
							return null;
						}
						case "glPixelStorei" -> {
							if ((int) arguments[0] == GL.GL_UNPACK_ALIGNMENT)
								unpackAlignments.add((int) arguments[1]);
							return null;
						}
						case "glGetError" -> {
							return GL.GL_NO_ERROR;
						}
						default -> {
							return defaultValue(method.getReturnType());
						}
					}
				});

		final Texture texture = decoded.upload(gl);

		assertEquals(17, texture.getTextureObject());
		assertNotNull(texImageCall[0]);
		assertEquals(GL.GL_TEXTURE_2D, texImageCall[0][0]);
		assertEquals(GL.GL_RGBA, texImageCall[0][2]);
		assertEquals(2, texImageCall[0][3]);
		assertEquals(2, texImageCall[0][4]);
		assertEquals(GL.GL_RGBA, texImageCall[0][6]);
		assertEquals(GL.GL_UNSIGNED_SHORT_4_4_4_4, texImageCall[0][7]);
		assertTrue(((ByteBuffer) texImageCall[0][8]).isDirect());
		assertEquals(List.of(1, 4), unpackAlignments);
	}

	@Test
	void decodesRawRgb565IntoNativePackedPixels() throws Exception {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			JOGLPackedTexture.writeHeader(output, 2, 2, JOGLPackedTexture.Encoding.RGB565);
			writePacked16(output, 0xF800);
			writePacked16(output, 0x07E0);
			writePacked16(output, 0x001F);
			writePacked16(output, 0xFFFF);
		}

		final JOGLPackedTexture.Decoded decoded = JOGLPackedTexture.read(
				new ByteArrayInputStream(bytes.toByteArray()), false);
		final ByteBuffer pixels = decoded.pixels().duplicate().order(ByteOrder.nativeOrder());
		assertFalse(decoded.compressed());
		assertEquals(JOGLPackedTexture.PixelFormat.RGB565, decoded.pixelFormat());
		assertEquals(0xF800, pixels.getShort(0) & 0xFFFF);
		assertEquals(0x07E0, pixels.getShort(2) & 0xFFFF);
		assertEquals(0x001F, pixels.getShort(4) & 0xFFFF);
		assertEquals(0xFFFF, pixels.getShort(6) & 0xFFFF);
	}

	@Test
	void roundsArgbComponentsIntoTheNativeProductionFormats() {
		assertEquals(0xFFFF, JOGLPackedTexture.PixelFormat.RGB565.packArgb(0xFFFFFFFF));
		assertEquals(0xF800, JOGLPackedTexture.PixelFormat.RGB565.packArgb(0xFFFF0000));
		assertEquals(0x07E0, JOGLPackedTexture.PixelFormat.RGB565.packArgb(0xFF00FF00));
		assertEquals(0x001F, JOGLPackedTexture.PixelFormat.RGB565.packArgb(0xFF0000FF));
		assertEquals(0xF00F, JOGLPackedTexture.PixelFormat.RGBA4.packArgb(0xFFFF0000));
		assertEquals(0xF800, JOGLPackedTexture.PixelFormat.RGB5_A1.packArgb(0x7FFF0000));
		assertEquals(0xF801, JOGLPackedTexture.PixelFormat.RGB5_A1.packArgb(0x80FF0000));
	}

	@Test
	void rejectsTrailingOrTruncatedPayloads() throws Exception {
		assertThrows(IOException.class, () -> JOGLPackedTexture.read(
				new ByteArrayInputStream(palette4Texture(true)), true));

		final byte[] complete = palette4Texture(false);
		final byte[] truncated = java.util.Arrays.copyOf(complete, complete.length - 1);
		assertThrows(IOException.class, () -> JOGLPackedTexture.read(
				new ByteArrayInputStream(truncated), false));
	}

	@Test
	void rejectsInvalidHeadersBeforeAllocatingPixelStorage() throws Exception {
		assertThrows(IOException.class, () -> JOGLPackedTexture.read(
				new ByteArrayInputStream(header(0x12345678, 2, 2, 1)), true));
		assertThrows(IOException.class, () -> JOGLPackedTexture.read(
				new ByteArrayInputStream(header(JOGLPackedTexture.MAGIC, 3, 2, 1)), true));
		assertThrows(IOException.class, () -> JOGLPackedTexture.read(
				new ByteArrayInputStream(header(JOGLPackedTexture.MAGIC,
						JOGLPackedTexture.MAX_TEXTURE_DIMENSION + 1, 2, 1)), true));
		assertThrows(IOException.class, () -> JOGLPackedTexture.read(
				new ByteArrayInputStream(header(JOGLPackedTexture.MAGIC, 2, 2, 99)), true));
	}

	private static byte[] palette4Texture(final boolean appendTrailingByte) throws IOException {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			JOGLPackedTexture.writeHeader(output, 2, 2,
					JOGLPackedTexture.Encoding.PALETTE4_RGBA4);
			writePacked16(output, 0x1234);
			writePacked16(output, 0xABCD);
			for (int i = 2; i < 16; i++)
				writePacked16(output, 0);
			output.writeByte(0x01);
			output.writeByte(0x10);
			if (appendTrailingByte)
				output.writeByte(0x7F);
		}
		return bytes.toByteArray();
	}

	private static void writePacked16(final DataOutputStream output, final int value)
			throws IOException {
		output.writeByte(value);
		output.writeByte(value >>> 8);
	}

	private static byte[] header(final int magic, final int width, final int height,
			final int encoding) throws IOException {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream output = new DataOutputStream(bytes)) {
			output.writeInt(magic);
			output.writeShort(width);
			output.writeShort(height);
			output.writeByte(encoding);
		}
		return bytes.toByteArray();
	}

	private static Object defaultValue(final Class<?> type) {
		if (!type.isPrimitive() || type == void.class)
			return null;
		if (type == boolean.class)
			return false;
		if (type == char.class)
			return '\0';
		if (type == byte.class)
			return (byte) 0;
		if (type == short.class)
			return (short) 0;
		if (type == int.class)
			return 0;
		if (type == long.class)
			return 0L;
		if (type == float.class)
			return 0f;
		if (type == double.class)
			return 0d;
		throw new AssertionError("Unsupported primitive return type: " + type);
	}
}
