package it.cavallium.warppi.math.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import it.cavallium.warppi.gui.expression.blocks.BlockPostfixFunction;
import it.cavallium.warppi.gui.expression.blocks.BlockNegative;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockDivision;
import it.cavallium.warppi.gui.expression.blocks.BlockMixedFraction;
import it.cavallium.warppi.gui.expression.containers.NormalInputContainer;
import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.functions.Comparison;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Negative;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.functions.ScientificFunction;
import it.cavallium.warppi.math.functions.ScientificFunctionKind;
import it.cavallium.warppi.math.functions.SumSubtraction;
import it.cavallium.warppi.math.functions.trigonometry.ArcCosine;
import it.cavallium.warppi.math.functions.trigonometry.ArcSine;
import it.cavallium.warppi.math.functions.trigonometry.ArcTangent;
import it.cavallium.warppi.math.functions.trigonometry.Cosine;
import it.cavallium.warppi.math.functions.trigonometry.Sine;
import it.cavallium.warppi.math.functions.trigonometry.Tangent;
import it.cavallium.warppi.math.rules.functions.SumSubtractionRule;
import it.cavallium.warppi.util.Error;

class ScientificInputParsingTest {
	private final MathContext context = new MathContext();

	@BeforeAll
	static void initializeBlockFonts() {
		if (BlockContainer.isInitialized())
			return;
		final BinaryFont font = (BinaryFont) Proxy.newProxyInstance(
				BinaryFont.class.getClassLoader(), new Class<?>[] { BinaryFont.class },
				(proxy, method, arguments) -> method.getReturnType() == int.class ? 8
						: method.getReturnType() == boolean.class ? false : null);
		BlockContainer.initializeFonts(font, font);
	}

	@Test
	void parsesAllSixTrigonometricInputBlocks() throws Error {
		assertInstanceOf(Sine.class, parseFunction(MathematicalSymbols.SINE));
		assertInstanceOf(Cosine.class, parseFunction(MathematicalSymbols.COSINE));
		assertInstanceOf(Tangent.class, parseFunction(MathematicalSymbols.TANGENT));
		assertInstanceOf(ArcSine.class, parseFunction(MathematicalSymbols.ARC_SINE));
		assertInstanceOf(ArcCosine.class, parseFunction(MathematicalSymbols.ARC_COSINE));
		assertInstanceOf(ArcTangent.class, parseFunction(MathematicalSymbols.ARC_TANGENT));
	}

	@Test
	void parsesComparisonSymbolsWithoutChangingTheirDirection() throws Error {
		for (final char symbol : MathematicalSymbols.comparisons) {
			final NormalInputContainer input = input();
			input.typeChar('1');
			input.typeChar(symbol);
			input.typeChar('2');
			final Comparison comparison = assertInstanceOf(Comparison.class, input.toFunction(context));
			assertEquals(symbol, comparison.getOperator().symbol());
		}
	}

	@Test
	void parsesLeadingPlusAsIdentity() throws Error {
		final NormalInputContainer input = input();
		input.typeChar('+');
		input.typeChar('6');

		assertEquals(new Number(context, 6), input.toFunction(context));
	}

	@Test
	void parsesLeadingPlusMinusAsTwoSignedBranches() throws Error {
		final NormalInputContainer input = input();
		input.typeChar(MathematicalSymbols.SUM_SUBTRACTION);
		input.typeChar('6');

		final SumSubtraction plusMinus = assertInstanceOf(SumSubtraction.class,
				input.toFunction(context));
		assertEquals(new Number(context, 0), plusMinus.getParameter1());
		assertEquals(new Number(context, 6), plusMinus.getParameter2());
		assertEquals(java.util.List.of(new Number(context, 6), new Number(context, -6)),
				new SumSubtractionRule().execute(plusMinus));
	}

	@Test
	void acceptsPrefixSignsAfterAnotherOperator() throws Error {
		final NormalInputContainer plusInput = input();
		plusInput.typeChar('2');
		plusInput.typeChar('*');
		plusInput.typeChar('+');
		plusInput.typeChar('6');
		assertEquals(new it.cavallium.warppi.math.functions.Multiplication(context,
				new Number(context, 2), new Number(context, 6)), plusInput.toFunction(context));

		final NormalInputContainer plusMinusInput = input();
		plusMinusInput.typeChar('2');
		plusMinusInput.typeChar('*');
		plusMinusInput.typeChar(MathematicalSymbols.SUM_SUBTRACTION);
		plusMinusInput.typeChar('6');
		assertEquals(new it.cavallium.warppi.math.functions.Multiplication(context,
				new Number(context, 2), new SumSubtraction(context,
						new Number(context, 0), new Number(context, 6))),
				plusMinusInput.toFunction(context));
	}

	@Test
	void keepsEachRepeatedPlusMinusSignAsAUnaryBranch() throws Error {
		final NormalInputContainer input = input();
		input.typeChar(MathematicalSymbols.SUM_SUBTRACTION);
		input.typeChar(MathematicalSymbols.SUM_SUBTRACTION);
		input.typeChar('6');

		assertEquals(new SumSubtraction(context, new Number(context, 0),
				new SumSubtraction(context, new Number(context, 0), new Number(context, 6))),
				input.toFunction(context));
	}

