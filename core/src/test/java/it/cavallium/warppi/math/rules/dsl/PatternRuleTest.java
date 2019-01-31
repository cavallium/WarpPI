package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.functions.SumSubtraction;
import it.cavallium.warppi.math.rules.RuleType;
import it.cavallium.warppi.math.rules.dsl.patterns.*;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class PatternRuleTest {
	private final MathContext mathContext = new MathContext();

	private final Pattern x = new SubFunctionPattern("x");
	private final Pattern xPlus0 = new SumPattern(
			x,
			new NumberPattern(new BigDecimal(0))
	);

	@Test
	public void testNonMatching() throws InterruptedException, Error {
		final Function func = new Sum(
				mathContext,
				new Number(mathContext, 1),
				new Number(mathContext, 2)
		);

		final PatternRule rule = new PatternRule("TestRule", RuleType.REDUCTION, xPlus0, xPlus0);
		assertNull(func.simplify(rule));
	}

	@Test
	public void testMatching() throws InterruptedException, Error {
		final Function func = new Sum(
				mathContext,
				new Number(mathContext, 1),
				new Number(mathContext, 0)
		);

		final PatternRule identityRule = new PatternRule("Identity", RuleType.REDUCTION, xPlus0, xPlus0);
		final ObjectArrayList<Function> identityResult = func.simplify(identityRule);
		assertEquals(1, identityResult.size());
		assertEquals(func, identityResult.get(0));

		final PatternRule simplifyRule = new PatternRule("Simplify", RuleType.REDUCTION, xPlus0, x);
		final ObjectArrayList<Function> simplifyResult = func.simplify(simplifyRule);
		assertEquals(1, identityResult.size());
		assertEquals(new Number(mathContext, 1), simplifyResult.get(0));
	}

	@Test
	public void testMatchingRecursive() throws InterruptedException, Error {
		final Function func = new Sum(
				mathContext,
				new Number(mathContext, 3),
				new Sum(
						mathContext,
						new Number(mathContext, 5),
						new Number(mathContext, 0)
				)
		);

		final PatternRule identityRule = new PatternRule("Identity", RuleType.REDUCTION, xPlus0, xPlus0);
		final ObjectArrayList<Function> identityResult = func.simplify(identityRule);
		assertEquals(1, identityResult.size());
		assertEquals(func, identityResult.get(0));

		final PatternRule simplifyRule = new PatternRule("Simplify", RuleType.REDUCTION, xPlus0, x);
		final ObjectArrayList<Function> simplifyResult = func.simplify(simplifyRule);
		assertEquals(1, identityResult.size());
		final Function expected = new Sum(
				mathContext,
				new Number(mathContext, 3),
				new Number(mathContext, 5)
		);
		assertEquals(expected, simplifyResult.get(0));
	}

	@Test
	public void testMultipleReplacements() throws InterruptedException, Error {
		final Number one = new Number(mathContext, 1);
		final Number two = new Number(mathContext, 2);
		final Function func = new SumSubtraction(mathContext, one, two);

		final Pattern x = new SubFunctionPattern("x");
		final Pattern y = new SubFunctionPattern("y");
		final PatternRule rule = new PatternRule(
				"TestRule",
				RuleType.EXPANSION,
				new SumSubtractionPattern(x, y),
				new SumPattern(x, y), new SubtractionPattern(x, y)
		);

		final ObjectArrayList<Function> result = func.simplify(rule);
		final ObjectArrayList<Function> expected = ObjectArrayList.wrap(new Function[]{
				new Sum(mathContext, one, two),
				new Subtraction(mathContext, one, two)
		});
		assertEquals(expected, result);
	}

	@Test
	public void testNoReplacements() throws InterruptedException, Error {
		final Function func = new Sum(
				mathContext,
				new Number(mathContext, 1),
				new Number(mathContext, 2)
		);

		final PatternRule rule = new PatternRule(
				"TestRule",
				RuleType.REDUCTION,
				new SubFunctionPattern("x")
		);

		assertTrue(func.simplify(rule).isEmpty());
	}
}
