package it.cavallium.warppi.gui.expression.containers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockDivision;
import it.cavallium.warppi.gui.expression.blocks.BlockMixedFraction;
import it.cavallium.warppi.gui.expression.blocks.BlockNumericChar;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.parser.MathParser;
import it.cavallium.warppi.util.Error;

class ExpressionInputDiagnosticsTest {

	@BeforeAll
	static void initializeBlockFonts() {
		if (BlockContainer.isInitialized()) {
			return;
		}
		final BinaryFont font = (BinaryFont) Proxy.newProxyInstance(
				BinaryFont.class.getClassLoader(), new Class<?>[] { BinaryFont.class },
				(proxy, method, arguments) -> method.getReturnType() == int.class ? 8
						: method.getReturnType() == boolean.class ? false : null);
		BlockContainer.initializeFonts(font, font);
	}

	@Test
	void guidesLonePlusMinusToItsMissingRightOperand() {
		final NormalInputContainer input = input(MathematicalSymbols.SUM_SUBTRACTION);

		assertThrows(Error.class, () -> MathParser.parseInput(new MathContext(), input));
		ExpressionInputDiagnostic diagnostic = input.diagnoseSyntaxError();

		assertEquals(1, diagnostic.caretPosition());
		assertEquals(ExpressionInputDiagnostic.HighlightTarget.PREVIOUS_BLOCK, diagnostic.highlightTarget());
		assertEquals("INCOMPLETE EXPRESSION", diagnostic.title());
		assertEquals("± NEEDS A VALUE ON THE RIGHT", diagnostic.message());
		assertEquals("TYPE VALUE AT CURSOR", diagnostic.correction());

		input.showError(diagnostic);
		assertTrue(input.isErrorHighlighted());
		assertEquals(1, input.getCaretPosition());

		input.typeChar('3');
		assertFalse(input.isErrorHighlighted());
		assertNull(input.diagnoseSyntaxError());
		assertDoesNotThrow(() -> MathParser.parseInput(new MathContext(), input));
	}

	@Test
	void distinguishesPrefixAndBinaryPlusMinus() {
		assertNull(input(MathematicalSymbols.SUM_SUBTRACTION, '2').diagnoseSyntaxError());

		final ExpressionInputDiagnostic after = input('2', MathematicalSymbols.SUM_SUBTRACTION)
				.diagnoseSyntaxError();
		assertEquals(2, after.caretPosition());
		assertEquals(ExpressionInputDiagnostic.HighlightTarget.PREVIOUS_BLOCK, after.highlightTarget());
		assertEquals("± NEEDS A VALUE ON THE RIGHT", after.message());
		assertEquals("TYPE RIGHT VALUE AT CURSOR", after.correction());
	}

	@Test
	void insertsTheCorrectionBetweenAdjacentOperators() {
		final NormalInputContainer input = input('2', '+', '*', '3');
		final ExpressionInputDiagnostic diagnostic = input.diagnoseSyntaxError();

		assertEquals(2, diagnostic.caretPosition());
		assertEquals(ExpressionInputDiagnostic.HighlightTarget.NEXT_BLOCK, diagnostic.highlightTarget());
		assertEquals("MISSING VALUE", diagnostic.title());
		assertEquals("ENTER A VALUE BETWEEN + AND *", diagnostic.message());

		input.showError(diagnostic);
		input.typeChar('4');
		assertNull(input.diagnoseSyntaxError());
		assertDoesNotThrow(() -> MathParser.parseInput(new MathContext(), input));
	}

	@Test
	void reportsAMissingValueBetweenPrefixSignsAndAnotherOperator() {
		final ExpressionInputDiagnostic plusThenMultiply = input('+', '*', '3')
				.diagnoseSyntaxError();
		assertEquals("ENTER A VALUE BETWEEN + AND *", plusThenMultiply.message());

		assertNull(input('+', '-', '3').diagnoseSyntaxError());
		assertNull(input(MathematicalSymbols.SUM_SUBTRACTION, '-', '3').diagnoseSyntaxError());
	}

	@Test
	void identifiesAndRepairsInvalidDecimalPoints() {
		final NormalInputContainer repeated = input('1', '.', '.', '2');
		ExpressionInputDiagnostic diagnostic = repeated.diagnoseSyntaxError();
		assertEquals(3, diagnostic.caretPosition());
		assertEquals(ExpressionInputDiagnostic.HighlightTarget.PREVIOUS_BLOCK, diagnostic.highlightTarget());
		assertEquals("INVALID NUMBER", diagnostic.title());
		assertEquals("ONLY ONE DECIMAL POINT IS ALLOWED", diagnostic.message());

		repeated.showError(diagnostic);
		repeated.del();
		assertNull(repeated.diagnoseSyntaxError());

		final NormalInputContainer incomplete = input('.');
		diagnostic = incomplete.diagnoseSyntaxError();
		assertEquals(1, diagnostic.caretPosition());
		assertEquals("A DECIMAL POINT NEEDS A DIGIT", diagnostic.message());
		incomplete.showError(diagnostic);
		incomplete.typeChar('2');
		assertNull(incomplete.diagnoseSyntaxError());
		assertDoesNotThrow(() -> MathParser.parseInput(new MathContext(), incomplete));
	}

