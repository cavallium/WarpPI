package it.cavallium.warppi.gui.graphicengine.common;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Utils;
import it.cavallium.warppi.deps.Platform.ConsoleUtils;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.gui.graphicengine.GraphicEngine;

public abstract class RFTFont implements BinaryFont {

	public boolean[][] rawchars;
	public int[] chars32;
	public int minBound = 10;
	public int maxBound = 0;
	public int charW;
	public int charH;
	public int charS;
	public int charIntCount;
	public int[] intervals;
	public int intervalsTotalSize = 0;
	public static final int intBits = 31;
	@SuppressWarnings("unused")
	private final boolean isResource;

	public RFTFont(String fontName) throws IOException {
		this(fontName, false);
	}

	RFTFont(String fontName, boolean onlyRaw) throws IOException {
		isResource = true;
		load("/font_" + fontName + ".rft", onlyRaw);
	}

	public RFTFont(String path, String fontName) throws IOException {
		this(path, fontName, false);
	}

	RFTFont(String path, String fontName, boolean onlyRaw) throws IOException {
		isResource = false;
		load(path + "/font_" + fontName + ".rft", onlyRaw);
	}

	public static RFTFont loadTemporaryFont(String name) throws IOException {
		return new BlankRFTFont(name, true);
	}

	public static RFTFont loadTemporaryFont(String path, String name) throws IOException {
		return new BlankRFTFont(path, name, true);
	}

	@Override
	public void load(String path) throws IOException {
		load(path, false);
	}

	private void load(String path, boolean onlyRaw) throws IOException {
		Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_MIN, "Loading font " + path);
		loadFont(path);
		if (!onlyRaw) {
			chars32 = new int[(intervalsTotalSize) * charIntCount];
			for (int charCompressedIndex = 0; charCompressedIndex < intervalsTotalSize; charCompressedIndex++) {
				final boolean[] currentChar = rawchars[charCompressedIndex];
				if (currentChar == null) {
					int currentInt = 0;
					int currentBit = 0;
					for (int i = 0; i < charS; i++) {
						if (currentInt * intBits + currentBit >= (currentInt + 1) * intBits) {
							currentInt += 1;
							currentBit = 0;
						}
						chars32[charCompressedIndex * charIntCount + currentInt] = (chars32[charCompressedIndex * charIntCount + currentInt] << 1) + 1;
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
						chars32[charCompressedIndex * charIntCount + currentInt] = (chars32[charCompressedIndex * charIntCount + currentInt]) | ((currentChar[i] ? 1 : 0) << currentBit);
						currentBit++;
					}
				}
			}
		}

