package it.cavallium.warppi.math.rules.dsl.frontend;

import it.cavallium.warppi.math.rules.dsl.DslError;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static it.cavallium.warppi.math.rules.dsl.frontend.TokenType.*;

/**
 * Converts the source <code>String</code> to a list of {@link Token}s.
 */
public class Lexer {
	private static final Map<String, TokenType> KEYWORDS;
	static {
		TokenType[] keywordTokenTypes = {
			REDUCTION, EXPANSION, CALCULATION, EXISTENCE,
			ARCCOS, ARCSIN, ARCTAN, COS, SIN, TAN, ROOT, SQRT, LOG,
			UNDEFINED, PI, E
		};
		Map<String, TokenType> map = new HashMap<>();
		for (TokenType type : keywordTokenTypes) {
			map.put(type.name().toLowerCase(), type);
		}
		KEYWORDS = Collections.unmodifiableMap(map);
	}

	private final String source;
	private final Consumer<? super DslError> errorReporter;

	private boolean used = false;
	private final List<Token> tokens = new ArrayList<>();
	private int startOfLexeme = 0;
	private int curPosition = 0;
	private UnexpectedCharacters unexpectedCharacters = null;

	/**
	 * Constructs a <code>Lexer</code> that will split the given source code into {@link Token}s.
	 *
	 * @param source        a <code>String</code> containing the DSL source code to process.
	 * @param errorReporter a <code>Consumer</code> used to report each <code>DslError</code> that the
	 *                      <code>Lexer</code> finds within the source string.
	 */
	public Lexer(final String source, final Consumer<? super DslError> errorReporter) {
		this.source = source;
		this.errorReporter = errorReporter;
	}

	/**
	 * Runs the <code>Lexer</code>.
	 * <p>
	 * This method can only be called once per instance.
	 *
	 * @return the list of <code>Token</code>s extracted from the source string.
	 * 		   If any errors are reported, this list should not be considered to represent a valid set of DSL rules,
	 * 		   but it can still be parsed to potentially find additional errors (which may allow the user to fix more
	 * 		   errors before having to rerun the <code>Lexer</code>).
	 * @throws IllegalStateException if called multiple times on the same instance.
	 */
	public List<Token> lex() {
		if (used) {
			throw new IllegalStateException("Lexer.lex can only be called once per instance");
		}
		used = true;

		while (!atEnd()) {
			startOfLexeme = curPosition;
			lexAndHandleErrors();
		}
		// lexAndHandleErrors reports unexpected characters when they're followed by expected ones:
		// if there are unexpected characters at the end of the source, they have to be reported here
		reportAndClearUnexpectedCharacters();
		tokens.add(new Token(EOF, "", source.length()));
		return tokens;
	}

	private void lexAndHandleErrors() {
		try {
			lexToken();
			reportAndClearUnexpectedCharacters(); // After finding some expected characters
		} catch (final SyntaxException e) {
			final DslError error = e.getError();
			if (error instanceof UnexpectedCharacters) {
				addUnexpectedCharacters((UnexpectedCharacters) error);
			} else {
				// If there are multiple errors, report them in the order in which they occur in the source
				reportAndClearUnexpectedCharacters();
				errorReporter.accept(error);
			}
		}
	}

	private void addUnexpectedCharacters(final UnexpectedCharacters unexpected) {
		if (unexpectedCharacters == null) {
			unexpectedCharacters = unexpected;
		} else {
			unexpectedCharacters = unexpectedCharacters.concat(unexpected);
		}
	}

	private void reportAndClearUnexpectedCharacters() {
		if (unexpectedCharacters == null) {
			return;
		}
		errorReporter.accept(unexpectedCharacters);
		unexpectedCharacters = null;
	}

	private void lexToken() throws SyntaxException {
		char current = popChar();
		switch (current) {
			case ':': emitToken(COLON); break;
			case ',': emitToken(COMMA); break;
			case '(': emitToken(LEFT_PAREN); break;
			case ')': emitToken(RIGHT_PAREN); break;
			case '[': emitToken(LEFT_BRACKET); break;
			case ']': emitToken(RIGHT_BRACKET); break;
			case '=': emitToken(EQUALS); break;
			case '*': emitToken(TIMES); break;
			case '^': emitToken(POWER); break;

			case '+':
				if (matchChar('-')) {
					emitToken(PLUS_MINUS);
				} else {
					emitToken(PLUS);
				}
				break;

			case '-':
				if (matchChar('>')) {
					emitToken(ARROW);
				} else {
					emitToken(MINUS);
				}
				break;

			case '/':
				if (matchChar('/')) {
					singleLineComment();
				} else if (matchChar('*')) {
					multiLineComment();
				} else {
					emitToken(DIVIDE);
				}
				break;

			default:
				if (isAsciiDigit(current)) {
					number();
				} else if (Character.isJavaIdentifierStart(current)) {
					keywordOrIdentifier();
				} else if (!Character.isWhitespace(current)) {
					throw new SyntaxException(
							new UnexpectedCharacters(curPosition - 1, String.valueOf(current))
					);
				}
		}
	}

	private void singleLineComment() {
		matchWhile(c -> c != '\n');
	}

	private void multiLineComment() throws SyntaxException {
		while (!(matchChar('*') && matchChar('/'))) {
			if (atEnd()) {
				throw new SyntaxException(new UnterminatedComment(startOfLexeme));
			}
			popChar();
		}
	}

	private void number() throws SyntaxException {
		matchWhile(Lexer::isAsciiDigit);
		if (matchChar('.') && matchWhile(Lexer::isAsciiDigit) == 0) {
			throw new SyntaxException(
					new IncompleteNumberLiteral(startOfLexeme, currentLexeme())
			);
		}
		emitToken(NUMBER);
	}

	private void keywordOrIdentifier() {
		matchWhile(Character::isJavaIdentifierPart);
		TokenType type = KEYWORDS.getOrDefault(currentLexeme(), IDENTIFIER);
		emitToken(type);
	}

	private char popChar() {
		char current = source.charAt(curPosition);
		curPosition++;
		return current;
	}

	private boolean matchChar(char expected) {
		if (atEnd() || source.charAt(curPosition) != expected) {
			return false;
		}
		curPosition++;
		return true;
	}

	private int matchWhile(Predicate<Character> predicate) {
		int matched = 0;
		while (!atEnd() && predicate.test(source.charAt(curPosition))) {
			curPosition++;
			matched++;
		}
		return matched;
	}

	private void emitToken(TokenType type) {
		tokens.add(new Token(type, currentLexeme(), startOfLexeme));
	}

	private String currentLexeme() {
		return source.substring(startOfLexeme, curPosition);
	}

	private boolean atEnd() {
		return curPosition >= source.length();
	}

	// Character.isDigit also allows various Unicode digits
	private static boolean isAsciiDigit(char c) {
		return '0' <= c && c <= '9';
	}
}
