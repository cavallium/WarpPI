package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Locale;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;

/**
 * Build-time encoded texture data for the memory-constrained Raspberry path.
 * Palette payloads match {@code GL_OES_compressed_paletted_texture} exactly,
 * allowing a supported GPU to consume the resource without a runtime decode.
 */
final class JOGLPackedTexture {

	static final int MAGIC = 0x57505432; // WPT2
	static final String PALETTED_TEXTURE_EXTENSION = "GL_OES_compressed_paletted_texture";
	static final int MAX_TEXTURE_DIMENSION = 2_048;

	private static final int GL_PALETTE4_R5_G6_B5_OES = 0x8B92;
	private static final int GL_PALETTE4_RGBA4_OES = 0x8B93;
	private static final int GL_PALETTE4_RGB5_A1_OES = 0x8B94;
	private static final int GL_PALETTE8_R5_G6_B5_OES = 0x8B97;
	private static final int GL_PALETTE8_RGBA4_OES = 0x8B98;
	private static final int GL_PALETTE8_RGB5_A1_OES = 0x8B99;
	private static final int COPY_BUFFER_SIZE = 8_192;

	private JOGLPackedTexture() {}

	static String resourcePath(final String pngResource) {
		if (pngResource == null)
			return null;
		String normalized = pngResource.replace('\\', '/');
		if (!normalized.startsWith("/"))
			normalized = "/" + normalized;
		if (!normalized.toLowerCase(Locale.ROOT).endsWith(".png"))
			return null;
		return "/texture-atlas" + normalized.substring(0, normalized.length() - 4) + ".wpt";
	}

	static Decoded read(final InputStream input, final boolean usePalettedCompression)
			throws IOException {
		if (input == null)
			throw new IOException("Missing preprocessed texture input");

		try (DataInputStream in = new DataInputStream(input)) {
			if (in.readInt() != MAGIC)
				throw new IOException("Invalid preprocessed texture magic");
			final int width = in.readUnsignedShort();
			final int height = in.readUnsignedShort();
			final Encoding encoding = Encoding.fromId(in.readUnsignedByte());
			validateDimensions(width, height);
			final int pixelCount = Math.multiplyExact(width, height);

			if (encoding.isPaletted() && usePalettedCompression
					&& supportsNativePalettedUpload()) {
				final ByteBuffer payload = readDirect(in, encoding.payloadBytes(pixelCount));
				requireEndOfStream(in);
				return new Decoded(width, height, encoding.glInternalFormat, true,
						encoding.pixelFormat, payload);
			}

			final ByteBuffer pixels;
			if (encoding.isPaletted()) {
				pixels = decodePalette(in, encoding, pixelCount);
			} else {
				pixels = readPackedPixels(in, pixelCount);
			}
			requireEndOfStream(in);
			return new Decoded(width, height, encoding.pixelFormat.glFormat, false,
					encoding.pixelFormat, pixels);
		}
	}

	static boolean supportsNativePalettedUpload() {
		return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
	}

	static void writeHeader(final DataOutputStream output, final int width,
			final int height, final Encoding encoding) throws IOException {
		validateDimensions(width, height);
		output.writeInt(MAGIC);
		output.writeShort(width);
		output.writeShort(height);
		output.writeByte(encoding.id);
	}

	private static ByteBuffer decodePalette(final DataInputStream in, final Encoding encoding,
			final int pixelCount) throws IOException {
		final int[] palette = new int[encoding.paletteEntries];
		for (int i = 0; i < palette.length; i++)
			palette[i] = readPacked16(in);

		final ByteBuffer pixels = allocatePackedPixels(pixelCount);
		final ShortBuffer packedPixels = pixels.asShortBuffer();
		final int indexBytes = encoding.indexBytes(pixelCount);
		final byte[] indexes = new byte[Math.min(COPY_BUFFER_SIZE, indexBytes)];
		int remaining = indexBytes;
		int writtenPixels = 0;
		while (remaining > 0) {
			final int count = Math.min(remaining, indexes.length);
			in.readFully(indexes, 0, count);
			for (int i = 0; i < count && writtenPixels < pixelCount; i++) {
				final int value = indexes[i] & 0xFF;
				if (encoding.paletteEntries == 16) {
					packedPixels.put((short) palette[value >>> 4]);
					writtenPixels++;
					if (writtenPixels < pixelCount) {
						packedPixels.put((short) palette[value & 0xF]);
						writtenPixels++;
					}
				} else {
					packedPixels.put((short) palette[value]);
					writtenPixels++;
				}
			}
			remaining -= count;
		}
		if (writtenPixels != pixelCount)
			throw new EOFException("Truncated preprocessed texture indexes");
		return pixels;
	}

