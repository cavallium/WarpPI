package it.cavallium.warppi.math.rules.dsl.frontend;

import it.cavallium.warppi.math.rules.dsl.DslException;

import java.util.Objects;

/**
 * Thrown when DSL source code contains one or more (consecutive) characters which are not expected by the lexer.
 */
public class UnexpectedCharactersException extends DslException {
	private final int position;
	private final String unexpectedCharacters;

	public UnexpectedCharactersException(final int position, final String unexpectedCharacters) {
		this.position = position;
		this.unexpectedCharacters = unexpectedCharacters;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public int getLength() {
		return unexpectedCharacters.length();
	}

	/**
	 * @return The string of one or more consecutive unexpected characters.
	 */
	public String getUnexpectedCharacters() {
		return unexpectedCharacters;
	}

	UnexpectedCharactersException concat(UnexpectedCharactersException other) {
		return new UnexpectedCharactersException(this.position, this.unexpectedCharacters + other.unexpectedCharacters);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof UnexpectedCharactersException)) {
			return false;
		}
		final UnexpectedCharactersException other = (UnexpectedCharactersException) o;
		return this.position == other.position && this.unexpectedCharacters.equals(other.unexpectedCharacters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(position, unexpectedCharacters);
	}
}
