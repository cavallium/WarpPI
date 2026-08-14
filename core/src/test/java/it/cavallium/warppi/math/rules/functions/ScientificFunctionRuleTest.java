package it.cavallium.warppi.math.rules.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Logarithm;
import it.cavallium.warppi.math.functions.RandomInteger;
import it.cavallium.warppi.math.functions.ScientificFunction;
import it.cavallium.warppi.math.functions.ScientificFunctionKind;
import it.cavallium.warppi.math.functions.PrimeFactorization;
import it.cavallium.warppi.math.functions.Sexagesimal;
import it.cavallium.warppi.math.functions.trigonometry.ArcCosine;
import it.cavallium.warppi.math.functions.trigonometry.ArcSine;
import it.cavallium.warppi.math.functions.trigonometry.ArcTangent;
import it.cavallium.warppi.math.functions.trigonometry.Cosine;
import it.cavallium.warppi.math.functions.trigonometry.Sine;
import it.cavallium.warppi.math.functions.trigonometry.Tangent;
import it.cavallium.warppi.util.Error;

class ScientificFunctionRuleTest {
	private final MathContext context = new MathContext();

	@Test
	void calculatesAbsoluteFactorialAndPercent() throws Error {
		final ScientificFunctionRule rule = new ScientificFunctionRule();
		assertEquals(new Number(context, 12), only(rule.execute(function(
				ScientificFunctionKind.ABSOLUTE, new Number(context, -12)))));
		assertEquals(new Number(context, 120), only(rule.execute(function(
				ScientificFunctionKind.FACTORIAL, new Number(context, 5)))));
		assertEquals(new Division(context, new Number(context, 25), new Number(context, 100)),
				only(rule.execute(function(ScientificFunctionKind.PERCENT, new Number(context, 25)))));
	}

	@Test
	void factorizationRemainsAVisiblePrimeProduct() throws Error {
		final Function result = only(new ScientificFunctionRule().execute(function(
				ScientificFunctionKind.FACTORIZE, new Number(context, 12))));
		final PrimeFactorization factorization = (PrimeFactorization) result;
		assertEquals(List.of(new Number(context, 2), new Number(context, 2), new Number(context, 3)),
				List.of(factorization.getParameters()));
		final PrimeFactorization prime = (PrimeFactorization) only(new ScientificFunctionRule().execute(
				function(ScientificFunctionKind.FACTORIZE, new Number(context, 13))));
		assertEquals(List.of(new Number(context, 13)), List.of(prime.getParameters()));
	}

	@Test
	void convertsDecimalDegreesToSexagesimal() throws Error {
		final Function result = only(new ScientificFunctionRule().execute(function(
				ScientificFunctionKind.DEGREE_MINUTE_SECOND, new Number(context, "2.5"))));
		assertEquals(new Sexagesimal(context, false, BigInteger.valueOf(2),
				BigInteger.valueOf(30), java.math.BigDecimal.ZERO), result);
		assertEquals("2°30′0″", result.toString());
	}

	@Test
	void randomIntegerIsAlwaysInsideInclusiveBounds() throws Error {
		final RandomIntegerRule rule = new RandomIntegerRule();
		for (int iteration = 0; iteration < 64; iteration++) {
			final Number result = (Number) only(rule.execute(new RandomInteger(context,
					new Number(context, -2), new Number(context, 2))));
			final BigInteger value = result.getTerm().toBigIntegerExact();
			assertTrue(value.compareTo(BigInteger.valueOf(-2)) >= 0);
			assertTrue(value.compareTo(BigInteger.valueOf(2)) <= 0);
		}
	}

	@Test
	void evaluatesACommonLogarithm() throws Error {
		assertEquals(new Number(context, 3), only(new LogarithmRule().execute(
				new Logarithm(context, new Number(context, 10), new Number(context, 1000)))));
	}

	@Test
	void evaluatesAllSixTrigonometricFunctionsInDegreeMode() throws Error {
		final TrigonometryRule rule = new TrigonometryRule();
		assertApproximately("0.5", only(rule.execute(new Sine(context, number("30")))));
		assertApproximately("0.5", only(rule.execute(new Cosine(context, number("60")))));
		assertApproximately("1", only(rule.execute(new Tangent(context, number("45")))));
		assertApproximately("30", only(rule.execute(new ArcSine(context, number("0.5")))));
		assertApproximately("60", only(rule.execute(new ArcCosine(context, number("0.5")))));
		assertApproximately("45", only(rule.execute(new ArcTangent(context, number("1")))));
	}


	private ScientificFunction function(final ScientificFunctionKind kind, final Function parameter) {
		return new ScientificFunction(context, kind, parameter);
	}

	private Number number(final String value) throws Error {
		return new Number(context, value);
	}

	private static void assertApproximately(final String expected, final Function result) throws Error {
		final java.math.BigDecimal difference = ((Number) result).getTerm()
				.subtract(new java.math.BigDecimal(expected)).abs();
		assertTrue(difference.compareTo(new java.math.BigDecimal("0.000001")) < 0,
				() -> "Expected " + expected + " but got " + result);
	}

	private static Function only(final List<Function> functions) {
		return functions.getFirst();
	}
}
