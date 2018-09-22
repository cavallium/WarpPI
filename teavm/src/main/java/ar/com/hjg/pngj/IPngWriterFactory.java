package ar.com.hjg.pngj;

import java.io.OutputStream;

public interface IPngWriterFactory {
	PngWriter createPngWriter(OutputStream outputStream, ImageInfo imgInfo);
}
