package it.cavallium.warppi.math.rules.dsl;

import java.util.List;

/**
 * Thrown when processing DSL code which contains one or more errors.
 *
 * Contains a list of {@link DslError}s, which should not be empty.
 */
public class DslAggregateException extends Exception {
	private final List<DslError> errors;

	/**
	 * Constructs a <code>DslAggregateException</code> containing the specified list of errors.
	 * @param errors The list of errors. Should not be empty.
	 */
	public DslAggregateException(final List<DslError> errors) {
		this.errors = errors;
	}

	/**
	 * @return The list of errors detected in the DSL code.
	 */
	public List<DslError> getErrors() {
		return errors;
	}
}
