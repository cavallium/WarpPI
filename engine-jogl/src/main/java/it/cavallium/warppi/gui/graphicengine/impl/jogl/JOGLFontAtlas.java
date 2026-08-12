package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import it.cavallium.warppi.gui.graphicengine.impl.common.RFTFont;

/** Binary, directly uploadable OpenGL alpha atlas plus its font metadata. */
final class JOGLFontAtlas {

	private static final int MAGIC = 0x57464131; // WFA1
	private static final int LEGACY_MAX_BOUND = 66_000;
	private static final int MAX_TEXTURE_DIMENSION = 8_192;
	private static final int MAX_INTERVAL_VALUES = 3 * 65_536;

	final int charW;
	final int charH;
	final int minCharIndex;
	final int maxCharIndex;
	final int textureW;
	final int textureH;
	final int columns;
	final int[] intervals;
	final byte[] alphaPixels;

	private JOGLFontAtlas(final int charW, final int charH, final int minCharIndex, final int maxCharIndex,
			final int textureW, final int textureH, final int columns, final int[] intervals,
			final byte[] alphaPixels) throws IOException {
		this.charW = charW;
		this.charH = charH;
		this.minCharIndex = minCharIndex;
		this.maxCharIndex = maxCharIndex;
		this.textureW = textureW;
		this.textureH = textureH;
		this.columns = columns;
		this.intervals = intervals;
		this.alphaPixels = alphaPixels;
		validate();
	}

	static JOGLFontAtlas fromRft(final InputStream input) throws IOException {
		if (input == null)
			throw new IOException("Missing RFT font input");

		try (DataInputStream in = new DataInputStream(new BufferedInputStream(input))) {
			if (in.readUnsignedByte() != 'r' || in.readUnsignedByte() != 'a' || in.readUnsignedByte() != 'w'
					|| in.readUnsignedByte() != 0xFF)
				throw new IOException("Invalid RFT magic");

			final int charW = in.readUnsignedShort();
			final int charH = in.readUnsignedShort();
			if (in.readUnsignedByte() != 0xFF)
				throw new IOException("Invalid RFT width separator");
			final int minBound = in.readInt();
			if (in.readUnsignedByte() != 0xFF)
				throw new IOException("Invalid RFT bounds separator");
			final int encodedMaxBound = in.readInt();
			final int maxBound = encodedMaxBound > minBound ? encodedMaxBound : LEGACY_MAX_BOUND;
			final int pixelsPerGlyph = checkedGlyphSize(charW, charH);
			final int bytesPerGlyph = (pixelsPerGlyph + 7) / 8;

			final TreeMap<Integer, byte[]> glyphs = new TreeMap<>();
			while (true) {
				final int high = in.read();
				if (high < 0)
					break;
				final int low = in.read();
				if (low < 0)
					throw new EOFException("Truncated RFT character index");
				final int charIndex = high << 8 | low;
				if (charIndex < minBound || charIndex >= maxBound)
					throw new IOException("RFT character index outside declared bounds: " + charIndex);
				final byte[] packedGlyph = in.readNBytes(bytesPerGlyph);
				if (packedGlyph.length != bytesPerGlyph)
					throw new EOFException("Truncated RFT glyph for character " + charIndex);
				if (glyphs.put(charIndex - minBound, packedGlyph) != null)
					throw new IOException("Duplicate RFT character index: " + charIndex);
			}

			if (glyphs.isEmpty())
				throw new IOException("RFT font contains no glyphs");
			final int[] intervals = findIntervals(glyphs.keySet().stream().mapToInt(Integer::intValue).toArray());
			final AtlasLayout layout = chooseLayout(glyphs.size(), charW, charH);
			final byte[] alphaPixels = new byte[Math.multiplyExact(layout.width(), layout.height())];
			int compressedIndex = 0;
			for (final Map.Entry<Integer, byte[]> glyph : glyphs.entrySet()) {
				writePackedGlyph(alphaPixels, layout, compressedIndex, charW, charH, glyph.getValue());
				compressedIndex++;
			}
			return new JOGLFontAtlas(charW, charH, minBound, maxBound, layout.width(), layout.height(),
					layout.columns(), intervals, alphaPixels);
		}
	}

	static JOGLFontAtlas fromRftFont(final RFTFont font) throws IOException {
		if (font.rawchars == null || font.rawchars.length == 0)
			throw new IOException("RFT font contains no glyphs");
		if (font.intervals == null)
			throw new IOException("RFT font contains no character intervals");

		final AtlasLayout layout = chooseLayout(font.rawchars.length, font.charW, font.charH);
		final byte[] alphaPixels = new byte[Math.multiplyExact(layout.width(), layout.height())];
		for (int i = 0; i < font.rawchars.length; i++)
			writeUnpackedGlyph(alphaPixels, layout, i, font.charW, font.charH, font.rawchars[i]);
		return new JOGLFontAtlas(font.charW, font.charH, font.minBound, font.maxBound, layout.width(),
				layout.height(), layout.columns(), font.intervals.clone(), alphaPixels);
	}

