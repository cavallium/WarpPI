package it.cavallium.warppi.math.rules.dsl.frontend;

import java.util.Objects;

/**
 * Represents a single token extracted from DSL source code.
 * <p>
 * <code>Token</code>s are produced by the {@link Lexer} and consumed by the {@link Parser}.
 */
public class Token {
	/** The type of the token. */
	public final TokenType type;
	/**
	 * The part of the source code which corresponds to the token.
	 * <p>
	 * Some types of token always have the same lexemes (for example, <code>PLUS</code> is always represented by
	 * <code>"+"</code>), while others have variable lexemes (like <code>IDENTIFIER</code>, which may correspond to any
	 * valid identifier).
	 * <p>
	 * As a special case, tokens of type <code>EOF</code> (which signal the end of the source code) have empty lexemes
	 * (<code>""</code>). Such tokens only exist to simplify the parser code, by allowing the end of the input to be
	 * treated like any other token (which is especially useful for error handling, because an unexpected end of input
	 * just becomes an "unexpected token" error).
	 */
	public final String lexeme;
	/** The index at which the token starts in the source string. */
	public final int position;

	/**
	 * Constructs a <code>Token</code>.
	 *
	 * @param type     the type of the token.
	 * @param lexeme   the part of the source string which corresponds to the token.
	 * @param position the index at which the token starts in the source string.
	 */
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
