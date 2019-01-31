package it.cavallium.warppi.math.rules.dsl;

/**
 * Represents an error in DSL code.
 */
public interface DslError {
	/**
	 * @return The index at which the error starts in the source string.
	 */
	int getPosition();

	/**
	 * @return The length of the error in the source string.
	 */
	int getLength();
}
