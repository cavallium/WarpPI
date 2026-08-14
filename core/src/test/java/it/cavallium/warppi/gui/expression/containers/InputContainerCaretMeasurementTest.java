package it.cavallium.warppi.gui.expression.containers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.math.MathematicalSymbols;

class InputContainerCaretMeasurementTest {

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
	void measuresTheCaretInsideLargeNestedFractionsWithoutChangingItsPosition() {
		final NormalInputContainer input = new NormalInputContainer(new InputContext());
		input.typeChar('1');
		input.typeChar(MathematicalSymbols.DIVISION);
		input.typeChar('2');
		for (int digit = 0; digit < 80; digit++) {
			input.typeChar((char) ('0' + digit % 10));
		}
		final int caretPosition = input.getCaretPosition();

		final InputContainer.CaretBounds bounds = input.measureCaret(null);
		assertEquals(bounds, input.measureCaret(null));
		assertEquals(caretPosition, input.getCaretPosition());
		assertTrue(bounds.x() > 0);
		assertTrue(bounds.y() > 0);
		assertTrue(bounds.width() > 0);
		assertTrue(bounds.height() > 0);

		input.moveLeft();
		final InputContainer.CaretBounds previous = input.measureCaret(null);
		assertEquals(caretPosition - 1, input.getCaretPosition());
		assertTrue(previous.x() < bounds.x());
	}

	@Test
	void movesVerticallyBetweenFractionRowsAndKeepsTheHorizontalColumn() {
		final NormalInputContainer input = new NormalInputContainer(new InputContext());
		input.typeChar('1');
		input.typeChar(MathematicalSymbols.DIVISION);
		for (int digit = 0; digit < 80; digit++) {
			input.typeChar((char) ('0' + digit % 10));
		}

		final int denominatorPosition = input.getCaretPosition();
		final InputContainer.CaretBounds denominatorCaret = input.measureCaret(null);
		assertTrue(input.moveUp(null));
		final InputContainer.CaretBounds numeratorCaret = input.measureCaret(null);
		assertTrue(numeratorCaret.y() < denominatorCaret.y());

		assertTrue(input.moveDown(null));
		assertEquals(denominatorPosition, input.getCaretPosition());
		assertEquals(denominatorCaret, input.measureCaret(null));
	}

	@Test
	void leavesVerticalKeysAtTheCurrentPositionWhenThereIsNoOtherRow() {
		final NormalInputContainer input = new NormalInputContainer(new InputContext());
		input.typeChar('1');
		input.typeChar('+');
		input.typeChar('2');
		final int position = input.getCaretPosition();

		assertFalse(input.moveUp(null));
		assertFalse(input.moveDown(null));
		assertEquals(position, input.getCaretPosition());
	}
}
