package it.cavallium.warppi.math.rules.dsl.frontend;

import it.cavallium.warppi.math.rules.dsl.DslError;

/**
 * Thrown when DSL source code contains a syntax error.
 * <p>
 * Used for error reporting and recovery in the implementation of {@link Lexer} and {@link Parser}.
 */
class SyntaxException extends Exception {
	private final DslError error;

	SyntaxException(final DslError error) {
		this.error = error;
	}

	public DslError getError() {
		return error;
	}
}