	private static ByteBuffer readPackedPixels(final DataInputStream input,
			final int pixelCount) throws IOException {
		final ByteBuffer pixels = allocatePackedPixels(pixelCount);
		final ShortBuffer packedPixels = pixels.asShortBuffer();
		for (int i = 0; i < pixelCount; i++)
			packedPixels.put((short) readPacked16(input));
		return pixels;
	}

	private static ByteBuffer allocatePackedPixels(final int pixelCount) {
		return ByteBuffer.allocateDirect(Math.multiplyExact(pixelCount, Short.BYTES))
				.order(ByteOrder.nativeOrder());
	}

	private static int readPacked16(final DataInputStream input) throws IOException {
		final int low = input.readUnsignedByte();
		return low | input.readUnsignedByte() << 8;
	}

	private static ByteBuffer readDirect(final DataInputStream input, final int size)
			throws IOException {
		final ByteBuffer output = ByteBuffer.allocateDirect(size);
		final byte[] copyBuffer = new byte[Math.min(COPY_BUFFER_SIZE, size)];
		int remaining = size;
		while (remaining > 0) {
			final int count = Math.min(remaining, copyBuffer.length);
			input.readFully(copyBuffer, 0, count);
			output.put(copyBuffer, 0, count);
			remaining -= count;
		}
		output.flip();
		return output;
	}

	private static void requireEndOfStream(final DataInputStream input) throws IOException {
		if (input.read() >= 0)
			throw new IOException("Trailing data in preprocessed texture");
	}

	private static void validateDimensions(final int width, final int height) throws IOException {
		if (width <= 0 || height <= 0 || width > MAX_TEXTURE_DIMENSION
				|| height > MAX_TEXTURE_DIMENSION || !isPowerOfTwo(width)
				|| !isPowerOfTwo(height))
			throw new IOException("Invalid preprocessed texture dimensions: " + width + "x" + height);
		try {
			Math.multiplyExact(width, height);
		} catch (final ArithmeticException exception) {
			throw new IOException("Preprocessed texture dimensions overflow", exception);
		}
	}

	private static boolean isPowerOfTwo(final int value) {
		return (value & value - 1) == 0;
	}

	enum Encoding {
		PALETTE4_RGB565(1, 16, GL_PALETTE4_R5_G6_B5_OES, PixelFormat.RGB565),
		PALETTE8_RGB565(2, 256, GL_PALETTE8_R5_G6_B5_OES, PixelFormat.RGB565),
		PALETTE4_RGBA4(3, 16, GL_PALETTE4_RGBA4_OES, PixelFormat.RGBA4),
		PALETTE8_RGBA4(4, 256, GL_PALETTE8_RGBA4_OES, PixelFormat.RGBA4),
		PALETTE4_RGB5_A1(5, 16, GL_PALETTE4_RGB5_A1_OES, PixelFormat.RGB5_A1),
		PALETTE8_RGB5_A1(6, 256, GL_PALETTE8_RGB5_A1_OES, PixelFormat.RGB5_A1),
		RGB565(7, 0, GL.GL_RGB, PixelFormat.RGB565),
		RGBA4(8, 0, GL.GL_RGBA, PixelFormat.RGBA4),
		RGB5_A1(9, 0, GL.GL_RGBA, PixelFormat.RGB5_A1);

		final int id;
		final int paletteEntries;
		final int glInternalFormat;
		final PixelFormat pixelFormat;

