package it.cavallium.warppi.math.rules.dsl.frontend;

import it.cavallium.warppi.math.rules.dsl.DslException;

import java.util.Objects;

/**
 * Thrown when DSL source code contains a number literal with a decimal separator which is not followed by digits.
 * <p>
 * An example of an incomplete literal is <code>2.</code>, while <code>2</code> and <code>2.2</code> are valid.
 */
public class IncompleteNumberLiteralException extends DslException {
	private final int position;
	private final String literal;

	public IncompleteNumberLiteralException(final int position, final String literal) {
		this.position = position;
		this.literal = literal;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public int getLength() {
		return literal.length();
	}

	/**
	 * @return The incomplete number literal.
	 */
	public String getLiteral() {
		return literal;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof IncompleteNumberLiteralException)) {
			return false;
		}
		final IncompleteNumberLiteralException other = (IncompleteNumberLiteralException) o;
		return this.position == other.position && this.literal.equals(other.literal);
	}

	@Override
	public int hashCode() {
		return Objects.hash(position, literal);
	}
}
