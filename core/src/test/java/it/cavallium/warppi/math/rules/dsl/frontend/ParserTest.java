package it.cavallium.warppi.math.rules.dsl.frontend;

import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.math.rules.dsl.DslError;
import it.cavallium.warppi.math.rules.dsl.Pattern;
import it.cavallium.warppi.math.rules.dsl.PatternRule;
import it.cavallium.warppi.math.rules.dsl.patterns.*;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.cavallium.warppi.math.rules.dsl.frontend.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
	private final List<DslError> errors = new ArrayList<>();

	@BeforeEach
	void setUp() {
		errors.clear();
	}

	@Test
	void noRules() {
		final List<Token> tokens = Collections.singletonList(
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);
		assertEquals(Collections.emptyList(), parser.parse());
	}

	@Test
	void multipleParseCalls() {
		final List<Token> tokens = Collections.singletonList(
			new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);
		parser.parse();
		assertThrows(IllegalStateException.class, parser::parse);
	}

	@Test
	void validRuleMultipleReplacements() {
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
	void validRuleNoReplacements() {
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
	void validRuleOneReplacement() {
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
	void validRuleOneReplacementBrackets() {
		final List<Token> tokens = Arrays.asList(
				new Token(REDUCTION, "reduction", 0),
				new Token(IDENTIFIER, "test", 0),
				new Token(COLON, ":", 0),
				new Token(NUMBER, "1", 0),
				new Token(DIVIDE, "/", 0),
				new Token(LEFT_PAREN, "(", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(TIMES, "*", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(RIGHT_PAREN, ")", 0),
				new Token(ARROW, "->", 0),
				new Token(LEFT_BRACKET, "[", 0),
				new Token(IDENTIFIER, "x", 0),
				new Token(POWER, "^", 0),
				new Token(MINUS, "-", 0),
				new Token(NUMBER, "2", 0),
				new Token(RIGHT_BRACKET, "]", 0),
				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);

		final List<PatternRule> expected = Collections.singletonList(new PatternRule(
				"test",
				RuleType.REDUCTION,
				new DivisionPattern(
						new NumberPattern(new BigDecimal(1)),
						new MultiplicationPattern(
								new SubFunctionPattern("x"),
								new SubFunctionPattern("x")
						)
				),
				new PowerPattern(
						new SubFunctionPattern("x"),
						new NegativePattern(
								new NumberPattern(new BigDecimal(2))
						)
				)
		));
		assertEquals(expected, parser.parse());
	}

	@Test
	void multipleValidRules() {
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

	@Test
	void subFunctionIdentifiers() {
		final List<ReferenceEqualityToken> rule0x = new ArrayList<>();
		final List<ReferenceEqualityToken> rule1x = new ArrayList<>();
		final List<ReferenceEqualityToken> rule1y = new ArrayList<>();
		final List<ReferenceEqualityToken> rule2x = new ArrayList<>();
		final List<ReferenceEqualityToken> rule3x = new ArrayList<>();

		final List<Token> tokens = Arrays.asList(
				new Token(REDUCTION, "reduction", 0),
				new Token(IDENTIFIER, "test1", 0),
				new Token(COLON, ":", 0),
				new Token(PLUS, "+", 0),
				addIdentifierToken(rule0x, "x"),
				new Token(ARROW, "->", 0),
				addIdentifierToken(rule0x, "x"),

				new Token(EXPANSION, "expansion", 0),
				new Token(IDENTIFIER, "test2", 0),
				new Token(COLON, ":", 0),
				addIdentifierToken(rule1x, "x"),
				new Token(POWER, "^", 0),
				new Token(MINUS, "-", 0),
				addIdentifierToken(rule1y, "y"),
				new Token(ARROW, "->", 0),
				new Token(NUMBER, "1", 0),
				new Token(DIVIDE, "/", 0),
				addIdentifierToken(rule1x, "x"),
				new Token(POWER, "^", 0),
				addIdentifierToken(rule1y, "y"),

				// Rule with the same name (and type)
				new Token(CALCULATION, "expansion", 0),
				new Token(IDENTIFIER, "test2", 0),
				new Token(COLON, ":", 0),
				addIdentifierToken(rule2x, "x"),
				new Token(ARROW, "->", 0),
				addIdentifierToken(rule2x, "x"),
				new Token(PLUS, "+", 0),
				new Token(NUMBER, "0", 0),

				// Identical rule
				new Token(CALCULATION, "expansion", 0),
				new Token(IDENTIFIER, "test2", 0),
				new Token(COLON, ":", 0),
				addIdentifierToken(rule3x, "x"),
				new Token(ARROW, "->", 0),
				addIdentifierToken(rule3x, "x"),
				new Token(PLUS, "+", 0),
				new Token(NUMBER, "0", 0),

				new Token(EOF, "", 0)
		);
		final Parser parser = new Parser(tokens, errors::add);
		final List<PatternRule> rules = parser.parse();

		assertEquals(rule0x, getSubFunctionIdentifiers(parser, rules.get(0), "x"));
		assertEquals(rule1x, getSubFunctionIdentifiers(parser, rules.get(1), "x"));
		assertEquals(rule1y, getSubFunctionIdentifiers(parser, rules.get(1), "y"));
		assertEquals(rule2x, getSubFunctionIdentifiers(parser, rules.get(2), "x"));
		assertEquals(rule3x, getSubFunctionIdentifiers(parser, rules.get(3), "x"));
	}

	private static Token addIdentifierToken(final List<ReferenceEqualityToken> list, final String identifier) {
		final Token token = new Token(IDENTIFIER, identifier, 0);
		list.add(new ReferenceEqualityToken(token));
		return token;
	}

	private static List<ReferenceEqualityToken> getSubFunctionIdentifiers(
		final Parser parser,
		final PatternRule rule,
		final String subFunctionName
	) {
		final SubFunctionPattern exampleSubFunction = new SubFunctionPattern(subFunctionName);
		final Stream<SubFunctionPattern> allSubFunctions = Stream.concat(
			rule.getTarget().getSubFunctions(),
			rule.getReplacements().stream().flatMap(Pattern::getSubFunctions)
		);
		return allSubFunctions
			.filter(subFunc -> subFunc.equals(exampleSubFunction)) // Match the name without having access to it directly
			.map(subFunc -> new ReferenceEqualityToken(
				parser.getSubFunctionIdentifier(subFunc)
			))
			.collect(Collectors.toList());
	}

	private static class ReferenceEqualityToken {
		private final Token token;

		ReferenceEqualityToken(final Token token) {
			this.token = token;
		}

		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof ReferenceEqualityToken)) {
				return false;
			}
			return this.token == ((ReferenceEqualityToken) o).token;
		}

		@Override
		public String toString() {
			return "ReferenceEqualityToken{" + ObjectUtils.identityToString(token) + '}';
		}
	}

	// The EOF token is inserted by the lexer, therefore it can only be missing
	// in case of programming errors, and not directly because of user input.
	@Test
	void missingEof() {
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
		assertThrows(RuntimeException.class, parser::parse);
	}

	@Test
	void incompleteRule() {
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
	void missingRuleType() {
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
	void unexpectedTokenPrimary() {
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
	void missingRuleName() {
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
	void missingColon() {
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
	void missingArrow() {
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
	void missingRightBracket() {
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
	void missingOneArgFunctionLeftParen() {
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
	void missingOneArgFunctionRightParen() {
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
	void missingTwoArgFunctionLeftParen() {
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
	void missingTwoArgFunctionComma() {
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
	void missingTwoArgFunctionRightParen() {
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
	void missingExpressionRightParen() {
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
	void recoveryToNextRule() {
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
