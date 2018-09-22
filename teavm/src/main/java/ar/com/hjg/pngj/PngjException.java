package ar.com.hjg.pngj;

/**
 * Generic exception for this library. It's a RuntimeException (unchecked)
 */
public class PngjException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PngjException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public PngjException(final String message) {
		super(message);
	}

	public PngjException(final Throwable cause) {
		super(cause);
	}
}
