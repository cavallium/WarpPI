package it.cavallium.warppi;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.FileOutputStream;

public class BMPFile extends Component {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9182927946568629682L;
	// --- Private constants
	private final static int BITMAPFILEHEADER_SIZE = 14;
	private final static int BITMAPINFOHEADER_SIZE = 40;
	// --- Private variable declaration
	// --- Bitmap file header
	@SuppressWarnings("unused")
	private final byte bitmapFileHeader[] = new byte[14];
	private final byte bfType[] = { 'B', 'M' };
	private int bfSize = 0;
	private final int bfReserved1 = 0;
	private final int bfReserved2 = 0;
	private final int bfOffBits = BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE;
	// --- Bitmap info header
	@SuppressWarnings("unused")
	private final byte bitmapInfoHeader[] = new byte[40];
	private final int biSize = BITMAPINFOHEADER_SIZE;
	private int biWidth = 0;
	private int biHeight = 0;
	private final int biPlanes = 1;
	private final int biBitCount = 24;
	private final int biCompression = 0;
	private int biSizeImage = 0x030000;
	private final int biXPelsPerMeter = 0x0;
	private final int biYPelsPerMeter = 0x0;
	private final int biClrUsed = 0;
	private final int biClrImportant = 0;
	// --- Bitmap raw data
	private int bitmap[];
	// --- File section
	private FileOutputStream fo;

	// --- Default constructor
	public BMPFile() {}

	public void saveBitmap(String parFilename, Image parImage, int parWidth, int parHeight) {
		try {
			fo = new FileOutputStream(parFilename);
			save(parImage, parWidth, parHeight);
			fo.close();
		} catch (final Exception saveEx) {
			saveEx.printStackTrace();
		}
	}

	/*
	 * The saveMethod is the main method of the process. This method
	 * will call the convertImage method to convert the memory image to
	 * a byte array; method writeBitmapFileHeader creates and writes
	 * the bitmap file header; writeBitmapInfoHeader creates the
	 * information header; and writeBitmap writes the image.
	 *
	 */
	private void save(Image parImage, int parWidth, int parHeight) {
		try {
			convertImage(parImage, parWidth, parHeight);
			writeBitmapFileHeader();
			writeBitmapInfoHeader();
			writeBitmap();
		} catch (final Exception saveEx) {
			saveEx.printStackTrace();
		}
	}

	/*
	 * convertImage converts the memory image to the bitmap format (BRG).
	 * It also computes some information for the bitmap info header.
	 *
	 */
	private boolean convertImage(Image parImage, int parWidth, int parHeight) {
		int pad;
		bitmap = new int[parWidth * parHeight];
		final PixelGrabber pg = new PixelGrabber(parImage, 0, 0, parWidth, parHeight, bitmap, 0, parWidth);
		try {
			pg.grabPixels();
		} catch (final InterruptedException e) {
			e.printStackTrace();
			return (false);
		}
		pad = (4 - ((parWidth * 3) % 4)) * parHeight;
		biSizeImage = ((parWidth * parHeight) * 3) + pad;
		bfSize = biSizeImage + BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE;
		biWidth = parWidth;
		biHeight = parHeight;
		return (true);
	}

	/*
	 * writeBitmap converts the image returned from the pixel grabber to
	 * the format required. Remember: scan lines are inverted in
	 * a bitmap file!
	 *
	 * Each scan line must be padded to an even 4-byte boundary.
	 */
	private void writeBitmap() {
		int size;
		int value;
		int j;
		int i;
		int rowCount;
		int rowIndex;
		int lastRowIndex;
		int pad;
		int padCount;
		final byte rgb[] = new byte[3];
		size = (biWidth * biHeight) - 1;
		pad = 4 - ((biWidth * 3) % 4);
		if (pad == 4) {
			pad = 0; // <==== Bug correction
		}
		rowCount = 1;
		padCount = 0;
		rowIndex = size - biWidth;
		lastRowIndex = rowIndex;
		try {
			for (j = 0; j < size; j++) {
				value = bitmap[rowIndex];
				rgb[0] = (byte) (value & 0xFF);
				rgb[1] = (byte) ((value >> 8) & 0xFF);
				rgb[2] = (byte) ((value >> 16) & 0xFF);
				fo.write(rgb);
				if (rowCount == biWidth) {
					padCount += pad;
					for (i = 1; i <= pad; i++) {
						fo.write(0x00);
					}
					rowCount = 1;
					rowIndex = lastRowIndex - biWidth;
					lastRowIndex = rowIndex;
				} else {
					rowCount++;
				}
				rowIndex++;
			}
			// --- Update the size of the file
			bfSize += padCount - pad;
			biSizeImage += padCount - pad;
		} catch (final Exception wb) {
			wb.printStackTrace();
		}
	}

	/*
	 * writeBitmapFileHeader writes the bitmap file header to the file.
	 *
	 */
	private void writeBitmapFileHeader() {
		try {
			fo.write(bfType);
			fo.write(intToDWord(bfSize));
			fo.write(intToWord(bfReserved1));
			fo.write(intToWord(bfReserved2));
			fo.write(intToDWord(bfOffBits));
		} catch (final Exception wbfh) {
			wbfh.printStackTrace();
		}
	}

	/*
	 *
	 * writeBitmapInfoHeader writes the bitmap information header
	 * to the file.
	 *
	 */
	private void writeBitmapInfoHeader() {
		try {
			fo.write(intToDWord(biSize));
			fo.write(intToDWord(biWidth));
			fo.write(intToDWord(biHeight));
			fo.write(intToWord(biPlanes));
			fo.write(intToWord(biBitCount));
			fo.write(intToDWord(biCompression));
			fo.write(intToDWord(biSizeImage));
			fo.write(intToDWord(biXPelsPerMeter));
			fo.write(intToDWord(biYPelsPerMeter));
			fo.write(intToDWord(biClrUsed));
			fo.write(intToDWord(biClrImportant));
		} catch (final Exception wbih) {
			wbih.printStackTrace();
		}
	}

	/*
	 *
	 * intToWord converts an int to a word, where the return
	 * value is stored in a 2-byte array.
	 *
	 */
	private byte[] intToWord(int parValue) {
		final byte retValue[] = new byte[2];
		retValue[0] = (byte) (parValue & 0x00FF);
		retValue[1] = (byte) ((parValue >> 8) & 0x00FF);
		return (retValue);
	}

	/*
	 *
	 * intToDWord converts an int to a double word, where the return
	 * value is stored in a 4-byte array.
	 *
	 */
	private byte[] intToDWord(int parValue) {
		final byte retValue[] = new byte[4];
		retValue[0] = (byte) (parValue & 0x00FF);
		retValue[1] = (byte) ((parValue >> 8) & 0x000000FF);
		retValue[2] = (byte) ((parValue >> 16) & 0x000000FF);
		retValue[3] = (byte) ((parValue >> 24) & 0x000000FF);
		return (retValue);
	}
}