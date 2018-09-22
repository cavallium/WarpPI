package ar.com.hjg.pngj;

/**
 * Exception thrown when reading a PNG.
 */
public class PngjInputException extends PngjException {
	private static final long serialVersionUID = 1L;

	public PngjInputException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public PngjInputException(final String message) {
		super(message);
	}

	public PngjInputException(final Throwable cause) {
		super(cause);
	}
}