	@Test
	void composesPrefixPlusAndPlusMinusWithNegativeNumbers() throws Error {
		final NormalInputContainer plusNegative = input();
		plusNegative.typeChar('+');
		plusNegative.typeChar('-');
		plusNegative.typeChar('6');
		assertEquals(new Number(context, -6), plusNegative.toFunction(context));

		final NormalInputContainer plusMinusNegative = input();
		plusMinusNegative.typeChar(MathematicalSymbols.SUM_SUBTRACTION);
		plusMinusNegative.typeChar('-');
		plusMinusNegative.typeChar('6');
		assertEquals(new SumSubtraction(context, new Number(context, 0), new Number(context, -6)),
				plusMinusNegative.toFunction(context));
	}

	@Test
	void attachesPostfixFunctionsToTheExpressionOnTheirLeft() throws Error {
		for (final ScientificFunctionKind kind : new ScientificFunctionKind[] {
				ScientificFunctionKind.FACTORIAL, ScientificFunctionKind.PERCENT,
				ScientificFunctionKind.DEGREE_MINUTE_SECOND }) {
			final NormalInputContainer input = input();
			input.typeChar('5');
			input.typeBlock(new BlockPostfixFunction(kind));
			final ScientificFunction function = assertInstanceOf(ScientificFunction.class,
					input.toFunction(context));
			assertEquals(kind, function.getKind());
			assertEquals(new Number(context, 5), function.getParameter());
		}
	}

	@Test
	void wrapsTheCompleteLeftExpressionInCalculatorNegative() throws Error {
		final NormalInputContainer input = input();
		input.typeChar('2');
		input.typeChar('+');
		input.typeChar('3');
		final BlockNegative negative = new BlockNegative();
		final int emptyWidth = negative.getWidth();
		input.wrapExpressionToLeft(negative, negative.getNumberContainer(), 1);

		assertEquals(new Negative(context, new Sum(context,
				new Number(context, 2), new Number(context, 3))), input.toFunction(context));
		org.junit.jupiter.api.Assertions.assertTrue(negative.getWidth() > emptyWidth);
	}

	@Test
	void fractionKeyWrapsTheCompleteLeftExpressionAndMovesToDenominator() throws Error {
		final NormalInputContainer input = input();
		input.typeChar('2');
		input.typeChar('+');
		input.typeChar('3');
		input.typeChar(MathematicalSymbols.DIVISION);
		input.typeChar('4');

		final Division division = assertInstanceOf(Division.class, input.toFunction(context));
		assertEquals(new Sum(context, new Number(context, 2), new Number(context, 3)),
				division.getParameter1());
		assertEquals(new Number(context, 4), division.getParameter2());
		assertInstanceOf(BlockDivision.class, input.getContent().getFirst());
	}

	@Test
	void fractionKeyAlsoWrapsTheCurrentNestedContainer() throws Error {
		final NormalInputContainer input = input();
		input.typeChar(MathematicalSymbols.PARENTHESIS_OPEN);
		input.typeChar('2');
		input.typeChar('+');
		input.typeChar('3');
		input.typeChar(MathematicalSymbols.DIVISION);
		input.typeChar('4');

		final Division division = assertInstanceOf(Division.class, input.toFunction(context));
		assertEquals(new Sum(context, new Number(context, 2), new Number(context, 3)),
				division.getParameter1());
		assertEquals(new Number(context, 4), division.getParameter2());
	}

	@Test
	void mixedFractionKeyCapturesWholePartAndAdvancesThroughBothFractionFields() throws Error {
		final NormalInputContainer input = input();
		input.typeChar('2');
		input.typeMixedFraction();
		input.typeChar('1');
		input.moveRight();
		input.typeChar('3');

		assertEquals(new Sum(context, new Number(context, 2),
				new Division(context, new Number(context, 1), new Number(context, 3))),
				input.toFunction(context));
		assertInstanceOf(BlockMixedFraction.class, input.getContent().getFirst());
	}

	@Test
	void mixedFractionPreservesTheSignOfANegativeWholePart() throws Error {
		final NormalInputContainer input = input();
		input.typeChar('-');
		input.typeChar('2');
		input.typeMixedFraction();
		input.typeChar('1');
		input.moveRight();
		input.typeChar('3');

		assertEquals(new it.cavallium.warppi.math.functions.Subtraction(context,
				new Number(context, -2),
				new Division(context, new Number(context, 1), new Number(context, 3))),
				input.toFunction(context));
	}

	@Test
	void mixedFractionRejectsNonIntegerTemplateFields() {
		final NormalInputContainer input = input();
		input.typeChar('2');
		input.typeMixedFraction();
		input.typeChar('1');
		input.typeChar('.');
		input.typeChar('5');
		input.moveRight();
		input.typeChar('3');

		assertThrows(Error.class, () -> input.toFunction(context));
	}

	private Function parseFunction(final char symbol) throws Error {
		final NormalInputContainer input = input();
		input.typeChar(symbol);
		input.typeChar('0');
		return input.toFunction(context);
	}

	private static NormalInputContainer input() {
		return new NormalInputContainer(new InputContext());
	}
}
