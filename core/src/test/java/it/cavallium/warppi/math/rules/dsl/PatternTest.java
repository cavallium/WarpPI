package it.cavallium.warppi.math.rules.dsl;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.functions.*;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.equations.Equation;
import it.cavallium.warppi.math.functions.equations.EquationsSystem;
import it.cavallium.warppi.math.functions.trigonometry.*;
import it.cavallium.warppi.math.rules.dsl.patterns.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

public class PatternTest {
	private final MathContext mathContext = new MathContext();

	@Test
	public void subFunctionPattern() {
		final Pattern pattern = new SubFunctionPattern("x");

		final Function func = new Sum(
				mathContext,
				new Number(mathContext, 1),
				new Number(mathContext, 2)
		);

		final Optional<Map<String, Function>> subFunctions = pattern.match(func);
		assertTrue(subFunctions.isPresent());

		assertEquals(func, pattern.replace(mathContext, subFunctions.get()));
	}

	@Test(expected = UndefinedSubFunctionException.class) // TODO assert exception.getSubFunctionName().equals("x")
	public void undefinedSubFunction() {
		final Pattern pattern = new SubFunctionPattern("x");
		final Map<String, Function> subFunctions = Collections.singletonMap("y", new Number(mathContext, 1));

		pattern.replace(mathContext, subFunctions);
	}

	@Test
	public void sumPattern() {
		final Pattern pattern = new SumPattern(
				new SubFunctionPattern("x"),
				new SubFunctionPattern("y")
		);

		final Function shouldNotMatch = new Subtraction(
				mathContext,
				new Number(mathContext, 1),
				new Number(mathContext, 2)
		);
		assertFalse(pattern.match(shouldNotMatch).isPresent());

		final Function shouldMatch = new Sum(
				mathContext,
				new Number(mathContext, 1),
				new Number(mathContext, 2)
		);
		final Optional<Map<String, Function>> subFunctions = pattern.match(shouldMatch);
		assertTrue(subFunctions.isPresent());
		assertEquals(shouldMatch, pattern.replace(mathContext, subFunctions.get()));
	}

	@Test
	public void repeatedSubFunction() {
		final Pattern pattern = new SumPattern(
				new SubFunctionPattern("x"),
				new SubFunctionPattern("x")
		);

		final Function shouldMatch = new Sum(
				mathContext,
				new Number(mathContext, 1),
				new Number(mathContext, 1)
		);
		final Optional<Map<String, Function>> subFunctions = pattern.match(shouldMatch);
		assertTrue(subFunctions.isPresent());
		assertEquals(shouldMatch, pattern.replace(mathContext, subFunctions.get()));

		final Function shouldNotMatch = new Sum(
				mathContext,
				new Number(mathContext, 1),
				new Number(mathContext, 2)
		);
		assertFalse(pattern.match(shouldNotMatch).isPresent());
	}

	@Test
	public void numberPattern() {
		final Pattern pattern = new NumberPattern(BigDecimal.valueOf(Math.PI));

		final Function shouldNotMatch = new Number(mathContext, 2);
		assertFalse(pattern.match(shouldNotMatch).isPresent());

		final Function shouldMatch = new Number(mathContext, Math.PI);
		final Optional<Map<String, Function>> subFunctions = pattern.match(shouldMatch);
		assertTrue(subFunctions.isPresent());
		assertEquals(shouldMatch, pattern.replace(mathContext, subFunctions.get()));
	}

	@Test
	public void negativePattern() {
		final Pattern pattern = new NegativePattern(
				new SubFunctionPattern("x")
		);

		final Function shouldNotMatch = new Number(mathContext, 1);
		assertFalse(pattern.match(shouldNotMatch).isPresent());

		final Function shouldMatch = new Negative(
				mathContext,
				new Variable(mathContext, 'x', Variable.V_TYPE.VARIABLE)
		);
		final Optional<Map<String, Function>> subFunctions = pattern.match(shouldMatch);
		assertTrue(subFunctions.isPresent());
		assertEquals(shouldMatch, pattern.replace(mathContext, subFunctions.get()));
	}

	@Test
	public void negativePatternForNumber() {
		final Pattern pattern = new NegativePattern(
				new NumberPattern(new BigDecimal(1))
		);

		final Function shouldNotMatch = new Number(mathContext, 1);
		assertFalse(pattern.match(shouldNotMatch).isPresent());

		final Function shouldMatch = new Number(mathContext, -1);
		final Optional<Map<String, Function>> subFunctions = pattern.match(shouldMatch);
		assertTrue(subFunctions.isPresent());
		assertEquals(shouldMatch, pattern.replace(mathContext, subFunctions.get()));
	}

	@Test
	public void undefinedPattern() {
		final Pattern pattern = new UndefinedPattern();

		final Function shouldNotMatch = new Number(mathContext, 0);
		assertFalse(pattern.match(shouldNotMatch).isPresent());

		final Function shouldMatch = new Undefined(mathContext);
		final Optional<Map<String, Function>> subFunctions = pattern.match(shouldMatch);
		assertTrue(subFunctions.isPresent());
		assertTrue(pattern.replace(mathContext, subFunctions.get()) instanceof Undefined);
	}

