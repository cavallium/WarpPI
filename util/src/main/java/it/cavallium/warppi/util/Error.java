package it.cavallium.warppi.util;

public class Error extends java.lang.Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -1014947815755694651L;

	public Error(final Errors errorID) {
		super(errorID.toString());
		id = errorID;
	}

	public Error(final Errors errorID, final String errorMessage) {
		super(errorID.toString() + ": " + errorMessage);
		id = errorID;
	}

	public Errors id = Errors.ERROR;
}
