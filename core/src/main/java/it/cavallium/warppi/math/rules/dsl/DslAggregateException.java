package it.cavallium.warppi.math.rules.dsl;

import java.util.List;
import java.util.Objects;

/**
 * Thrown when processing DSL code which contains one or more errors.
 *
 * Contains a non-empty list of {@link DslError}s.
 */
public class DslAggregateException extends Exception {
	private final List<DslError> errors;

	/**
	 * Constructs a <code>DslAggregateException</code> containing the specified list of errors.
	 * @param errors The (non-empty) list of errors.
	 * @throws IllegalArgumentException If the list of errors is empty.
	 */
	public DslAggregateException(final List<DslError> errors) {
		if (errors.isEmpty()) {
			throw new IllegalArgumentException("The list of errors can't be empty");
		}
		this.errors = errors;
	}

	/**
	 * @return The list of errors detected in the DSL code.
	 */
	public List<DslError> getErrors() {
		return errors;
	}
}