	@Test
	public void equationsSystemPattern() {
		final Pattern pattern = new EquationsSystemPattern(new Pattern[]{
				new SubFunctionPattern("x"),
				new SubFunctionPattern("y"),
				new SubFunctionPattern("z")
		});

		final Function shouldNotMatch = new EquationsSystem(
				mathContext,
				new Function[]{
						new Number(mathContext, 1),
						new Number(mathContext, 2),
				}
		);
		assertFalse(pattern.match(shouldNotMatch).isPresent());

		final Function shouldMatch = new EquationsSystem(
				mathContext,
				new Function[]{
						new Number(mathContext, 1),
						new Number(mathContext, 2),
						new Number(mathContext, 3)
				}
		);
		final Optional<Map<String, Function>> subFunctions = pattern.match(shouldMatch);
		assertTrue(subFunctions.isPresent());
		assertEquals(shouldMatch, pattern.replace(mathContext, subFunctions.get()));
	}

	@Test
	public void rootPatternForRootSquare() {
		final Pattern pattern = new RootPattern(
				new SubFunctionPattern("x"),
				new SubFunctionPattern("y")
		);

		final Function root = new Root(
				mathContext,
				new Number(mathContext, 2),
				new Number(mathContext, 1)
		);
		final Optional<Map<String, Function>> rootSubFunctions = pattern.match(root);
		assertTrue(rootSubFunctions.isPresent());

		final Function rootSquare = new RootSquare(
				mathContext,
				new Number(mathContext, 1)
		);
		final Optional<Map<String, Function>> rootSquareSubFunctions = pattern.match(rootSquare);
		assertTrue(rootSquareSubFunctions.isPresent());
		assertEquals(rootSubFunctions.get(), rootSquareSubFunctions.get());

		final Function replacement = pattern.replace(mathContext, rootSubFunctions.get());
		assertTrue(replacement instanceof RootSquare);
		assertEquals(rootSquare, replacement);
	}

	@Test
	public void constantPattern() {
		final Pattern pattern = new ConstantPattern(MathematicalSymbols.PI);

		final Function shouldNotMatch = new Variable(
				mathContext,
				MathematicalSymbols.EULER_NUMBER,
				Variable.V_TYPE.CONSTANT
		);
		assertFalse(pattern.match(shouldNotMatch).isPresent());

		final Function shouldMatch = new Variable(
				mathContext,
				MathematicalSymbols.PI,
				Variable.V_TYPE.CONSTANT
		);
		final Optional<Map<String, Function>> subFunctions = pattern.match(shouldMatch);
		assertTrue(subFunctions.isPresent());
		assertEquals(shouldMatch, pattern.replace(mathContext, subFunctions.get()));
	}

	@Test
	public void otherBinaryPatterns() {
		final Number one = new Number(mathContext, 1);
		final Number two = new Number(mathContext, 2);
		final SubFunctionPattern x = new SubFunctionPattern("x");
		final SubFunctionPattern y = new SubFunctionPattern("y");

		final Function shouldNotMatch = new Sum(mathContext, one, two);

		final List<ImmutablePair<Pattern, Function>> patternsAndMatchingFunctions = Arrays.asList(
				new ImmutablePair<>(
						new DivisionPattern(x, y),
						new Division(mathContext, one, two)
				),
				new ImmutablePair<>(
						new EquationPattern(x, y),
						new Equation(mathContext, one, two)
				),
				new ImmutablePair<>(
						new LogarithmPattern(x, y),
						new Logarithm(mathContext, one, two)
				),
				new ImmutablePair<>(
						new MultiplicationPattern(x, y),
						new Multiplication(mathContext, one, two)
				),
				new ImmutablePair<>(
						new PowerPattern(x, y),
						new Power(mathContext, one, two)
				),
				new ImmutablePair<>(
						new RootPattern(x, y),
						new Root(mathContext, one, two)
				),
				new ImmutablePair<>(
						new SubtractionPattern(x, y),
						new Subtraction(mathContext, one, two)
				),
				new ImmutablePair<>(
						new SumSubtractionPattern(x, y),
						new SumSubtraction(mathContext, one, two)
				)
		);

		testMultiplePatterns(shouldNotMatch, patternsAndMatchingFunctions);
	}

	@Test
	public void otherUnaryPatterns() {
		final Number one = new Number(mathContext, 1);
		final SubFunctionPattern x = new SubFunctionPattern("x");

		final Function shouldNotMatch = new Negative(mathContext, one);

		final List<ImmutablePair<Pattern, Function>> patternsAndMatchingFunctions = Arrays.asList(
				new ImmutablePair<>(
						new ArcCosinePattern(x),
						new ArcCosine(mathContext, one)
				),
				new ImmutablePair<>(
						new ArcSinePattern(x),
						new ArcSine(mathContext, one)
				),
				new ImmutablePair<>(
						new ArcTangentPattern(x),
						new ArcTangent(mathContext, one)
				),
				new ImmutablePair<>(
						new CosinePattern(x),
						new Cosine(mathContext, one)
				),
				new ImmutablePair<>(
						new SinePattern(x),
						new Sine(mathContext, one)
				),
				new ImmutablePair<>(
						new TangentPattern(x),
						new Tangent(mathContext, one)
				)
		);

		testMultiplePatterns(shouldNotMatch, patternsAndMatchingFunctions);
	}

	private void testMultiplePatterns(
			final Function shouldNotMatch,
			final List<ImmutablePair<Pattern, Function>> patternsAndMatchingFunctions
	) {
		for (final ImmutablePair<Pattern, Function> patternAndMatchingFunction : patternsAndMatchingFunctions) {
			final Pattern pattern = patternAndMatchingFunction.getLeft();
			final Function shouldMatch = patternAndMatchingFunction.getRight();

			assertFalse(pattern.match(shouldNotMatch).isPresent());

			final Optional<Map<String, Function>> subFunctions = pattern.match(shouldMatch);
			assertTrue(subFunctions.isPresent());
			assertEquals(shouldMatch, pattern.replace(mathContext, subFunctions.get()));
		}
	}
}