	static JOGLFontAtlas read(final InputStream input) throws IOException {
		if (input == null)
			throw new IOException("Missing generated JOGL font atlas");

		try (DataInputStream in = new DataInputStream(new BufferedInputStream(input))) {
			if (in.readInt() != MAGIC)
				throw new IOException("Invalid JOGL font atlas magic");
			final int charW = in.readInt();
			final int charH = in.readInt();
			final int minCharIndex = in.readInt();
			final int maxCharIndex = in.readInt();
			final int textureW = in.readInt();
			final int textureH = in.readInt();
			final int columns = in.readInt();
			final int intervalValues = in.readInt();
			if (intervalValues <= 0 || intervalValues > MAX_INTERVAL_VALUES || intervalValues % 3 != 0)
				throw new IOException("Invalid JOGL font atlas interval count: " + intervalValues);
			final int[] intervals = new int[intervalValues];
			for (int i = 0; i < intervalValues; i++)
				intervals[i] = in.readInt();
			final int pixelCount = in.readInt();
			final int expectedPixelCount = checkedTextureSize(textureW, textureH);
			if (pixelCount != expectedPixelCount)
				throw new IOException("Invalid JOGL font atlas pixel count: " + pixelCount + ", expected "
						+ expectedPixelCount);
			final byte[] alphaPixels = in.readNBytes(pixelCount);
			if (alphaPixels.length != pixelCount)
				throw new EOFException("Truncated JOGL font atlas pixels");
			if (in.read() >= 0)
				throw new IOException("Trailing data in JOGL font atlas");
			return new JOGLFontAtlas(charW, charH, minCharIndex, maxCharIndex, textureW, textureH, columns,
					intervals, alphaPixels);
		}
	}

