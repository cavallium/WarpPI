package ar.com.hjg.pngj;

import java.util.Arrays;

import ar.com.hjg.pngj.chunks.PngChunkPLTE;
import ar.com.hjg.pngj.chunks.PngChunkTRNS;

/**
 * Bunch of utility static methods to proces an image line at the pixel level.
 * <p>
 * WARNING: this has little testing/optimizing, and this API is not stable. some
 * methods will probably be changed or
 * removed if future releases.
 * <p>
 * WARNING: most methods for getting/setting values work currently only for
 * ImageLine or ImageLineByte
 */
public class ImageLineHelper {

	static int[] DEPTH_UNPACK_1;
	static int[] DEPTH_UNPACK_2;
	static int[] DEPTH_UNPACK_4;
	static int[][] DEPTH_UNPACK;

	static {
		ImageLineHelper.initDepthScale();
	}

	private static void initDepthScale() {
		ImageLineHelper.DEPTH_UNPACK_1 = new int[2];
		for (int i = 0; i < 2; i++)
			ImageLineHelper.DEPTH_UNPACK_1[i] = i * 255;
		ImageLineHelper.DEPTH_UNPACK_2 = new int[4];
		for (int i = 0; i < 4; i++)
			ImageLineHelper.DEPTH_UNPACK_2[i] = i * 255 / 3;
		ImageLineHelper.DEPTH_UNPACK_4 = new int[16];
		for (int i = 0; i < 16; i++)
			ImageLineHelper.DEPTH_UNPACK_4[i] = i * 255 / 15;
		ImageLineHelper.DEPTH_UNPACK = new int[][] { null, ImageLineHelper.DEPTH_UNPACK_1, ImageLineHelper.DEPTH_UNPACK_2, null, ImageLineHelper.DEPTH_UNPACK_4 };
	}

	/**
	 * When the bitdepth is less than 8, the imageLine is usually
	 * returned/expected unscaled. This method upscales it in
	 * place. Eg, if bitdepth=1, values 0-1 will be converted to 0-255
	 */
	public static void scaleUp(final IImageLineArray line) {
		if (line.getImageInfo().indexed || line.getImageInfo().bitDepth >= 8)
			return;
		final int[] scaleArray = ImageLineHelper.DEPTH_UNPACK[line.getImageInfo().bitDepth];
		if (line instanceof ImageLineInt) {
			final ImageLineInt iline = (ImageLineInt) line;
			for (int i = 0; i < iline.getSize(); i++)
				iline.scanline[i] = scaleArray[iline.scanline[i]];
		} else if (line instanceof ImageLineByte) {
			final ImageLineByte iline = (ImageLineByte) line;
			for (int i = 0; i < iline.getSize(); i++)
				iline.scanline[i] = (byte) scaleArray[iline.scanline[i]];
		} else
			throw new PngjException("not implemented");
	}

	/**
	 * Reverse of {@link #scaleUp(IImageLineArray)}
	 */
	public static void scaleDown(final IImageLineArray line) {
		if (line.getImageInfo().indexed || line.getImageInfo().bitDepth >= 8)
			return;
		if (line instanceof ImageLineInt) {
			final int scalefactor = 8 - line.getImageInfo().bitDepth;
			if (line instanceof ImageLineInt) {
				final ImageLineInt iline = (ImageLineInt) line;
				for (int i = 0; i < line.getSize(); i++)
					iline.scanline[i] = iline.scanline[i] >> scalefactor;
			} else if (line instanceof ImageLineByte) {
				final ImageLineByte iline = (ImageLineByte) line;
				for (int i = 0; i < line.getSize(); i++)
					iline.scanline[i] = (byte) ((iline.scanline[i] & 0xFF) >> scalefactor);
			}
		} else
			throw new PngjException("not implemented");
	}

	public static byte scaleUp(final int bitdepth, final byte v) {
		return bitdepth < 8 ? (byte) ImageLineHelper.DEPTH_UNPACK[bitdepth][v] : v;
	}

	public static byte scaleDown(final int bitdepth, final byte v) {
		return bitdepth < 8 ? (byte) (v >> 8 - bitdepth) : v;
	}

