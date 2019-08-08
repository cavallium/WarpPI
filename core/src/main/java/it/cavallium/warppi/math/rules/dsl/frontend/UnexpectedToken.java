package it.cavallium.warppi.math.rules.dsl.frontend;

import it.cavallium.warppi.math.rules.dsl.DslError;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Occurs when DSL source code contains a token which doesn't match the grammar.
 */
public class UnexpectedToken implements DslError {
	private final Token unexpected;
	private final Set<TokenType> suggested;

	public UnexpectedToken(final Token unexpected, final Set<TokenType> suggested) {
		this.unexpected = unexpected;
		this.suggested = suggested;
	}

	public UnexpectedToken(final Token unexpected, final TokenType... suggested) {
		this.unexpected = unexpected;
		this.suggested = new HashSet<>(Arrays.asList(suggested)); // TeaVM doesn't support Set.of
	}

	@Override
	public int getPosition() {
		return unexpected.position;
	}

	@Override
	public int getLength() {
		return unexpected.lexeme.length();
	}

	@Override
	public <T> T accept(final DslError.Visitor<T> visitor) {
		return visitor.visit(this);
	}

	/**
	 * @return The unexpected token.
	 */
	public Token getUnexpected() {
		return unexpected;
	}

	/**
	 * @return A (possibly empty) set of <code>TokenType</code>s which would match the grammar.
	 *         As the name implies, this is only a suggestion: the parser can't list all allowed token types
	 *         when it detects an error.
	 */
	public Set<TokenType> getSuggested() {
		return suggested;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof UnexpectedToken)) {
			return false;
		}
		final UnexpectedToken other = (UnexpectedToken) o;
		return this.unexpected.equals(other.unexpected) && this.suggested.equals(other.suggested);
	}

	@Override
	public int hashCode() {
		return Objects.hash(unexpected, suggested);
	}
}
