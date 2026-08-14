package it.cavallium.warppi.math.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockDivision;
import it.cavallium.warppi.gui.expression.blocks.BlockNumericChar;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

class DivisionRenderingTest {
	@Test
	void togglesAnImproperResultToMixedFractionRendering() throws Error {
		initializeBlockFonts();
		final MathContext context = new MathContext();
		final Division division = new Division(context, new Number(context, 7), new Number(context, 3));

		assertInstanceOf(BlockDivision.class, division.toBlock(context).getFirst());

		context.mixedFractionMode = true;
		final ObjectArrayList<Block> mixed = division.toBlock(context);
		assertEquals(3, mixed.size());
		assertEquals('2', ((BlockNumericChar) mixed.get(0)).getChar());
		assertEquals(' ', ((BlockChar) mixed.get(1)).getChar());
		assertInstanceOf(BlockDivision.class, mixed.get(2));
	}

	@Test
	void numberBlocksAreLosslessEditableDecimalsRatherThanFormattedDisplayText() throws Error {
		initializeBlockFonts();
		final MathContext context = new MathContext();
		final Number largeDecimal = new Number(context, new BigDecimal("123456789.125"));

		assertEquals("1.2345679ℯ℮8", largeDecimal.toString());
		assertInstanceOf(it.cavallium.warppi.gui.expression.blocks.BlockExponentialNotation.class,
				largeDecimal.toBlock(context).getLast());

		final MathContext inputContext = new MathContext(context);
		inputContext.losslessInputMode = true;
		final ObjectArrayList<Block> blocks = largeDecimal.clone(inputContext).toBlock(inputContext);
		final StringBuilder editable = new StringBuilder();
		for (final Block block : blocks)
			editable.append(((BlockNumericChar) block).getChar());
		assertEquals("123456789.125", editable.toString());

		final it.cavallium.warppi.gui.expression.containers.NormalInputContainer input =
				new it.cavallium.warppi.gui.expression.containers.NormalInputContainer(
						new it.cavallium.warppi.gui.expression.InputContext());
		for (final Block block : blocks)
			input.typeBlock(block);
		assertEquals(largeDecimal, input.toFunction(context));
	}

	@Test
	void scientificFormattingHandlesNegativeValuesAndCarryWithoutUnknownComma() {
		final MathContext context = new MathContext();

		assertEquals("-1.2345679ℯ℮8", new Number(context, new BigDecimal("-123456789")).toString());
		assertEquals("1ℯ℮9", new Number(context, new BigDecimal("999999999")).toString());
	}

	@Test
	void equalNumbersKeepEqualHashesAcrossDisplayModes() {
		final MathContext decimalContext = new MathContext();
		final MathContext exactContext = new MathContext();
		exactContext.exactMode = true;
		final Number decimal = new Number(decimalContext, new BigDecimal("123456789"));
		final Number exact = new Number(exactContext, new BigDecimal("123456789.0"));

		assertNotEquals(decimal.toString(), exact.toString());
		assertEquals(decimal, exact);
		assertEquals(decimal.hashCode(), exact.hashCode());
	}

	@Test
	void losslessAnswerModeDoesNotReuseMixedFractionDisplayBlocks() throws Error {
		initializeBlockFonts();
		final MathContext context = new MathContext();
		context.mixedFractionMode = true;
		context.losslessInputMode = true;
		final Division division = new Division(context, new Number(context, 7), new Number(context, 3));

		assertInstanceOf(BlockDivision.class, division.toBlock(context).getFirst());
	}

	private static void initializeBlockFonts() {
		if (BlockContainer.isInitialized())
			return;
		final BinaryFont font = (BinaryFont) Proxy.newProxyInstance(BinaryFont.class.getClassLoader(),
				new Class<?>[] { BinaryFont.class },
				(proxy, method, arguments) -> method.getReturnType() == int.class ? 8
						: method.getReturnType() == boolean.class ? false : null);
		BlockContainer.initializeFonts(font, font);
	}
}
