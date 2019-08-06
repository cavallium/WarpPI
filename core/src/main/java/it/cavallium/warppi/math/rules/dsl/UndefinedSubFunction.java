package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.rules.dsl.frontend.Token;

import java.util.Objects;

/**
 * Occurs when a sub-function is used in one of the replacement pattern of a <code>PatternRule</code>,
 * but not defined (captured) in the target pattern.
 */
public class UndefinedSubFunction implements DslError {
	private final Token identifier;

	public UndefinedSubFunction(final Token identifier) {
		this.identifier = identifier;
	}

	@Override
	public int getPosition() {
		return identifier.position;
	}

	@Override
	public int getLength() {
		return identifier.lexeme.length();
	}

	@Override
	public <T> T accept(final DslError.Visitor<T> visitor) {
		return visitor.visit(this);
	}

	/**
	 * @return The name of the undefined sub-function.
	 */
	public String getName() {
		return identifier.lexeme;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof UndefinedSubFunction)) {
			return false;
		}
		final UndefinedSubFunction other = (UndefinedSubFunction) o;
		return this.identifier.equals(other.identifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier);
	}
}