	/**
	 * Given an indexed line with a palette, unpacks as a RGB array, or RGBA if
	 * a non nul PngChunkTRNS chunk is passed
	 * 
	 * @param line
	 *            ImageLine as returned from PngReader
	 * @param pal
	 *            Palette chunk
	 * @param trns
	 *            Transparency chunk, can be null (absent)
	 * @param buf
	 *            Preallocated array, optional
	 * @return R G B (A), one sample 0-255 per array element. Ready for
	 *         pngw.writeRowInt()
	 */
	public static int[] palette2rgb(final ImageLineInt line, final PngChunkPLTE pal, final PngChunkTRNS trns,
			final int[] buf) {
		return ImageLineHelper.palette2rgb(line, pal, trns, buf, false);
	}

	/**
	 * Warning: the line should be upscaled, see
	 * {@link #scaleUp(IImageLineArray)}
	 */
	static int[] lineToARGB32(final ImageLineByte line, final PngChunkPLTE pal, final PngChunkTRNS trns, int[] buf) {
		final boolean alphachannel = line.imgInfo.alpha;
		final int cols = line.getImageInfo().cols;
		if (buf == null || buf.length < cols)
			buf = new int[cols];
		int index, rgb, alpha, ga, g;
		if (line.getImageInfo().indexed) {// palette
			final int nindexesWithAlpha = trns != null ? trns.getPalletteAlpha().length : 0;
			for (int c = 0; c < cols; c++) {
				index = line.scanline[c] & 0xFF;
				rgb = pal.getEntry(index);
				alpha = index < nindexesWithAlpha ? trns.getPalletteAlpha()[index] : 255;
				buf[c] = alpha << 24 | rgb;
			}
		} else if (line.imgInfo.greyscale) { // gray
			ga = trns != null ? trns.getGray() : -1;
			for (int c = 0, c2 = 0; c < cols; c++) {
				g = line.scanline[c2++] & 0xFF;
				alpha = alphachannel ? line.scanline[c2++] & 0xFF : g != ga ? 255 : 0;
				buf[c] = alpha << 24 | g | g << 8 | g << 16;
			}
		} else { // true color
			ga = trns != null ? trns.getRGB888() : -1;
			for (int c = 0, c2 = 0; c < cols; c++) {
				rgb = (line.scanline[c2++] & 0xFF) << 16 | (line.scanline[c2++] & 0xFF) << 8 | line.scanline[c2++] & 0xFF;
				alpha = alphachannel ? line.scanline[c2++] & 0xFF : rgb != ga ? 255 : 0;
				buf[c] = alpha << 24 | rgb;
			}
		}
		return buf;
	}

	/**
	 * Warning: the line should be upscaled, see
	 * {@link #scaleUp(IImageLineArray)}
	 */
	static byte[] lineToRGBA8888(final ImageLineByte line, final PngChunkPLTE pal, final PngChunkTRNS trns,
			byte[] buf) {
		final boolean alphachannel = line.imgInfo.alpha;
		final int cols = line.imgInfo.cols;
		final int bytes = cols * 4;
		if (buf == null || buf.length < bytes)
			buf = new byte[bytes];
		int index, rgb, ga;
		byte val;
		if (line.imgInfo.indexed) {// palette
			final int nindexesWithAlpha = trns != null ? trns.getPalletteAlpha().length : 0;
			for (int c = 0, b = 0; c < cols; c++) {
				index = line.scanline[c] & 0xFF;
				rgb = pal.getEntry(index);
				buf[b++] = (byte) (rgb >> 16 & 0xFF);
				buf[b++] = (byte) (rgb >> 8 & 0xFF);
				buf[b++] = (byte) (rgb & 0xFF);
				buf[b++] = (byte) (index < nindexesWithAlpha ? trns.getPalletteAlpha()[index] : 255);
			}
		} else if (line.imgInfo.greyscale) { //
			ga = trns != null ? trns.getGray() : -1;
			for (int c = 0, b = 0; b < bytes;) {
				val = line.scanline[c++];
				buf[b++] = val;
				buf[b++] = val;
				buf[b++] = val;
				buf[b++] = alphachannel ? line.scanline[c++] : (val & 0xFF) == ga ? (byte) 0 : (byte) 255;
			}
		} else if (alphachannel) // same format!
			System.arraycopy(line.scanline, 0, buf, 0, bytes);
		else
			for (int c = 0, b = 0; b < bytes;) {
				buf[b++] = line.scanline[c++];
				buf[b++] = line.scanline[c++];
				buf[b++] = line.scanline[c++];
				buf[b++] = (byte) 255; // tentative (probable)
				if (trns != null && buf[b - 3] == (byte) trns.getRGB()[0] && buf[b - 2] == (byte) trns.getRGB()[1] && buf[b - 1] == (byte) trns.getRGB()[2]) // not
																																								// very
																																								// efficient,
																																								// but
																																								// not
																																								// frecuent
					buf[b - 1] = 0;
			}
		return buf;
	}