		Engine.getPlatform().gc();
	}

	private void loadFont(String string) throws IOException {
		InputStream res;
		try {
			if (!string.startsWith("/"))
				string = "/" + string;
			res = Engine.getPlatform().getStorageUtils().getResourceStream(string);
		} catch (URISyntaxException e) {
			IOException ex = new IOException();
			ex.initCause(e);
			throw ex;
		}
		final int[] file = Utils.realBytes(Utils.convertStreamToByteArray(res, res.available()));
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
					maxBound = 66000; //TODO remove it: temp fix
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
						Engine.getPlatform().exit(-1);
					}
				}
			} else {
				throw new IOException();
			}
		} else {
			throw new IOException();
		}
		findIntervals();
		/*int[] screen = new int[rawchars.length * charW * charH];
		for (int i = 0; i < rawchars.length; i++) {
			if (rawchars[i] != null)
				for (int charX = 0; charX < charW; charX++) {
					for (int charY = 0; charY < charH; charY++) {
						int x = i * charW + charX;
						int y = charY;
						screen[x + y * rawchars.length * charW] = rawchars[i][charX + charY * charW] ? 0xFF000000 : 0xFFFFFFFF;
					}
				}
		}
		System.out.println();
		System.out.println((('1' & 0xFFFF) - minBound) + "=>"+ (getCharIndexes("1")[0]));
		this.saveArray(screen, rawchars.length * charW, charH, "N:\\TimedTemp"+string+".png");
		System.out.println();
		System.out.println();
		*/
	}

	private void findIntervals() {
		final LinkedList<int[]> intervals = new LinkedList<>();
		int beginIndex = -1;
		int endIndex = 0;
		int intervalSize = 0;
		@SuppressWarnings("unused")
		final int holeSize = 0;
		for (int i = 0; i < rawchars.length; i++) {
			if (rawchars[i] != null) {
				beginIndex = i;
				int firstNull = 0;
				while (i + firstNull < rawchars.length && rawchars[i + firstNull] != null) {
					firstNull++;
				}
				endIndex = beginIndex + firstNull - 1;
				i = endIndex;
				if (endIndex >= 0) {
					intervalSize = endIndex - beginIndex + 1;
					intervals.add(new int[] { beginIndex, endIndex, intervalSize });
					intervalsTotalSize += intervalSize;
				}
				beginIndex = -1;
			}
		}
		int lastIndex = 0;
		final boolean[][] newrawchars = new boolean[intervalsTotalSize][];
		for (final int[] interval : intervals) {
			if (rawchars.length - (interval[0]) - interval[2] < 0) {
				System.err.println(interval[0] + "-" + interval[1] + "(" + interval[2] + ")");
				System.err.println(rawchars.length - (interval[0]) - interval[2]);
				throw new ArrayIndexOutOfBoundsException();
			}
			if (newrawchars.length - (lastIndex - 1) - interval[2] < 0) {
				System.err.println(newrawchars.length - (lastIndex - 1) - interval[2]);
				throw new ArrayIndexOutOfBoundsException();
			}
			System.arraycopy(rawchars, interval[0], newrawchars, lastIndex, interval[2]);
			lastIndex += interval[2];
		}
		rawchars = newrawchars;
		final int intervalsSize = intervals.size();
		this.intervals = new int[intervalsSize * 3];
		for (int i = 0; i < intervalsSize; i++) {
			final int[] interval = intervals.get(i);
			this.intervals[i * 3 + 0] = interval[0];
			this.intervals[i * 3 + 1] = interval[1];
			this.intervals[i * 3 + 2] = interval[2];
		}
	}

	@SuppressWarnings("unused")
	private void saveArray(int[] screen, int w, int h, String coutputpng) {
		final BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		final int[] a = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
		System.arraycopy(screen, 0, a, 0, screen.length);
		try {
			ImageIO.write(bi, "PNG", new File(coutputpng));
		} catch (final IOException ex) {
			Logger.getLogger(BinaryFont.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public int[] getCharIndexes(String txt) {
		final int l = txt.length();
		final int[] indexes = new int[l];
		final char[] chars = txt.toCharArray();
		for (int i = 0; i < l; i++) {
			final int originalIndex = (chars[i] & 0xFFFF) - minBound;
			indexes[i] = compressIndex(originalIndex);
		}
		return indexes;
	}

	public int getCharIndex(char c) {
		final int originalIndex = c & 0xFFFF;
		return compressIndex(originalIndex);
	}

	protected int compressIndex(int originalIndex) {
		int compressedIndex = 0;
		for (int i = 0; i < intervals.length; i += 3) {
			if (intervals[i] > originalIndex) {
				break;
			} else if (originalIndex <= intervals[i + 1]) {
				compressedIndex += (originalIndex - intervals[i]);
				break;
			} else {
				compressedIndex += intervals[i + 2];
			}
		}
		return compressedIndex;
	}

	@SuppressWarnings("unused")
	private int decompressIndex(int compressedIndex) {
		final int originalIndex = 0;
		int i = 0;
		for (int intvl = 0; intvl < intervals.length; intvl += 3) {
			i += intervals[intvl + 2];
			if (i == compressedIndex) {
				return intervals[intvl + 1];
			} else if (i > compressedIndex) {
				return intervals[intvl + 1] - (i - compressedIndex);
			}
		}
		return originalIndex;
	}

	@Override
	public void initialize(GraphicEngine d) {}

	@Override
	public int getStringWidth(String text) {
		final int w = charW * text.length();
		if (text.length() > 0 && w > 0) {
			return w;
		} else {
			return 0;
		}
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
		return true;
	}

	@Override
	public int getSkinWidth() {
		return -1;
	}

	@Override
	public int getSkinHeight() {
		return -1;
	}
	
	private static class BlankRFTFont extends RFTFont {

		BlankRFTFont(String fontName, boolean onlyRaw) throws IOException {
			super(fontName, onlyRaw);
		}

		public BlankRFTFont(String path, String name, boolean b) throws IOException {
			super(path, name, b);
		}

		@Override
		public void use(GraphicEngine d) {
			
		}
		
	}

}
