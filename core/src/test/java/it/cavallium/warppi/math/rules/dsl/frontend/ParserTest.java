package it.cavallium.warppi.math.rules.dsl.frontend;

import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.math.rules.dsl.DslError;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.PatternRule;
import it.cavallium.warppi.math.rules.dsl.patterns.*;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static it.cavallium.warppi.math.rules.dsl.frontend.TokenType.*;
import static org.junit.Assert.*;

public class ParserTest {
	private final List<DslError> errors = new ArrayList<>();

	@Before
	public void setUp() {
		errors.clear();
	}

	@Test
	public void noRules() {
		final List<Token> tokens = Collections.singletonList(
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);
		assertEquals(Collections.emptyList(), parser.parse());
	}

	@Test
	public void validRuleMultipleReplacements() {
		final List<Token> tokens = Arrays.asList(
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
				new Token(UNDEFINED, "undefined", 113),
				new Token(COMMA, ",", 122),
				new Token(RIGHT_BRACKET, "]", 138),
				new Token(EOF, "", 140)
		);
		final Parser parser = new Parser(tokens, errors::add);

		// x + y * z = -(a_123 +- 3 / 2.2)
		final Pattern target = new EquationPattern(
				new SumPattern(
						new SubFunctionPattern("x"),
						new MultiplicationPattern(
								new SubFunctionPattern("y"),
								new SubFunctionPattern("z")
						)
				),
				new NegativePattern(new SumSubtractionPattern(
						new SubFunctionPattern("a_123"),
						new DivisionPattern(
								new NumberPattern(new BigDecimal(3)),
								new NumberPattern(new BigDecimal("2.2"))
						)
				))
		);
		// x^a_123 = cos(pi) - log(e, e)
		final Pattern replacement1 = new EquationPattern(
				new PowerPattern(
						new SubFunctionPattern("x"),
						new SubFunctionPattern("a_123")
				),
				new SubtractionPattern(
						new CosinePattern(new ConstantPattern(MathematicalSymbols.PI)),
						new LogarithmPattern(
								new ConstantPattern(MathematicalSymbols.EULER_NUMBER),
								new ConstantPattern(MathematicalSymbols.EULER_NUMBER)
						)
				)
		);
		final Pattern replacement2 = new UndefinedPattern();
		final List<PatternRule> expected = Collections.singletonList(new PatternRule(
				"TestRule_123",
				RuleType.REDUCTION,
				target,
				replacement1,
				replacement2
		));

		assertEquals(expected, parser.parse());
	}

