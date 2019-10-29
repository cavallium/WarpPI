package it.cavallium.warppi.math.rules.dsl;

/**
 * Thrown when a <code>SubFunctionPattern</code> is used to generate a <code>Function</code>, but the named sub-function
 * it references is not defined.
 */
public class UndefinedSubFunctionException extends RuntimeException {
	private final String subFunctionName;

	/**
	 * Constructs an <code>UndefinedSubFunction</code> instance with the specified sub-function name.
	 *
	 * @param subFunctionName the name of the undefined sub-function.
	 */
	public UndefinedSubFunctionException(final String subFunctionName) {
		super("Sub-function '" + subFunctionName + "' is not defined");
		this.subFunctionName = subFunctionName;
	}

	/**
	 * @return the name of the undefined sub-function.
	 */
	public String getSubFunctionName() {
		return subFunctionName;
	}
}
