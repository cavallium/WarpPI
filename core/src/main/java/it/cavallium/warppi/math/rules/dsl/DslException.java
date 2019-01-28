package it.cavallium.warppi.math.rules.dsl;

/**
 * The superclass of all exceptions which represent errors in DSL code.
 */
public abstract class DslException extends Exception {
	/**
	 * @return The index at which the error starts in the source string.
	 */
	public abstract int getPosition();

	/**
	 * @return The length of the error in the source string.
	 */
	public abstract int getLength();
}
