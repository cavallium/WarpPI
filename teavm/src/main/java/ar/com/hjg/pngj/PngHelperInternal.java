package ar.com.hjg.pngj;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * Some utility static methods for internal use.
 * <p>
 * Client code should not normally use this class
 * <p>
 */
public final class PngHelperInternal {

	public static final String KEY_LOGGER = "ar.com.pngj";
	public static final Logger LOGGER = Logger.getLogger(PngHelperInternal.KEY_LOGGER);

	/**
	 * Default charset, used internally by PNG for several things
	 */
	public static String charsetLatin1name = "UTF-8";
	public static Charset charsetLatin1 = Charset.forName(PngHelperInternal.charsetLatin1name);
	/**
	 * UTF-8 is only used for some chunks
	 */
	public static String charsetUTF8name = "UTF-8";
	public static Charset charsetUTF8 = Charset.forName(PngHelperInternal.charsetUTF8name);

	private static ThreadLocal<Boolean> DEBUG = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	};

	/**
	 * PNG magic bytes
	 */
	public static byte[] getPngIdSignature() {
		return new byte[] { -119, 80, 78, 71, 13, 10, 26, 10 };
	}

	public static int doubleToInt100000(final double d) {
		return (int) (d * 100000.0 + 0.5);
	}

	public static double intToDouble100000(final int i) {
		return i / 100000.0;
	}

	public static int readByte(final InputStream is) {
		try {
			return is.read();
		} catch (final IOException e) {
			throw new PngjInputException("error reading byte", e);
		}
	}

	/**
	 * -1 if eof
	 * 
	 * PNG uses "network byte order"
	 */
	public static int readInt2(final InputStream is) {
		try {
			final int b1 = is.read();
			final int b2 = is.read();
			if (b1 == -1 || b2 == -1)
				return -1;
			return b1 << 8 | b2;
		} catch (final IOException e) {
			throw new PngjInputException("error reading Int2", e);
		}
	}

	/**
	 * -1 if eof
	 */
	public static int readInt4(final InputStream is) {
		try {
			final int b1 = is.read();
			final int b2 = is.read();
			final int b3 = is.read();
			final int b4 = is.read();
			if (b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1)
				return -1;
			return b1 << 24 | b2 << 16 | (b3 << 8) + b4;
		} catch (final IOException e) {
			throw new PngjInputException("error reading Int4", e);
		}
	}

	public static int readInt1fromByte(final byte[] b, final int offset) {
		return b[offset] & 0xff;
	}

	public static int readInt2fromBytes(final byte[] b, final int offset) {
		return (b[offset] & 0xff) << 8 | b[offset + 1] & 0xff;
	}

	public static int readInt4fromBytes(final byte[] b, final int offset) {
		return (b[offset] & 0xff) << 24 | (b[offset + 1] & 0xff) << 16 | (b[offset + 2] & 0xff) << 8 | b[offset + 3] & 0xff;
	}

	public static void writeByte(final OutputStream os, final byte b) {
		try {
			os.write(b);
		} catch (final IOException e) {
			throw new PngjOutputException(e);
		}
	}

	public static void writeByte(final OutputStream os, final byte[] bs) {
		try {
			os.write(bs);
		} catch (final IOException e) {
			throw new PngjOutputException(e);
		}
	}

	public static void writeInt2(final OutputStream os, final int n) {
		final byte[] temp = { (byte) (n >> 8 & 0xff), (byte) (n & 0xff) };
		PngHelperInternal.writeBytes(os, temp);
	}

	public static void writeInt4(final OutputStream os, final int n) {
		final byte[] temp = new byte[4];
		PngHelperInternal.writeInt4tobytes(n, temp, 0);
		PngHelperInternal.writeBytes(os, temp);
	}

	public static void writeInt2tobytes(final int n, final byte[] b, final int offset) {
		b[offset] = (byte) (n >> 8 & 0xff);
		b[offset + 1] = (byte) (n & 0xff);
	}

	public static void writeInt4tobytes(final int n, final byte[] b, final int offset) {
		b[offset] = (byte) (n >> 24 & 0xff);
		b[offset + 1] = (byte) (n >> 16 & 0xff);
		b[offset + 2] = (byte) (n >> 8 & 0xff);
		b[offset + 3] = (byte) (n & 0xff);
	}

	/**
	 * guaranteed to read exactly len bytes. throws error if it can't
	 */
	public static void readBytes(final InputStream is, final byte[] b, final int offset, final int len) {
		if (len == 0)
			return;
		try {
			int read = 0;
			while (read < len) {
				final int n = is.read(b, offset + read, len - read);
				if (n < 1)
					throw new PngjInputException("error reading bytes, " + n + " !=" + len);
				read += n;
			}
		} catch (final IOException e) {
			throw new PngjInputException("error reading", e);
		}
	}

	public static void skipBytes(final InputStream is, long len) {
		try {
			while (len > 0) {
				final long n1 = is.skip(len);
				if (n1 > 0)
					len -= n1;
				else if (n1 == 0) { // should we retry? lets read one byte
					if (is.read() == -1) // EOF
						break;
					else
						len--;
				} else
					// negative? this should never happen but...
					throw new IOException("skip() returned a negative value ???");
			}
		} catch (final IOException e) {
			throw new PngjInputException(e);
		}
	}

	public static void writeBytes(final OutputStream os, final byte[] b) {
		try {
			os.write(b);
		} catch (final IOException e) {
			throw new PngjOutputException(e);
		}
	}

	public static void writeBytes(final OutputStream os, final byte[] b, final int offset, final int n) {
		try {
			os.write(b, offset, n);
		} catch (final IOException e) {
			throw new PngjOutputException(e);
		}
	}

	public static void logdebug(final String msg) {
		if (PngHelperInternal.isDebug())
			System.err.println("logdebug: " + msg);
	}

	// / filters
	public static int filterRowNone(final int r) {
		return r & 0xFF;
	}

	public static int filterRowSub(final int r, final int left) {
		return r - left & 0xFF;
	}

	public static int filterRowUp(final int r, final int up) {
		return r - up & 0xFF;
	}

	public static int filterRowAverage(final int r, final int left, final int up) {
		return r - (left + up) / 2 & 0xFF;
	}

	public static int filterRowPaeth(final int r, final int left, final int up, final int upleft) { // a = left, b = above, c
		// = upper left
		return r - PngHelperInternal.filterPaethPredictor(left, up, upleft) & 0xFF;
	}

	static int filterPaethPredictor(final int a, final int b, final int c) { // a = left, b =
																				// above, c = upper
																				// left
																				// from http://www.libpng.org/pub/png/spec/1.2/PNG-Filters.html

		final int p = a + b - c;// ; initial estimate
		final int pa = p >= a ? p - a : a - p;
		final int pb = p >= b ? p - b : b - p;
		final int pc = p >= c ? p - c : c - p;
		// ; return nearest of a,b,c,
		// ; breaking ties in order a,b,c.
		if (pa <= pb && pa <= pc)
			return a;
		else if (pb <= pc)
			return b;
		else
			return c;
	}

	/**
	 * Prits a debug message (prints class name, method and line number)
	 * 
	 * @param obj
	 *            : Object to print
	 */
	public static void debug(final Object obj) {
		PngHelperInternal.debug(obj, 1, true);
	}

	/**
	 * Prits a debug message (prints class name, method and line number)
	 * 
	 * @param obj
	 *            : Object to print
	 * @param offset
	 *            : Offset N lines from stacktrace
	 */
	static void debug(final Object obj, final int offset) {
		PngHelperInternal.debug(obj, offset, true);
	}

	public static InputStream istreamFromFile(final File f) {
		FileInputStream is;
		try {
			is = new FileInputStream(f);
		} catch (final Exception e) {
			throw new PngjInputException("Could not open " + f, e);
		}
		return is;
	}

	static OutputStream ostreamFromFile(final File f) {
		return PngHelperInternal.ostreamFromFile(f, true);
	}

	static OutputStream ostreamFromFile(final File f, final boolean overwrite) {
		return PngHelperInternal2.ostreamFromFile(f, overwrite);
	}

	/**
	 * Prints a debug message (prints class name, method and line number) to
	 * stderr and logFile
	 * 
	 * @param obj
	 *            : Object to print
	 * @param offset
	 *            : Offset N lines from stacktrace
	 * @param newLine
	 *            : Print a newline char at the end ('\n')
	 */
	static void debug(final Object obj, final int offset, final boolean newLine) {
		final StackTraceElement ste = new Exception().getStackTrace()[1 + offset];
		String steStr = ste.getClassName();
		final int ind = steStr.lastIndexOf('.');
		steStr = steStr.substring(ind + 1);
		steStr += "." + ste.getMethodName() + "(" + ste.getLineNumber() + "): " + (obj == null ? null : obj.toString());
		System.err.println(steStr);
	}

	/**
	 * Sets a global debug flag. This is bound to a thread.
	 */
	public static void setDebug(final boolean b) {
		PngHelperInternal.DEBUG.set(b);
	}

	public static boolean isDebug() {
		return PngHelperInternal.DEBUG.get().booleanValue();
	}

	public static long getDigest(final PngReader pngr) {
		return pngr.getSimpleDigest();
	}

	public static void initCrcForTests(final PngReader pngr) {
		pngr.prepareSimpleDigestComputation();
	}

	public static long getRawIdatBytes(final PngReader r) { // in case of image with frames, returns the current one
		return r.interlaced ? r.getChunkseq().getDeinterlacer().getTotalRawBytes() : r.getCurImgInfo().getTotalRawBytes();
	}

}