	static byte[] lineToRGB888(final ImageLineByte line, final PngChunkPLTE pal, byte[] buf) {
		final boolean alphachannel = line.imgInfo.alpha;
		final int cols = line.imgInfo.cols;
		final int bytes = cols * 3;
		if (buf == null || buf.length < bytes)
			buf = new byte[bytes];
		byte val;
		final int[] rgb = new int[3];
		if (line.imgInfo.indexed)
			for (int c = 0, b = 0; c < cols; c++) {
				pal.getEntryRgb(line.scanline[c] & 0xFF, rgb);
				buf[b++] = (byte) rgb[0];
				buf[b++] = (byte) rgb[1];
				buf[b++] = (byte) rgb[2];
			}
		else if (line.imgInfo.greyscale)
			for (int c = 0, b = 0; b < bytes;) {
				val = line.scanline[c++];
				buf[b++] = val;
				buf[b++] = val;
				buf[b++] = val;
				if (alphachannel)
					c++; // skip alpha
			}
		else if (!alphachannel) // same format!
			System.arraycopy(line.scanline, 0, buf, 0, bytes);
		else
			for (int c = 0, b = 0; b < bytes;) {
				buf[b++] = line.scanline[c++];
				buf[b++] = line.scanline[c++];
				buf[b++] = line.scanline[c++];
				c++;// skip alpha
			}
		return buf;
	}

	/**
	 * Same as palette2rgbx , but returns rgba always, even if trns is null
	 * 
	 * @param line
	 *            ImageLine as returned from PngReader
	 * @param pal
	 *            Palette chunk
	 * @param trns
	 *            Transparency chunk, can be null (absent)
	 * @param buf
	 *            Preallocated array, optional
	 * @return R G B (A), one sample 0-255 per array element. Ready for
	 *         pngw.writeRowInt()
	 */
	public static int[] palette2rgba(final ImageLineInt line, final PngChunkPLTE pal, final PngChunkTRNS trns,
			final int[] buf) {
		return ImageLineHelper.palette2rgb(line, pal, trns, buf, true);
	}

	public static int[] palette2rgb(final ImageLineInt line, final PngChunkPLTE pal, final int[] buf) {
		return ImageLineHelper.palette2rgb(line, pal, null, buf, false);
	}

	/** this is not very efficient, only for tests and troubleshooting */
	public static int[] convert2rgba(final IImageLineArray line, final PngChunkPLTE pal, final PngChunkTRNS trns,
			int[] buf) {
		final ImageInfo imi = line.getImageInfo();
		final int nsamples = imi.cols * 4;
		if (buf == null || buf.length < nsamples)
			buf = new int[nsamples];
		final int maxval = imi.bitDepth == 16 ? (1 << 16) - 1 : 255;
		Arrays.fill(buf, maxval);

		if (imi.indexed) {
			final int tlen = trns != null ? trns.getPalletteAlpha().length : 0;
			for (int s = 0; s < imi.cols; s++) {
				final int index = line.getElem(s);
				pal.getEntryRgb(index, buf, s * 4);
				if (index < tlen)
					buf[s * 4 + 3] = trns.getPalletteAlpha()[index];
			}
		} else if (imi.greyscale) {
			int[] unpack = null;
			if (imi.bitDepth < 8)
				unpack = ImageLineHelper.DEPTH_UNPACK[imi.bitDepth];
			for (int s = 0, i = 0, p = 0; p < imi.cols; p++) {
				buf[s++] = unpack != null ? unpack[line.getElem(i++)] : line.getElem(i++);
				buf[s] = buf[s - 1];
				s++;
				buf[s] = buf[s - 1];
				s++;
				if (imi.channels == 2)
					buf[s++] = unpack != null ? unpack[line.getElem(i++)] : line.getElem(i++);
				else
					buf[s++] = maxval;
			}
		} else
			for (int s = 0, i = 0, p = 0; p < imi.cols; p++) {
				buf[s++] = line.getElem(i++);
				buf[s++] = line.getElem(i++);
				buf[s++] = line.getElem(i++);
				buf[s++] = imi.alpha ? line.getElem(i++) : maxval;
			}
		return buf;
	}