	@Test
	public void validRuleNoReplacements() {
		final List<Token> tokens = Arrays.asList(
				new Token(EXISTENCE, "existence", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(PLUS, "+", 0),
				new Token(IDENTIFIER, "y", 0),
				new Token(ARROW, "->", 0),
				new Token(LEFT_BRACKET, "[", 0),
				new Token(RIGHT_BRACKET, "]", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		final List<PatternRule> expected = Collections.singletonList(new PatternRule(
				"test",
				RuleType.EXISTENCE,
				new SumPattern(
						new SubFunctionPattern("x"),
						new SubFunctionPattern("y")
				)
		));
		assertEquals(expected, parser.parse());
	}

	@Test
	public void validRuleOneReplacement() {
		final List<Token> tokens = Arrays.asList(
				new Token(REDUCTION, "reduction", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(MINUS, "-", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(TIMES, "*", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(ARROW, "->", 0),
				new Token(MINUS, "-", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(POWER, "^", 0),
				new Token(NUMBER, "2", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		final List<PatternRule> expected = Collections.singletonList(new PatternRule(
				"test",
				RuleType.REDUCTION,
				new MultiplicationPattern(
						new NegativePattern(new SubFunctionPattern("x")),
						new SubFunctionPattern("x")
				),
				new NegativePattern(new PowerPattern(
						new SubFunctionPattern("x"),
						new NumberPattern(new BigDecimal(2))
				))
		));
		assertEquals(expected, parser.parse());
	}

	@Test
	public void validRuleOneReplacementBrackets() {
		final List<Token> tokens = Arrays.asList(
				new Token(REDUCTION, "reduction", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(TIMES, "*", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(ARROW, "->", 0),
				new Token(LEFT_BRACKET, "[", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(POWER, "^", 0),
				new Token(NUMBER, "2", 0),
				new Token(RIGHT_BRACKET, "]", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		final List<PatternRule> expected = Collections.singletonList(new PatternRule(
				"test",
				RuleType.REDUCTION,
				new MultiplicationPattern(
						new SubFunctionPattern("x"),
						new SubFunctionPattern("x")
				),
				new PowerPattern(
						new SubFunctionPattern("x"),
						new NumberPattern(new BigDecimal(2))
				)
		));
		assertEquals(expected, parser.parse());
	}

	@Test
	public void multipleValidRules() {
		final List<Token> tokens = Arrays.asList(
				new Token(REDUCTION, "reduction", 0),
				new Token(IDENTIFIER, "test1", 0),
				new Token(COLON, ":", 0),
				new Token(PLUS, "+", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(ARROW, "->", 0),
				new Token(IDENTIFIER, "x", 0),

				new Token(EXPANSION, "expansion", 0),
				new Token(IDENTIFIER, "test2", 0),
				new Token(COLON, ":", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(ARROW, "->", 0),
				new Token(MINUS, "-", 0),
				new Token(MINUS, "-", 0),
				new Token(IDENTIFIER, "x", 0),

				new Token(CALCULATION, "calculation", 0),
				new Token(IDENTIFIER, "test3", 0),
				new Token(COLON, ":", 0),
				new Token(NUMBER, "1", 0),
				new Token(PLUS, "+", 0),
				new Token(NUMBER, "1", 0),
				new Token(ARROW, "->", 0),
				new Token(NUMBER, "2", 0),

				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		final List<PatternRule> expected = Arrays.asList(
				new PatternRule(
						"test1",
						RuleType.REDUCTION,
						new SubFunctionPattern("x"),
						new SubFunctionPattern("x")
				),
				new PatternRule(
						"test2",
						RuleType.EXPANSION,
						new SubFunctionPattern("x"),
						new NegativePattern(new NegativePattern(new SubFunctionPattern("x")))
				),
				new PatternRule(
						"test3",
						RuleType.CALCULATION,
						new SumPattern(
								new NumberPattern(new BigDecimal(1)),
								new NumberPattern(new BigDecimal(1))
						),
						new NumberPattern(new BigDecimal(2))
				)
		);
		assertEquals(expected, parser.parse());
	}

	// The EOF token is inserted by the lexer, therefore it can only be missing
	// in case of programming errors, and not directly because of user input.
	@Test(expected = RuntimeException.class)
	public void missingEof() {
		final List<Token> tokens = Arrays.asList(
				new Token(EXISTENCE, "existence", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(PLUS, "+", 0),
				new Token(IDENTIFIER, "y", 0),
				new Token(ARROW, "->", 0),
				new Token(LEFT_BRACKET, "[", 0),
				new Token(RIGHT_BRACKET, "]", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);
		parser.parse();
	}

	@Test
	public void incompleteRule() {
		final List<Token> tokens = Arrays.asList(
				new Token(EXISTENCE, "existence", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(PLUS, "+", 0),
				new Token(IDENTIFIER, "y", 0),
				new Token(ARROW, "->", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		assertTrue(parser.parse().isEmpty());

		final List<DslError> expectedErrors = Collections.singletonList(new UnexpectedToken(
				new Token(EOF, "", 0)
		));
		assertEquals(expectedErrors, errors);
	}

	@Test
	public void missingRuleType() {
		final List<Token> tokens = Arrays.asList(
				new Token(IDENTIFIER, "test", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		assertTrue(parser.parse().isEmpty());

		final List<DslError> expectedErrors = Collections.singletonList(new UnexpectedToken(
				new Token(IDENTIFIER, "test", 0),
				REDUCTION, EXPANSION, CALCULATION, EXISTENCE
		));
		assertEquals(expectedErrors, errors);
	}

	@Test
	public void unexpectedTokenPrimary() {
		final List<Token> tokens = Arrays.asList(
				new Token(EXISTENCE, "existence", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(PLUS, "+", 0),
				new Token(CALCULATION, "calculation", 0),
				new Token(ARROW, "->", 0),
				new Token(LEFT_BRACKET, "[", 0),
				new Token(RIGHT_BRACKET, "]", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		assertTrue(parser.parse().isEmpty());

		final List<DslError> expectedErrors = Collections.singletonList(new UnexpectedToken(
				new Token(CALCULATION, "calculation", 0)
		));
		assertEquals(expectedErrors, errors);
	}

	@Test
	public void missingRuleName() {
		final List<Token> tokens = Arrays.asList(
				new Token(REDUCTION, "reduction", 0),
				new Token(COLON, ":", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		assertTrue(parser.parse().isEmpty());

		final List<DslError> expectedErrors = Collections.singletonList(new UnexpectedToken(
				new Token(COLON, ":", 0),
				IDENTIFIER
		));
		assertEquals(expectedErrors, errors);
	}

	@Test
	public void missingColon() {
		final List<Token> tokens = Arrays.asList(
				new Token(REDUCTION, "reduction", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(ARROW, "->", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		assertTrue(parser.parse().isEmpty());

		final List<DslError> expectedErrors = Collections.singletonList(new UnexpectedToken(
				new Token(IDENTIFIER, "x", 0),
				COLON
		));
		assertEquals(expectedErrors, errors);
	}

	@Test
	public void missingArrow() {
		final List<Token> tokens = Arrays.asList(
				new Token(REDUCTION, "reduction", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(IDENTIFIER, "y", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		assertTrue(parser.parse().isEmpty());

		final List<DslError> expectedErrors = Collections.singletonList(new UnexpectedToken(
				new Token(IDENTIFIER, "y", 0),
				ARROW
		));
		assertEquals(expectedErrors, errors);
	}

	@Test
	public void missingRightBracket() {
		final List<Token> tokens = Arrays.asList(
				new Token(REDUCTION, "reduction", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(ARROW, "->", 0),
				new Token(LEFT_BRACKET, "[", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		assertTrue(parser.parse().isEmpty());

		final List<DslError> expectedErrors = Collections.singletonList(new UnexpectedToken(
				new Token(EOF, "", 0),
				RIGHT_BRACKET
		));
		assertEquals(expectedErrors, errors);
	}

	@Test
	public void missingOneArgFunctionLeftParen() {
		final List<Token> tokens = Arrays.asList(
				new Token(EXISTENCE, "existence", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(SIN, "sin", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(RIGHT_PAREN, ")", 0),
				new Token(ARROW, "->", 0),
				new Token(LEFT_BRACKET, "[", 0),
				new Token(RIGHT_BRACKET, "]", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		assertTrue(parser.parse().isEmpty());

		final List<DslError> expectedErrors = Collections.singletonList(new UnexpectedToken(
				new Token(IDENTIFIER, "x", 0),
				LEFT_PAREN
		));
		assertEquals(expectedErrors, errors);
	}

	@Test
	public void missingOneArgFunctionRightParen() {
		final List<Token> tokens = Arrays.asList(
				new Token(EXISTENCE, "existence", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(SIN, "sin", 0),
				new Token(LEFT_PAREN, "(", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(ARROW, "->", 0),
				new Token(LEFT_BRACKET, "[", 0),
				new Token(RIGHT_BRACKET, "]", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		assertTrue(parser.parse().isEmpty());

		final List<DslError> expectedErrors = Collections.singletonList(new UnexpectedToken(
				new Token(ARROW, "->", 0),
				RIGHT_PAREN
		));
		assertEquals(expectedErrors, errors);
	}

	@Test
	public void missingTwoArgFunctionLeftParen() {
		final List<Token> tokens = Arrays.asList(
				new Token(EXISTENCE, "existence", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(LOG, "log", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(COMMA, ",", 0),
				new Token(IDENTIFIER, "y", 0),
				new Token(RIGHT_PAREN, ")", 0),
				new Token(ARROW, "->", 0),
				new Token(LEFT_BRACKET, "[", 0),
				new Token(RIGHT_BRACKET, "]", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		assertTrue(parser.parse().isEmpty());

		final List<DslError> expectedErrors = Collections.singletonList(new UnexpectedToken(
				new Token(IDENTIFIER, "x", 0),
				LEFT_PAREN
		));
		assertEquals(expectedErrors, errors);
	}

	@Test
	public void missingTwoArgFunctionComma() {
		final List<Token> tokens = Arrays.asList(
				new Token(EXISTENCE, "existence", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(LOG, "log", 0),
				new Token(LEFT_PAREN, "(", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(IDENTIFIER, "y", 0),
				new Token(RIGHT_PAREN, ")", 0),
				new Token(ARROW, "->", 0),
				new Token(LEFT_BRACKET, "[", 0),
				new Token(RIGHT_BRACKET, "]", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		assertTrue(parser.parse().isEmpty());

		final List<DslError> expectedErrors = Collections.singletonList(new UnexpectedToken(
				new Token(IDENTIFIER, "y", 0),
				COMMA
		));
		assertEquals(expectedErrors, errors);
	}

	@Test
	public void missingTwoArgFunctionRightParen() {
		final List<Token> tokens = Arrays.asList(
				new Token(EXISTENCE, "existence", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(LOG, "log", 0),
				new Token(LEFT_PAREN, "(", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(COMMA, ",", 0),
				new Token(IDENTIFIER, "y", 0),
				new Token(ARROW, "->", 0),
				new Token(LEFT_BRACKET, "[", 0),
				new Token(RIGHT_BRACKET, "]", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		assertTrue(parser.parse().isEmpty());

		final List<DslError> expectedErrors = Collections.singletonList(new UnexpectedToken(
				new Token(ARROW, "->", 0),
				RIGHT_PAREN
		));
		assertEquals(expectedErrors, errors);
	}

	@Test
	public void missingExpressionRightParen() {
		final List<Token> tokens = Arrays.asList(
				new Token(EXISTENCE, "existence", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(LEFT_PAREN, "(", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(ARROW, "->", 0),
				new Token(LEFT_BRACKET, "[", 0),
				new Token(RIGHT_BRACKET, "]", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		assertTrue(parser.parse().isEmpty());

		final List<DslError> expectedErrors = Collections.singletonList(new UnexpectedToken(
				new Token(ARROW, "->", 0),
				RIGHT_PAREN
		));
		assertEquals(expectedErrors, errors);
	}

	@Test
	public void recoveryToNextRule() {
		final List<Token> tokens = Arrays.asList(
				new Token(REDUCTION, "reduction", 0),
				new Token(IDENTIFIER, "test1", 0),
				new Token(COLON, ":", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(TIMES, "+", 0),
				new Token(ARROW, "->", 0),
				new Token(IDENTIFIER, "x", 0),

				new Token(EXPANSION, "expansion", 0),
				new Token(IDENTIFIER, "test2", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(ARROW, "->", 0),
				new Token(MINUS, "-", 0),
				new Token(MINUS, "-", 0),
				new Token(IDENTIFIER, "x", 0),

				new Token(CALCULATION, "calculation", 0),
				new Token(IDENTIFIER, "test3", 0),
				new Token(COLON, ":", 0),
				new Token(NUMBER, "1", 0),
				new Token(PLUS, "+", 0),
				new Token(NUMBER, "1", 0),
				new Token(ARROW, "->", 0),
				new Token(NUMBER, "2", 0),

				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		final List<PatternRule> expectedRules = Collections.singletonList(new PatternRule(
				"test3",
				RuleType.CALCULATION,
				new SumPattern(
						new NumberPattern(new BigDecimal(1)),
						new NumberPattern(new BigDecimal(1))
				),
				new NumberPattern(new BigDecimal(2))
		));
		assertEquals(expectedRules, parser.parse());

		final List<DslError> expectedErrors = Arrays.asList(
				new UnexpectedToken(new Token(ARROW, "->", 0)),
				new UnexpectedToken(new Token(IDENTIFIER, "x", 0), COLON)
		);
		assertEquals(expectedErrors, errors);
	}
}
