package it.cavallium.warppi.math.rules.dsl.frontend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
	private final List<Token> tokens = new ArrayList<>();
	private int startOfLexeme = 0;
	private int curPosition = 0;

	public Lexer(final String source) {
		this.source = source;
	}

	public List<Token> lex() {
		while (!atEnd()) {
			startOfLexeme = curPosition;
			lexToken();
		}
		tokens.add(new Token(EOF, "", source.length()));
		return tokens;
	}

	private void lexToken() {
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
			case '/': emitToken(DIVIDE); break;
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

			default:
				if (isAsciiDigit(current)) {
					number();
				} else if (Character.isJavaIdentifierStart(current)) {
					keywordOrIdentifier();
				} else if (!Character.isWhitespace(current)) {
					throw new RuntimeException("Unexpected character " + current);
				}
		}
	}

	private void number() {
		matchWhile(Lexer::isAsciiDigit);
		if (matchChar('.') && matchWhile(Lexer::isAsciiDigit) == 0) {
			throw new RuntimeException("Expected digits after decimal separator");
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