	private static int[] palette2rgb(final IImageLine line, final PngChunkPLTE pal, final PngChunkTRNS trns, int[] buf,
			final boolean alphaForced) {
		final boolean isalpha = trns != null;
		final int channels = isalpha ? 4 : 3;
		final ImageLineInt linei = (ImageLineInt) (line instanceof ImageLineInt ? line : null);
		final ImageLineByte lineb = (ImageLineByte) (line instanceof ImageLineByte ? line : null);
		final boolean isbyte = lineb != null;
		final int cols = linei != null ? linei.imgInfo.cols : lineb.imgInfo.cols;
		final int nsamples = cols * channels;
		if (buf == null || buf.length < nsamples)
			buf = new int[nsamples];
		final int nindexesWithAlpha = trns != null ? trns.getPalletteAlpha().length : 0;
		for (int c = 0; c < cols; c++) {
			final int index = isbyte ? lineb.scanline[c] & 0xFF : linei.scanline[c];
			pal.getEntryRgb(index, buf, c * channels);
			if (isalpha) {
				final int alpha = index < nindexesWithAlpha ? trns.getPalletteAlpha()[index] : 255;
				buf[c * channels + 3] = alpha;
			}
		}
		return buf;
	}

	/**
	 * what follows is pretty uninteresting/untested/obsolete, subject to change
	 */
	/**
	 * Just for basic info or debugging. Shows values for first and last pixel.
	 * Does not include alpha
	 */
	public static String infoFirstLastPixels(final ImageLineInt line) {
		return line.imgInfo.channels == 1 ? String.format("first=(%d) last=(%d)", line.scanline[0], line.scanline[line.scanline.length - 1]) : String.format("first=(%d %d %d) last=(%d %d %d)", line.scanline[0], line.scanline[1], line.scanline[2], line.scanline[line.scanline.length - line.imgInfo.channels], line.scanline[line.scanline.length - line.imgInfo.channels + 1], line.scanline[line.scanline.length - line.imgInfo.channels + 2]);
	}

	/**
	 * integer packed R G B only for bitdepth=8! (does not check!)
	 * 
	 **/
	public static int getPixelRGB8(final IImageLine line, final int column) {
		if (line instanceof ImageLineInt) {
			final int offset = column * ((ImageLineInt) line).imgInfo.channels;
			final int[] scanline = ((ImageLineInt) line).getScanline();
			return scanline[offset] << 16 | scanline[offset + 1] << 8 | scanline[offset + 2];
		} else if (line instanceof ImageLineByte) {
			final int offset = column * ((ImageLineByte) line).imgInfo.channels;
			final byte[] scanline = ((ImageLineByte) line).getScanline();
			return (scanline[offset] & 0xff) << 16 | (scanline[offset + 1] & 0xff) << 8 | scanline[offset + 2] & 0xff;
		} else
			throw new PngjException("Not supported " + line.getClass());
	}

	public static int getPixelARGB8(final IImageLine line, final int column) {
		if (line instanceof ImageLineInt) {
			final int offset = column * ((ImageLineInt) line).imgInfo.channels;
			final int[] scanline = ((ImageLineInt) line).getScanline();
			return scanline[offset + 3] << 24 | scanline[offset] << 16 | scanline[offset + 1] << 8 | scanline[offset + 2];
		} else if (line instanceof ImageLineByte) {
			final int offset = column * ((ImageLineByte) line).imgInfo.channels;
			final byte[] scanline = ((ImageLineByte) line).getScanline();
			return (scanline[offset + 3] & 0xff) << 24 | (scanline[offset] & 0xff) << 16 | (scanline[offset + 1] & 0xff) << 8 | scanline[offset + 2] & 0xff;
		} else
			throw new PngjException("Not supported " + line.getClass());
	}