	@Test
	void identifiesEmptyStructuredFields() {
		final NormalInputContainer fractionInput = input(MathematicalSymbols.DIVISION);
		final BlockDivision fraction = (BlockDivision) fractionInput.getContent().get(0);

		ExpressionInputDiagnostic diagnostic = fractionInput.diagnoseSyntaxError();
		assertEquals(1, diagnostic.caretPosition());
		assertEquals(ExpressionInputDiagnostic.HighlightTarget.INSERTION_POINT, diagnostic.highlightTarget());
		assertEquals("INCOMPLETE FRACTION", diagnostic.title());
		assertEquals("ENTER A NUMERATOR", diagnostic.message());

		fraction.getUpperContainer().appendBlock(new BlockNumericChar('1'));
		fraction.recomputeDimensions();
		diagnostic = fractionInput.diagnoseSyntaxError();
		assertEquals(3, diagnostic.caretPosition());
		assertEquals("ENTER A DENOMINATOR", diagnostic.message());

		final ExpressionInputDiagnostic exponent = input('2', MathematicalSymbols.POWER)
				.diagnoseSyntaxError();
		assertEquals(2, exponent.caretPosition());
		assertEquals("INCOMPLETE POWER", exponent.title());
		assertEquals("ENTER AN EXPONENT", exponent.message());

		final ExpressionInputDiagnostic sine = input(MathematicalSymbols.SINE).diagnoseSyntaxError();
		assertEquals("INCOMPLETE FUNCTION", sine.title());
		assertEquals("ENTER A SINE ARGUMENT", sine.message());
	}

	@Test
	void identifiesEachMissingMixedFractionField() {
		final NormalInputContainer input = new NormalInputContainer(new InputContext());
		final BlockMixedFraction fraction = new BlockMixedFraction();
		input.typeBlock(fraction);

		ExpressionInputDiagnostic diagnostic = input.diagnoseSyntaxError();
		assertEquals("INCOMPLETE MIXED FRACTION", diagnostic.title());
		assertEquals("ENTER A WHOLE NUMBER", diagnostic.message());

		fraction.getWholeContainer().appendBlock(new BlockNumericChar('2'));
		fraction.recomputeDimensions();
		diagnostic = input.diagnoseSyntaxError();
		assertEquals("ENTER A NUMERATOR", diagnostic.message());

		fraction.getNumeratorContainer().appendBlock(new BlockNumericChar('1'));
		fraction.recomputeDimensions();
		diagnostic = input.diagnoseSyntaxError();
		assertEquals("ENTER A DENOMINATOR", diagnostic.message());
	}

	@Test
	void reportsEmptyInputAndRepeatedEquation() {
		final ExpressionInputDiagnostic empty = input().diagnoseSyntaxError();
		assertEquals(0, empty.caretPosition());
		assertEquals(ExpressionInputDiagnostic.HighlightTarget.INSERTION_POINT, empty.highlightTarget());
		assertEquals("NOTHING TO CALCULATE", empty.title());
		assertEquals("ENTER AN EXPRESSION", empty.message());

		final NormalInputContainer equation = input('2', '=', '2', '=', '2');
		final ExpressionInputDiagnostic repeatedEquals = equation.diagnoseSyntaxError();
		assertEquals(4, repeatedEquals.caretPosition());
		assertEquals(ExpressionInputDiagnostic.HighlightTarget.PREVIOUS_BLOCK, repeatedEquals.highlightTarget());
		assertEquals("INVALID EQUATION", repeatedEquals.title());
		assertEquals("ONLY ONE = IS ALLOWED", repeatedEquals.message());
	}

	@Test
	void treatsComparisonsAsBinaryOperators() {
		final ExpressionInputDiagnostic diagnostic = input('1', MathematicalSymbols.LESS_OR_EQUAL)
				.diagnoseSyntaxError();

		assertEquals("INCOMPLETE EXPRESSION", diagnostic.title());
		assertEquals("≤ NEEDS A VALUE ON THE RIGHT", diagnostic.message());
		assertEquals("TYPE RIGHT VALUE AT CURSOR", diagnostic.correction());
	}

	@Test
	void leavesCompleteExpressionsUnmarked() {
		assertNull(input('2', MathematicalSymbols.SUM_SUBTRACTION, '3').diagnoseSyntaxError());
		assertNull(input('2', '+', '3', '=', '5').diagnoseSyntaxError());
		assertNull(input('-', '2').diagnoseSyntaxError());
		assertNull(input('+', '2').diagnoseSyntaxError());
		assertNull(input(MathematicalSymbols.SUM_SUBTRACTION, '2').diagnoseSyntaxError());
		assertNull(input('2', '*', '+', '3').diagnoseSyntaxError());
		assertNull(input('2', '*', MathematicalSymbols.SUM_SUBTRACTION, '3').diagnoseSyntaxError());
	}

	private static NormalInputContainer input(final char... characters) {
		final NormalInputContainer input = new NormalInputContainer(new InputContext());
		for (final char character : characters) {
			input.typeChar(character);
		}
		return input;
	}
}
