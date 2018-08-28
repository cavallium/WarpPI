package org.warp.picalculator.device.graphicengine;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.warp.picalculator.ClassUtils;
import org.warp.picalculator.PICalculator;
import org.warp.picalculator.Utils;
import org.warp.picalculator.deps.DSystem;

/**
 *
 * @author andreacv
 */
public class RAWFont {

	public boolean[][] rawchars;
	public int[] chars32;
	public int minBound = 10;
	public int maxBound = 0;
	public int charW;
	public int charH;
	public int charS;
	public int charIntCount;
	public static final int intBits = 31;

	public void create(String name) {
		try {
			loadFont("/font_" + name + ".rft");
		} catch (final IOException e) {
			e.printStackTrace();
			DSystem.exit(1);
		}
		chars32 = new int[(maxBound - minBound) * charIntCount];
		for (int charIndex = 0; charIndex < maxBound - minBound; charIndex++) {
			final boolean[] currentChar = rawchars[charIndex];
			if (currentChar == null) {
				int currentInt = 0;
				int currentBit = 0;
				for (int i = 0; i < charS; i++) {
					if (currentInt * intBits + currentBit >= (currentInt + 1) * intBits) {
						currentInt += 1;
						currentBit = 0;
					}
					chars32[charIndex * charIntCount + currentInt] = (chars32[charIndex * charIntCount + currentInt] << 1) + 1;
					currentBit += 1;
				}
			} else {
				int currentInt = 0;
				int currentBit = 0;
				for (int i = 0; i < charS; i++) {
					if (currentBit >= intBits) {
						currentInt += 1;
						currentBit = 0;
					}
					chars32[charIndex * charIntCount + currentInt] = (chars32[charIndex * charIntCount + currentInt]) | ((currentChar[i] ? 1 : 0) << currentBit);
					currentBit++;
				}
			}
		}

		Object obj = new Object();
		final WeakReference<Object> ref = new WeakReference<>(obj);
		obj = null;
		while (ref.get() != null) {
			System.gc();
		}
	}

	private void loadFont(String string) throws IOException {
		final URL res = ClassUtils.classLoader.getClass().getResource(string);
		final int[] file = Utils.realBytes(Utils.convertStreamToByteArray(res.openStream(), res.getFile().length()));
		final int filelength = file.length;
		if (filelength >= 16) {
			if (file[0x0] == 114 && file[0x1] == 97 && file[0x2] == 119 && file[0x3] == 0xFF && file[0x8] == 0xFF && file[0xD] == 0xFF) {
				charW = file[0x4] << 8 | file[0x5];
				charH = file[0x6] << 8 | file[0x7];
				charS = charW * charH;
				charIntCount = (int) Math.ceil(((double) charS) / ((double) intBits));
				minBound = file[0x9] << 24 | file[0xA] << 16 | file[0xB] << 8 | file[0xC];
				maxBound = file[0xE] << 24 | file[0xF] << 16 | file[0x10] << 8 | file[0x11];
				if (maxBound <= minBound) {
					maxBound = 10000; //TODO remove it: temp fix
				}
				rawchars = new boolean[maxBound - minBound][];
				int index = 0x12;
				while (index < filelength) {
					try {
						final int charIndex = file[index] << 8 | file[index + 1];
						final boolean[] rawchar = new boolean[charS];
						int charbytescount = 0;
						while (charbytescount * 8 < charS) {
							charbytescount += 1;
						}
						int currentBit = 0;
						for (int i = 0; i <= charbytescount; i++) {
							for (int bit = 0; bit < 8; bit++) {
								if (currentBit >= charS) {
									break;
								}
								rawchar[currentBit] = (((file[index + 2 + i] >> (8 - 1 - bit)) & 0x1) == 1) ? true : false;
								currentBit++;
							}
						}
						rawchars[charIndex - minBound] = rawchar;
						index += 2 + charbytescount;
					} catch (final Exception ex) {
						ex.printStackTrace();
						System.out.println(string);
						DSystem.exit(-1);
					}
				}
			} else {
				throw new IOException();
			}
		} else {
			throw new IOException();
		}
	}

	public int[] getCharIndexes(String txt) {
		final int l = txt.length();
		final int[] indexes = new int[l];
		final char[] chars = txt.toCharArray();
		for (int i = 0; i < l; i++) {
			indexes[i] = (chars[i] & 0xFFFF) - minBound;
		}
		return indexes;
	}

	@SuppressWarnings("unused")
	private void saveArray(int[] screen, String coutputpng) {
		final BufferedImage bi = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
		final int[] a = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
		System.arraycopy(screen, 0, a, 0, screen.length);
		try {
			ImageIO.write(bi, "PNG", new File(coutputpng));
		} catch (final IOException ex) {
			Logger.getLogger(RAWFont.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void drawText(int[] screen, int[] screenSize, int x, int y, int[] text, int color) {
		final int screenLength = screen.length;
		int screenPos = 0;

		int currentInt;
		int currentIntBitPosition;
		int bitData;
		int cpos;
		int j;
		final int l = text.length;
		for (int i = 0; i < l; i++) {
			cpos = (i * (charW + 1));
			final int charIndex = text[i];
			for (int dy = 0; dy < charH; dy++) {
				for (int dx = 0; dx < charW; dx++) {
					j = x + cpos + dx;
					if (j > 0 & j < screenSize[0]) {
						final int bit = dx + dy * charW;
						currentInt = (int) (Math.floor(bit) / (intBits));
						currentIntBitPosition = bit - (currentInt * intBits);
						bitData = (chars32[charIndex * charIntCount + currentInt] >> currentIntBitPosition) & 1;
						screenPos = x + cpos + dx + (y + dy) * screenSize[0];
						if (bitData == 1 & screenLength > screenPos) {
							screen[screenPos] = color;
						}
					}
				}
			}
		}
	}
}
