package it.cavallium.warppi.math.rules.dsl.frontend;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static it.cavallium.warppi.math.rules.dsl.frontend.TokenType.*;
import static org.junit.Assert.*;

public class LexerTest {
	@Test
	public void validRule() {
		final Lexer lexer = new Lexer(
				"reduction TestRule_123:\n" +
						"  x + y * z = -(a_123 +- 3 / 2.2) -> [\n" +
						"    x^a_123 = cos(pi) - log(e, e),\n" +
						"    undefined,\n" +
						"]\n"
		);
		final List<Token> expected = Arrays.asList(
				new Token(REDUCTION, "reduction", 0),
				new Token(IDENTIFIER, "TestRule_123", 10),
				new Token(COLON, ":", 22),
				new Token(IDENTIFIER, "x", 26),
				new Token(PLUS, "+", 28),
				new Token(IDENTIFIER, "y", 30),
				new Token(TIMES, "*", 32),
				new Token(IDENTIFIER, "z", 34),
				new Token(EQUALS, "=", 36),
				new Token(MINUS, "-", 38),
				new Token(LEFT_PAREN, "(", 39),
				new Token(IDENTIFIER, "a_123", 40),
				new Token(PLUS_MINUS, "+-", 46),
				new Token(NUMBER, "3", 49),
				new Token(DIVIDE, "/", 51),
				new Token(NUMBER, "2.2", 53),
				new Token(RIGHT_PAREN, ")", 56),
				new Token(ARROW, "->", 58),
				new Token(LEFT_BRACKET, "[", 61),
				new Token(IDENTIFIER, "x", 67),
				new Token(POWER, "^", 68),
				new Token(IDENTIFIER, "a_123", 69),
				new Token(EQUALS, "=", 75),
				new Token(COS, "cos", 77),
				new Token(LEFT_PAREN, "(", 80),
				new Token(PI, "pi", 81),
				new Token(RIGHT_PAREN, ")", 83),
				new Token(MINUS, "-", 85),
				new Token(LOG, "log", 87),
				new Token(LEFT_PAREN, "(", 90),
				new Token(E, "e", 91),
				new Token(COMMA, ",", 92),
				new Token(E, "e", 94),
				new Token(RIGHT_PAREN, ")", 95),
				new Token(COMMA, ",", 96),
				new Token(UNDEFINED, "undefined", 102),
				new Token(COMMA, ",", 111),
				new Token(RIGHT_BRACKET, "]", 113),
				new Token(EOF, "", 115)
		);
		assertEquals(expected, lexer.lex());
	}

	@Test(expected = RuntimeException.class)
	public void incompleteNumberOtherChar() {
		final Lexer lexer = new Lexer("2. 5");
		lexer.lex();
	}

	@Test(expected = RuntimeException.class)
	public void incompleteNumberEof() {
		final Lexer lexer = new Lexer("2.");
		lexer.lex();
	}

	@Test(expected = RuntimeException.class)
	public void meaninglessCharacter() {
		final Lexer lexer = new Lexer("@");
		lexer.lex();
	}
}
