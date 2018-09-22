package ar.com.hjg.pngj;

import java.io.File;
import java.io.OutputStream;

import ar.com.hjg.pngj.pixels.PixelsWriter;
import ar.com.hjg.pngj.pixels.PixelsWriterMultiple;

/** Pngwriter with High compression EXPERIMENTAL */
public class PngWriterHc extends PngWriter {

	public PngWriterHc(final File file, final ImageInfo imgInfo, final boolean allowoverwrite) {
		super(file, imgInfo, allowoverwrite);
		setFilterType(FilterType.FILTER_SUPER_ADAPTIVE);
	}

	public PngWriterHc(final File file, final ImageInfo imgInfo) {
		super(file, imgInfo);
	}

	public PngWriterHc(final OutputStream outputStream, final ImageInfo imgInfo) {
		super(outputStream, imgInfo);
	}

	@Override
	protected PixelsWriter createPixelsWriter(final ImageInfo imginfo) {
		final PixelsWriterMultiple pw = new PixelsWriterMultiple(imginfo);
		return pw;
	}

	public PixelsWriterMultiple getPixelWriterMultiple() {
		return (PixelsWriterMultiple) pixelsWriter;
	}

}