	void write(final OutputStream output) throws IOException {
		try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(output))) {
			out.writeInt(MAGIC);
			out.writeInt(charW);
			out.writeInt(charH);
			out.writeInt(minCharIndex);
			out.writeInt(maxCharIndex);
			out.writeInt(textureW);
			out.writeInt(textureH);
			out.writeInt(columns);
			out.writeInt(intervals.length);
			for (final int interval : intervals)
				out.writeInt(interval);
			out.writeInt(alphaPixels.length);
			out.write(alphaPixels);
		}
	}

	private void validate() throws IOException {
		checkedGlyphSize(charW, charH);
		final int pixelCount = checkedTextureSize(textureW, textureH);
		if (minCharIndex < 0 || maxCharIndex <= minCharIndex)
			throw new IOException("Invalid JOGL font atlas character bounds");
		if (textureW < charW || textureH < charH || !isPowerOfTwo(textureW) || !isPowerOfTwo(textureH))
			throw new IOException("Invalid JOGL font atlas texture dimensions: " + textureW + "x" + textureH);
		if (columns != textureW / charW || columns <= 0)
			throw new IOException("Invalid JOGL font atlas column count: " + columns);
		if (alphaPixels.length != pixelCount)
			throw new IOException("Invalid JOGL font atlas alpha data length");
		if (intervals.length == 0 || intervals.length > MAX_INTERVAL_VALUES || intervals.length % 3 != 0)
			throw new IOException("Invalid JOGL font atlas intervals");

		int glyphCount = 0;
		int previousEnd = -2;
		for (int i = 0; i < intervals.length; i += 3) {
			final int start = intervals[i];
			final int end = intervals[i + 1];
			final int size = intervals[i + 2];
			if (start < 0 || end < start || size != end - start + 1 || start <= previousEnd + 1
					|| end >= maxCharIndex - minCharIndex)
				throw new IOException("Invalid JOGL font atlas interval at index " + i / 3);
			glyphCount = Math.addExact(glyphCount, size);
			previousEnd = end;
		}
		final long availableCells = (long) columns * (textureH / charH);
		if (glyphCount > availableCells)
			throw new IOException("JOGL font atlas texture is too small for its glyphs");
	}

	private static int checkedGlyphSize(final int charW, final int charH) throws IOException {
		if (charW <= 0 || charH <= 0 || charW > MAX_TEXTURE_DIMENSION || charH > MAX_TEXTURE_DIMENSION)
			throw new IOException("Invalid RFT glyph dimensions: " + charW + "x" + charH);
		try {
			return Math.multiplyExact(charW, charH);
		} catch (final ArithmeticException ex) {
			throw new IOException("RFT glyph dimensions overflow", ex);
		}
	}

	private static int checkedTextureSize(final int width, final int height) throws IOException {
		if (width <= 0 || height <= 0 || width > MAX_TEXTURE_DIMENSION || height > MAX_TEXTURE_DIMENSION)
			throw new IOException("Invalid JOGL font atlas texture dimensions: " + width + "x" + height);
		try {
			return Math.multiplyExact(width, height);
		} catch (final ArithmeticException ex) {
			throw new IOException("JOGL font atlas dimensions overflow", ex);
		}
	}

	private static int[] findIntervals(final int[] relativeIndexes) {
		final List<Integer> values = new ArrayList<>();
		int start = relativeIndexes[0];
		int previous = start;
		for (int i = 1; i < relativeIndexes.length; i++) {
			final int current = relativeIndexes[i];
			if (current != previous + 1) {
				appendInterval(values, start, previous);
				start = current;
			}
			previous = current;
		}
		appendInterval(values, start, previous);
		return values.stream().mapToInt(Integer::intValue).toArray();
	}

	private static void appendInterval(final List<Integer> values, final int start, final int end) {
		values.add(start);
		values.add(end);
		values.add(end - start + 1);
	}

	private static AtlasLayout chooseLayout(final int glyphCount, final int charW, final int charH) throws IOException {
		if (glyphCount <= 0)
			throw new IOException("Cannot create an empty font atlas");
		checkedGlyphSize(charW, charH);
		long bestArea = Long.MAX_VALUE;
		long bestSkew = Long.MAX_VALUE;
		AtlasLayout best = null;
		for (int width = nextPowerOfTwo(charW); width > 0 && width <= MAX_TEXTURE_DIMENSION; width <<= 1) {
			final int columns = width / charW;
			final int rows = (glyphCount + columns - 1) / columns;
			final long minimumHeight = (long) rows * charH;
			if (minimumHeight > MAX_TEXTURE_DIMENSION)
				continue;
			final int height = nextPowerOfTwo((int) minimumHeight);
			final long area = (long) width * height;
			final long skew = Math.abs((long) width - height);
			if (area < bestArea || area == bestArea && (skew < bestSkew
					|| skew == bestSkew && (best == null || width < best.width()))) {
				bestArea = area;
				bestSkew = skew;
				best = new AtlasLayout(width, height, columns);
			}
			if (width == MAX_TEXTURE_DIMENSION)
				break;
		}
		if (best == null)
			throw new IOException("Font does not fit in a " + MAX_TEXTURE_DIMENSION + "x" + MAX_TEXTURE_DIMENSION
					+ " texture");
		return best;
	}

	private static void writePackedGlyph(final byte[] target, final AtlasLayout layout, final int glyphIndex,
			final int charW, final int charH, final byte[] packedGlyph) {
		final int cellX = glyphIndex % layout.columns() * charW;
		final int cellY = glyphIndex / layout.columns() * charH;
		for (int y = 0; y < charH; y++) {
			final int targetY = layout.height() - 1 - cellY - y;
			for (int x = 0; x < charW; x++) {
				final int bitIndex = y * charW + x;
				if ((packedGlyph[bitIndex >>> 3] & 0x80 >>> (bitIndex & 7)) != 0)
					target[targetY * layout.width() + cellX + x] = (byte) 0xFF;
			}
		}
	}

	private static void writeUnpackedGlyph(final byte[] target, final AtlasLayout layout, final int glyphIndex,
			final int charW, final int charH, final boolean[] glyph) throws IOException {
		if (glyph == null || glyph.length != charW * charH)
			throw new IOException("Invalid unpacked RFT glyph at index " + glyphIndex);
		final int cellX = glyphIndex % layout.columns() * charW;
		final int cellY = glyphIndex / layout.columns() * charH;
		for (int y = 0; y < charH; y++) {
			final int targetY = layout.height() - 1 - cellY - y;
			for (int x = 0; x < charW; x++)
				if (glyph[y * charW + x])
					target[targetY * layout.width() + cellX + x] = (byte) 0xFF;
		}
	}

	private static int nextPowerOfTwo(final int value) throws IOException {
		if (value <= 0 || value > MAX_TEXTURE_DIMENSION)
			throw new IOException("Invalid texture extent: " + value);
		int result = 1;
		while (result < value)
			result <<= 1;
		return result;
	}

	private static boolean isPowerOfTwo(final int value) {
		return (value & value - 1) == 0;
	}

	@Override
	public boolean equals(final Object object) {
		if (this == object)
			return true;
		if (!(object instanceof JOGLFontAtlas other))
			return false;
		return charW == other.charW && charH == other.charH && minCharIndex == other.minCharIndex
				&& maxCharIndex == other.maxCharIndex && textureW == other.textureW && textureH == other.textureH
				&& columns == other.columns && Arrays.equals(intervals, other.intervals)
				&& Arrays.equals(alphaPixels, other.alphaPixels);
	}

	@Override
	public int hashCode() {
		int result = charW;
		result = 31 * result + charH;
		result = 31 * result + minCharIndex;
		result = 31 * result + maxCharIndex;
		result = 31 * result + textureW;
		result = 31 * result + textureH;
		result = 31 * result + columns;
		result = 31 * result + Arrays.hashCode(intervals);
		return 31 * result + Arrays.hashCode(alphaPixels);
	}

	private record AtlasLayout(int width, int height, int columns) {}
}
