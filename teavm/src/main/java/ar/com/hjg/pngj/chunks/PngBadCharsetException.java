package ar.com.hjg.pngj.chunks;

import ar.com.hjg.pngj.PngjException;

public class PngBadCharsetException extends PngjException {
	private static final long serialVersionUID = 1L;

	public PngBadCharsetException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public PngBadCharsetException(final String message) {
		super(message);
	}

	public PngBadCharsetException(final Throwable cause) {
		super(cause);
	}

}
