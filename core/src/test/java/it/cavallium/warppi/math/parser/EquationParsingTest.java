package it.cavallium.warppi.math.parser;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.graphicengine.BinaryFont;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.functions.Variable.V_TYPE;
import it.cavallium.warppi.math.functions.equations.Equation;
import it.cavallium.warppi.math.parser.features.FeatureChar;
import it.cavallium.warppi.math.parser.features.FeatureNumber;
import it.cavallium.warppi.math.parser.features.FeatureVariable;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquationParsingTest {
	private final MathContext context = new MathContext();

	@Test
	void parsesEquationAndPreservesSolutionVariableType() throws Error {
		final Function parsed = MathParser.joinFeatures(context, features(
				new FeatureVariable(MathematicalSymbols.X, V_TYPE.SOLUTION),
				new FeatureChar(MathematicalSymbols.EQUATION),
				new FeatureNumber('1')
		));

		assertTrue(parsed instanceof Equation);
		final Equation equation = (Equation) parsed;
		assertTrue(equation.getParameter1() instanceof Variable);
		final Variable variable = (Variable) equation.getParameter1();
		assertEquals(MathematicalSymbols.X, variable.getChar());
		assertEquals(V_TYPE.SOLUTION, variable.getType());
		assertEquals(new Number(context, 1), equation.getParameter2());
	}

	@Test
	void rejectsChainedEquations() {
		final Error error = assertThrows(Error.class, () -> MathParser.joinFeatures(context, features(
				new FeatureVariable('A', V_TYPE.VARIABLE),
				new FeatureChar(MathematicalSymbols.EQUATION),
				new FeatureVariable('B', V_TYPE.VARIABLE),
				new FeatureChar(MathematicalSymbols.EQUATION),
				new FeatureVariable('C', V_TYPE.VARIABLE)
		)));

		assertEquals(Errors.SYNTAX_ERROR, error.id);
	}

	@Test
	void rejectsEquationWithoutBothOperands() {
		final Error error = assertThrows(Error.class, () -> MathParser.joinFeatures(context, features(
				new FeatureChar(MathematicalSymbols.EQUATION),
				new FeatureNumber('1')
		)));

		assertEquals(Errors.MISSING_ARGUMENTS, error.id);
	}

	@Test
	void rendersEquationSymbolBetweenOperands() throws Error {
		initializeBlockFonts();
		final Equation equation = new Equation(context, new Number(context, 1), new Number(context, 2));

		final ObjectArrayList<Block> blocks = equation.toBlock(context);

		assertEquals(3, blocks.size());
		assertTrue(blocks.get(1) instanceof BlockChar);
		assertEquals(MathematicalSymbols.EQUATION, ((BlockChar) blocks.get(1)).getChar());
	}

	private static void initializeBlockFonts() {
		if (BlockContainer.isInitialized()) {
			return;
		}
		final BinaryFont font = (BinaryFont) Proxy.newProxyInstance(
				BinaryFont.class.getClassLoader(),
				new Class<?>[] { BinaryFont.class },
				(proxy, method, arguments) -> method.getReturnType() == int.class ? 8
						: method.getReturnType() == boolean.class ? false : null
		);
		BlockContainer.initializeFonts(font, font);
	}

	@SafeVarargs
	private static ObjectArrayList<Feature> features(final Feature... features) {
		return new ObjectArrayList<>(features);
	}
}