		Encoding(final int id, final int paletteEntries, final int glInternalFormat,
				final PixelFormat pixelFormat) {
			this.id = id;
			this.paletteEntries = paletteEntries;
			this.glInternalFormat = glInternalFormat;
			this.pixelFormat = pixelFormat;
		}

		boolean isPaletted() {
			return paletteEntries > 0;
		}

		int indexBytes(final int pixelCount) {
			return paletteEntries == 16 ? (pixelCount + 1) / 2 : pixelCount;
		}

		int payloadBytes(final int pixelCount) {
			if (!isPaletted())
				return Math.multiplyExact(pixelCount, Short.BYTES);
			return Math.addExact(Math.multiplyExact(paletteEntries, Short.BYTES),
					indexBytes(pixelCount));
		}

		static Encoding forPixelFormat(final PixelFormat pixelFormat,
				final int paletteEntries) {
			for (final Encoding encoding : values())
				if (encoding.pixelFormat == pixelFormat
						&& encoding.paletteEntries == paletteEntries)
					return encoding;
			throw new IllegalArgumentException("Unsupported texture encoding: "
					+ pixelFormat + " with " + paletteEntries + " palette entries");
		}

		static Encoding fromId(final int id) throws IOException {
			for (final Encoding encoding : values())
				if (encoding.id == id)
					return encoding;
			throw new IOException("Unknown preprocessed texture encoding: " + id);
		}
	}

	enum PixelFormat {
		RGB565(GL.GL_RGB, GL.GL_UNSIGNED_SHORT_5_6_5) {
			@Override
			int packArgb(final int argb) {
				return quantize(argb >>> 16 & 0xFF, 5) << 11
						| quantize(argb >>> 8 & 0xFF, 6) << 5
						| quantize(argb & 0xFF, 5);
			}
		},
		RGBA4(GL.GL_RGBA, GL.GL_UNSIGNED_SHORT_4_4_4_4) {
			@Override
			int packArgb(final int argb) {
				return quantize(argb >>> 16 & 0xFF, 4) << 12
						| quantize(argb >>> 8 & 0xFF, 4) << 8
						| quantize(argb & 0xFF, 4) << 4
						| quantize(argb >>> 24, 4);
			}
		},
		RGB5_A1(GL.GL_RGBA, GL.GL_UNSIGNED_SHORT_5_5_5_1) {
			@Override
			int packArgb(final int argb) {
				return quantize(argb >>> 16 & 0xFF, 5) << 11
						| quantize(argb >>> 8 & 0xFF, 5) << 6
						| quantize(argb & 0xFF, 5) << 1
						| (argb >>> 24) / 128;
			}
		};

		final int glFormat;
		final int glType;

		PixelFormat(final int glFormat, final int glType) {
			this.glFormat = glFormat;
			this.glType = glType;
		}

		abstract int packArgb(int argb);

		private static int quantize(final int component, final int bits) {
			final int maximum = (1 << bits) - 1;
			return (component * maximum + 127) / 255;
		}
	}

	record Decoded(int width, int height, int internalFormat, boolean compressed,
			PixelFormat pixelFormat, ByteBuffer pixels) {

		Texture upload(final GL gl) {
			final Texture texture = new Texture(GL.GL_TEXTURE_2D);
			try {
				texture.bind(gl);
				texture.set(width, height, width, height);
				gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
				final ByteBuffer uploadPixels = pixels.duplicate();
				uploadPixels.rewind();
				if (compressed) {
					gl.glCompressedTexImage2D(GL.GL_TEXTURE_2D, 0, internalFormat,
							width, height, 0, uploadPixels.remaining(), uploadPixels);
				} else {
					gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, internalFormat, width, height,
							0, pixelFormat.glFormat, pixelFormat.glType, uploadPixels);
				}
				final int error = gl.glGetError();
				if (error != GL.GL_NO_ERROR)
					throw new GLException("Packed texture upload failed with GL error 0x"
							+ Integer.toHexString(error));
				return texture;
			} catch (final RuntimeException exception) {
				try {
					texture.destroy(gl);
				} catch (final RuntimeException cleanupException) {
					exception.addSuppressed(cleanupException);
				}
				throw exception;
			} finally {
				gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 4);
			}
		}
	}
}
