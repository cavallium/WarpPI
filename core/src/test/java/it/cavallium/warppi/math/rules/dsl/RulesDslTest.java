package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.math.rules.dsl.patterns.NegativePattern;
import it.cavallium.warppi.math.rules.dsl.patterns.NumberPattern;
import it.cavallium.warppi.math.rules.dsl.patterns.SubFunctionPattern;
import it.cavallium.warppi.math.rules.dsl.patterns.SumPattern;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class RulesDslTest {
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

	@Test(expected = DslAggregateException.class)
	public void lexerError() throws DslAggregateException {
		RulesDsl.makeRules("reduction test: 2. 5 -> 1");
	}

	@Test(expected = DslAggregateException.class)
	public void parserError() throws DslAggregateException {
		RulesDsl.makeRules("existence test: x + y ->");
	}

	@Test(expected = DslAggregateException.class)
	public void undefinedSubFunction() throws DslAggregateException {
		RulesDsl.makeRules("expansion test: x -> x + y");
	}
}
