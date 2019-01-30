package it.cavallium.warppi.math.rules.dsl;

import java.util.List;

/**
 * Thrown when processing DSL code which contains one or more errors.
 *
 * Contains a list of {@link DslException}s, which should not be empty.
 */
public class DslAggregateException extends Exception {
	private final List<DslException> exceptions;

	/**
	 * Constructs a <code>DslAggregateException</code> containing the specified list of exceptions.
	 * @param exceptions The list of exceptions. Should not be empty.
	 */
	public DslAggregateException(final List<DslException> exceptions) {
		this.exceptions = exceptions;
	}

	/**
	 * @return The list of errors detected in the DSL code.
	 */
	public List<DslException> getExceptions() {
		return exceptions;
	}
}
