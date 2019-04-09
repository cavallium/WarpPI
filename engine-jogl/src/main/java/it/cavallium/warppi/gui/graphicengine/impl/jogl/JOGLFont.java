package it.cavallium.warppi.gui.graphicengine.impl.jogl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.Texture;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineHelper;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngWriter;
import it.cavallium.warppi.WarpPI;
import it.cavallium.warppi.device.display.DisplayOutputDevice;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.impl.common.RFTFont;

public class JOGLFont implements BinaryFont {

	public Texture texture;
	public int textureW;
	public int textureH;
	public int charW;
	public int charH;
	public int minCharIndex;
	public int maxCharIndex;
	public int[] intervals;
	public int intervalsTotalSize = 0;
	public int memoryWidth;
	public int memoryHeight;
	public int memoryWidthOfEachColumn;

	private boolean initialized = false;
	private File tmpFont;

	JOGLFont(final JOGLDisplayOutputDevice g, final String name) throws IOException {
		this(g, null, name);
	}

	public JOGLFont(final JOGLDisplayOutputDevice g, final String path, final String name) throws IOException {
		load(path, name);
		g.getGraphicEngine().registerFont(this);
	}

	@Override
	public void load(final String name) throws IOException {
		load(null, name);
	}

	public void load(final String path, final String name) throws IOException {
		RFTFont font;
		if (path == null)
			font = RFTFont.loadTemporaryFont(name);
		else
			font = RFTFont.loadTemporaryFont(path, name);
		charW = font.charW;
		charH = font.charH;
		minCharIndex = font.minBound;
		maxCharIndex = font.maxBound;
		intervals = font.intervals;
		intervalsTotalSize = font.intervalsTotalSize;
		boolean[][] rawchars = font.rawchars;
		font = null;
		WarpPI.getPlatform().gc();
		pregenTexture(rawchars);
		rawchars = null;
		WarpPI.getPlatform().gc();
	}

	public int[] getCharIndexes(final String txt) {
		final int[] indexes = new int[txt.length()];
		int i = 0;
		for (final char c : txt.toCharArray()) {
			indexes[i] = compressIndex((c & 0xFFFF) - minCharIndex);
			i++;
		}
		return indexes;
	}

	public int getCharIndex(final char c) {
		final int originalIndex = c & 0xFFFF;
		return compressIndex(originalIndex);
	}

	private int compressIndex(final int originalIndex) {
		int compressedIndex = 0;
		for (int i = 0; i < intervals.length; i += 3)
			if (intervals[i] > originalIndex)
				break;
			else if (originalIndex <= intervals[i + 1]) {
				compressedIndex += originalIndex - intervals[i];
				break;
			} else
				compressedIndex += intervals[i + 2];
		return compressedIndex;
	}

	@SuppressWarnings("unused")
	private int decompressIndex(final int compressedIndex) {
		final int originalIndex = 0;
		int i = 0;
		for (final int intvl = 0; i < intervals.length; i += 3) {
			i += intervals[intvl + 2];
			if (i >= compressedIndex)
				return intervals[intvl + 1] - (i - compressedIndex);
		}
		return originalIndex;
	}

	private void pregenTexture(boolean[][] chars) throws IOException {
		final int totalChars = intervalsTotalSize;
		int w = powerOf2((int) Math.ceil(Math.sqrt(totalChars) * charW));
		int h = powerOf2((int) Math.ceil(Math.sqrt(totalChars) * charH));
		int maxIndexW = (int) Math.floor((double) w / (double) charW) - 1;
		int maxIndexH = (int) Math.floor((double) h / (double) charH) - 1;
		if (w > h) {
			System.out.println("w > h");
			h = powerOf2((int) Math.ceil((double) totalChars / (double) maxIndexW * charH));
			maxIndexH = (int) Math.floor((double) h / (double) charH) - 1;
		} else {
			System.out.println("w <= h");
			w = powerOf2((int) Math.ceil((double) totalChars / (double) maxIndexH * charW));
			maxIndexW = (int) Math.floor((double) w / (double) charW) - 1;
		}
//		final int h = powerOf2((int) (Math.ceil(Math.sqrt(totalChars) * charH)));

		System.out.println((int) Math.ceil(Math.sqrt(totalChars) * charW) + " * " + (int) Math.ceil(Math.sqrt(totalChars) * charH) + " --> " + w + " * " + h);

		final File f = Files.createTempFile("texture-font-", ".png").toFile();
		f.deleteOnExit();
		final FileOutputStream outputStream = new FileOutputStream(f);
		final ImageInfo imi = new ImageInfo(w, h, 8, true); // 8 bits per channel, alpha
		// open image for writing to a output stream
		final PngWriter png = new PngWriter(outputStream, imi);
		for (int y = 0; y < png.imgInfo.rows; y++) {
			final ImageLineInt iline = new ImageLineInt(imi);
			final int[] xValues = new int[imi.cols];
			for (int indexX = 0; indexX <= maxIndexW; indexX++) {// this line will be written to all rows
				final int charY = y % charH;
				final int indexY = (y - charY) / charH;
				final int i = indexY * (maxIndexW + 1) + indexX - minCharIndex;
				boolean[] currentChar;
				if (i < totalChars && (currentChar = chars[i]) != null)
					for (int charX = 0; charX < charW; charX++)
						if (i >= 0 & i < totalChars && currentChar != null && currentChar[charX + charY * charW])
							xValues[indexX * charW + charX] = 0xFFFFFFFF;
			}
			ImageLineHelper.setPixelsRGBA8(iline, xValues);
			if (y % 10 == 0)
				System.out.println(y + "/" + png.imgInfo.rows);
			png.writeRow(iline);
		}
		chars = null;
		png.end();
		WarpPI.getPlatform().gc();

		try {
			memoryWidth = w;
			memoryHeight = h;
			memoryWidthOfEachColumn = maxIndexW + 1;
			textureW = w;
			textureH = h;
			outputStream.flush();
			outputStream.close();
			WarpPI.getPlatform().gc();
			tmpFont = f;
		} catch (GLException | IOException e) {
			e.printStackTrace();
		}
	}

	private void genTexture() {
		try {
			texture = JOGLRenderer.importTexture(tmpFont, true);
			tmpFont = null;
		} catch (GLException | IOException e) {
			e.printStackTrace();
		}
	}

	private int powerOf2(final int i) {
		return i > 1 ? Integer.highestOneBit(i - 1) << 1 : 1;
	}

	@Override
	public void initialize(final DisplayOutputDevice d) {
		genTexture();
		tmpFont = null;
		initialized = true;
	}

	@Override
	public void use(final DisplayOutputDevice d) {
		if (!initialized)
			initialize(d);
		final JOGLRenderer r = (JOGLRenderer) d.getGraphicEngine().getRenderer();
		r.currentFont = this;
		r.useTexture(texture, textureW, textureH);
	}

	@Override
	public int getStringWidth(final String text) {
		final int w = charW * text.length();
		if (text.length() > 0)
			return w;
		else
			return 0;
	}

	@Override
	public int getCharacterWidth() {
		return charW;
	}

	@Override
	public int getCharacterHeight() {
		return charH;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public int getSkinWidth() {
		return memoryWidth;
	}

	@Override
	public int getSkinHeight() {
		return memoryHeight;
	}
}