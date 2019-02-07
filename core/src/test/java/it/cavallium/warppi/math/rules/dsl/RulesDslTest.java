package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.math.rules.dsl.frontend.IncompleteNumberLiteral;
import it.cavallium.warppi.math.rules.dsl.frontend.Token;
import it.cavallium.warppi.math.rules.dsl.frontend.TokenType;
import it.cavallium.warppi.math.rules.dsl.frontend.UnexpectedToken;
import it.cavallium.warppi.math.rules.dsl.patterns.NegativePattern;
import it.cavallium.warppi.math.rules.dsl.patterns.NumberPattern;
import it.cavallium.warppi.math.rules.dsl.patterns.SubFunctionPattern;
import it.cavallium.warppi.math.rules.dsl.patterns.SumPattern;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class RulesDslTest {
	@org.junit.Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void validRules() throws DslAggregateException {
		final List<Rule> rules = RulesDsl.makeRules(
				"reduction test1: x -> x\n" +
				"expansion test2:\n" +
				"  x -> --x\n" +
				"calculation test3:\n" +
				"  1 + 1 -> 2\n"
		);

		final List<Rule> expected = Arrays.asList(
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

		assertEquals(expected, rules);
	}

	@Test
	public void lexerError() throws DslAggregateException {
		thrown.expect(DslAggregateException.class);
		thrown.expect(equalTo(
				new DslAggregateException(Collections.singletonList(
						new IncompleteNumberLiteral(16, "2.")
				))
		));

		RulesDsl.makeRules("reduction test: 2. 5 -> 1");
	}

	@Test
	public void parserError() throws DslAggregateException {
		thrown.expect(DslAggregateException.class);
		thrown.expect(equalTo(
				new DslAggregateException(Collections.singletonList(
						new UnexpectedToken(new Token(TokenType.EOF, "", 24))
				))
		));

		RulesDsl.makeRules("existence test: x + y ->");
	}

	@Test
	public void undefinedSubFunction() throws DslAggregateException {
		thrown.expect(DslAggregateException.class);
		thrown.expect(equalTo(
				new DslAggregateException(Collections.singletonList(
						new UndefinedSubFunction(new Token(TokenType.IDENTIFIER, "y", 25))
				))
		));

		RulesDsl.makeRules("expansion test: x -> x + y");
	}
}
