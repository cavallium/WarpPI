package it.cavallium.warppi.math.rules.dsl.frontend;

import it.cavallium.warppi.math.rules.dsl.DslError;

import java.util.Objects;

/**
 * Occurs when DSL source code contains one or more (consecutive) characters which are not expected by the lexer.
 */
public class UnexpectedCharacters implements DslError {
	private final int position;
	private final String unexpectedCharacters;

	public UnexpectedCharacters(final int position, final String unexpectedCharacters) {
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

	UnexpectedCharacters concat(UnexpectedCharacters other) {
		return new UnexpectedCharacters(this.position, this.unexpectedCharacters + other.unexpectedCharacters);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof UnexpectedCharacters)) {
			return false;
		}
		final UnexpectedCharacters other = (UnexpectedCharacters) o;
		return this.position == other.position && this.unexpectedCharacters.equals(other.unexpectedCharacters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(position, unexpectedCharacters);
	}
}
