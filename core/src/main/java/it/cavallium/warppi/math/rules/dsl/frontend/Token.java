package it.cavallium.warppi.math.rules.dsl.frontend;

import java.util.Objects;

public class Token {
	/** The type of the token. */
	public final TokenType type;
	/** The source string which corresponds to the token. */
	public final String lexeme;
	/** The index at which the token starts in the source string. */
	public final int position;

	public Token(final TokenType type, final String lexeme, final int position) {
		this.type = type;
		this.lexeme = lexeme;
		this.position = position;
	}

	@Override
	public String toString() {
		return String.format("%s(\"%s\")@%d", type, lexeme, position);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Token)) {
			return false;
		}
		Token other = (Token) o;
		return type == other.type && lexeme.equals(other.lexeme) && position == other.position;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, lexeme, position);
	}
}
