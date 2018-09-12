package it.cavallium.warppi.util;

public class Error extends java.lang.Throwable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1014947815755694651L;

	public Error(Errors errorID) {
		super(errorID.toString());
		id = errorID;
	}

	public Error(Errors errorID, String errorMessage) {
		super(errorID.toString() + ": " + errorMessage);
		id = errorID;
	}

	public Errors id = Errors.ERROR;
}