	public static void setPixelsRGB8(final ImageLineInt line, final int[] rgb) {
		for (int i = 0, j = 0; i < line.imgInfo.cols; i++) {
			line.scanline[j++] = rgb[i] >> 16 & 0xFF;
			line.scanline[j++] = rgb[i] >> 8 & 0xFF;
			line.scanline[j++] = rgb[i] & 0xFF;
		}
	}

	public static void setPixelRGB8(final ImageLineInt line, int col, final int r, final int g, final int b) {
		col *= line.imgInfo.channels;
		line.scanline[col++] = r;
		line.scanline[col++] = g;
		line.scanline[col] = b;
	}

	public static void setPixelRGB8(final ImageLineInt line, final int col, final int rgb) {
		ImageLineHelper.setPixelRGB8(line, col, rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
	}

	public static void setPixelsRGBA8(final ImageLineInt line, final int[] rgb) {
		for (int i = 0, j = 0; i < line.imgInfo.cols; i++) {
			line.scanline[j++] = rgb[i] >> 16 & 0xFF;
			line.scanline[j++] = rgb[i] >> 8 & 0xFF;
			line.scanline[j++] = rgb[i] & 0xFF;
			line.scanline[j++] = rgb[i] >> 24 & 0xFF;
		}
	}

	public static void setPixelRGBA8(final ImageLineInt line, int col, final int r, final int g, final int b,
			final int a) {
		col *= line.imgInfo.channels;
		line.scanline[col++] = r;
		line.scanline[col++] = g;
		line.scanline[col++] = b;
		line.scanline[col] = a;
	}

	public static void setPixelRGBA8(final ImageLineInt line, final int col, final int rgb) {
		ImageLineHelper.setPixelRGBA8(line, col, rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, rgb >> 24 & 0xFF);
	}

	public static void setValD(final ImageLineInt line, final int i, final double d) {
		line.scanline[i] = ImageLineHelper.double2int(line, d);
	}

	public static int interpol(final int a, final int b, final int c, final int d, final double dx, final double dy) {
		// a b -> x (0-1)
		// c d
		final double e = a * (1.0 - dx) + b * dx;
		final double f = c * (1.0 - dx) + d * dx;
		return (int) (e * (1 - dy) + f * dy + 0.5);
	}

	public static double int2double(final ImageLineInt line, final int p) {
		return line.imgInfo.bitDepth == 16 ? p / 65535.0 : p / 255.0;
		// TODO: replace my multiplication? check for other bitdepths
	}

	public static double int2doubleClamped(final ImageLineInt line, final int p) {
		// TODO: replace my multiplication?
		final double d = line.imgInfo.bitDepth == 16 ? p / 65535.0 : p / 255.0;
		return d <= 0.0 ? 0 : d >= 1.0 ? 1.0 : d;
	}

	public static int double2int(final ImageLineInt line, double d) {
		d = d <= 0.0 ? 0 : d >= 1.0 ? 1.0 : d;
		return line.imgInfo.bitDepth == 16 ? (int) (d * 65535.0 + 0.5) : (int) (d * 255.0 + 0.5); //
	}

	public static int double2intClamped(final ImageLineInt line, double d) {
		d = d <= 0.0 ? 0 : d >= 1.0 ? 1.0 : d;
		return line.imgInfo.bitDepth == 16 ? (int) (d * 65535.0 + 0.5) : (int) (d * 255.0 + 0.5); //
	}

	public static int clampTo_0_255(final int i) {
		return i > 255 ? 255 : i < 0 ? 0 : i;
	}

	public static int clampTo_0_65535(final int i) {
		return i > 65535 ? 65535 : i < 0 ? 0 : i;
	}

	public static int clampTo_128_127(final int x) {
		return x > 127 ? 127 : x < -128 ? -128 : x;
	}

	public static int getMaskForPackedFormats(final int bitDepth) { // Utility function for pack/unpack
		if (bitDepth == 4)
			return 0xf0;
		else if (bitDepth == 2)
			return 0xc0;
		else
			return 0x80; // bitDepth == 1
	}

	public static int getMaskForPackedFormatsLs(final int bitDepth) { // Utility function for pack/unpack
		if (bitDepth == 4)
			return 0x0f;
		else if (bitDepth == 2)
			return 0x03;
		else
			return 0x01; // bitDepth == 1
	}

}
