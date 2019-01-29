package it.cavallium.warppi.math.rules.dsl.frontend;

import it.cavallium.warppi.math.rules.dsl.DslException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.cavallium.warppi.math.rules.dsl.frontend.TokenType.*;

/**
 * Converts the source string to a list of tokens.
 */
public class Lexer {
	private static final Map<String, TokenType> keywords = Stream.of(
			REDUCTION, EXPANSION, CALCULATION, EXISTENCE,
			ARCCOS, ARCSIN, ARCTAN, COS, SIN, TAN, ROOT, SQRT, LOG,
			UNDEFINED, PI, E
	).collect(Collectors.toMap(
			tokenType -> tokenType.name().toLowerCase(),
			Function.identity()
	));

	private final String source;
	private final Consumer<? super DslException> errorReporter;

	private final List<Token> tokens = new ArrayList<>();
	private int startOfLexeme = 0;
	private int curPosition = 0;
	private UnexpectedCharactersException unexpectedCharacters = null;

	public Lexer(final String source, final Consumer<? super DslException> errorReporter) {
		this.source = source;
		this.errorReporter = errorReporter;
	}

	public List<Token> lex() {
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
		} catch (UnexpectedCharactersException e) {
			if (unexpectedCharacters == null) {
				unexpectedCharacters = e;
			} else {
				unexpectedCharacters = unexpectedCharacters.concat(e);
			}
		} catch (IncompleteNumberLiteralException e) {
			// If there are multiple errors, report them in the order in which they occur in the source
			reportAndClearUnexpectedCharacters();
			errorReporter.accept(e);
		}
	}

	private void reportAndClearUnexpectedCharacters() {
		if (unexpectedCharacters == null) {
			return;
		}
		errorReporter.accept(unexpectedCharacters);
		unexpectedCharacters = null;
	}

	private void lexToken() throws UnexpectedCharactersException, IncompleteNumberLiteralException {
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
					throw new UnexpectedCharactersException(curPosition - 1, String.valueOf(current));
				}
		}
	}

	private void singleLineComment() {
		matchWhile(c -> c != '\n');
	}

	private void multiLineComment() {
		while (!(matchChar('*') && matchChar('/'))) {
			popChar();
		}
	}

	private void number() throws IncompleteNumberLiteralException {
		matchWhile(Lexer::isAsciiDigit);
		if (matchChar('.') && matchWhile(Lexer::isAsciiDigit) == 0) {
			throw new IncompleteNumberLiteralException(startOfLexeme, currentLexeme());
		}
		emitToken(NUMBER);
	}

	private void keywordOrIdentifier() {
		matchWhile(Character::isJavaIdentifierPart);
		TokenType type = keywords.getOrDefault(currentLexeme(), IDENTIFIER);
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
