package it.cavallium.warppi.math.parser;

import it.cavallium.warppi.Engine;
import it.cavallium.warppi.Platform.ConsoleUtils;
import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.containers.InputContainer;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.math.functions.Expression;
import it.cavallium.warppi.math.functions.Variable.V_TYPE;
import it.cavallium.warppi.math.parser.features.FeatureChar;
import it.cavallium.warppi.math.parser.features.FeatureDivision;
import it.cavallium.warppi.math.parser.features.FeatureMultiplication;
import it.cavallium.warppi.math.parser.features.FeatureNumber;
import it.cavallium.warppi.math.parser.features.FeaturePower;
import it.cavallium.warppi.math.parser.features.FeaturePowerChar;
import it.cavallium.warppi.math.parser.features.FeatureSubtraction;
import it.cavallium.warppi.math.parser.features.FeatureSum;
import it.cavallium.warppi.math.parser.features.FeatureSumSubtraction;
import it.cavallium.warppi.math.parser.features.FeatureVariable;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.math.parser.steps.AddImplicitMultiplications;
import it.cavallium.warppi.math.parser.steps.FixMultiplicationsAndDivisions;
import it.cavallium.warppi.math.parser.steps.FixSingleFunctionArgs;
import it.cavallium.warppi.math.parser.steps.FixSumsAndSubtractions;
import it.cavallium.warppi.math.parser.steps.JoinNumberAndVariables;
import it.cavallium.warppi.math.parser.steps.RemoveParentheses;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.cavallium.warppi.util.IntWrapper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MathParser {
	public static Expression parseInput(final MathContext context, final InputContainer c) throws Error {
		Expression result;

		final Function resultFunction = c.toFunction(context);

		result = new Expression(context, resultFunction);
		return result;
	}

	public static ObjectArrayList<ObjectArrayList<Block>> parseOutput(final MathContext context,
			final ObjectArrayList<Function> resultExpressions) throws Error {
		final ObjectArrayList<ObjectArrayList<Block>> result = new ObjectArrayList<>();
		for (final Function resultExpression : resultExpressions) {
			final ObjectArrayList<Block> resultBlocks = resultExpression.toBlock(context);
			if (resultBlocks == null)
				throw new Error(Errors.NOT_IMPLEMENTED, "Unknown function " + resultExpression.getClass().getSimpleName());
			result.add(resultBlocks);
		}
		return result;
	}

	public static Function joinFeatures(final MathContext context, ObjectArrayList<Feature> features) throws Error {

		features = MathParser.fixFeatures(context, features);

		ObjectArrayList<Function> process = new ObjectArrayList<>();

		for (final Feature f : features) {
			final Function fnc = f.toFunction(context);
			if (fnc == null)
				throw new Error(Errors.SYNTAX_ERROR, "\"" + f.getClass().getSimpleName() + "\" can't be converted into a Function!");
			process.add(fnc);
		}

		process = MathParser.fixStack(context, process);

		if (process.size() > 1)
			throw new Error(Errors.UNBALANCED_STACK, "The stack is unbalanced. Not all the functions are nested correctly");

		return process.get(0);
	}

	private static ObjectArrayList<Function> fixStack(final MathContext context,
			final ObjectArrayList<Function> functionsList) throws Error {
		final MathParserStep[] steps = new MathParserStep[] { new JoinNumberAndVariables(context), new FixSingleFunctionArgs(), new RemoveParentheses(context), new FixMultiplicationsAndDivisions(), new FixSumsAndSubtractions(), new AddImplicitMultiplications(context), };
		boolean lastLoopDidSomething;
		Function lastElement;

		if (Engine.getPlatform().getSettings().isDebugEnabled()) {
			Engine.getPlatform().getConsoleUtils().out().print(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, "\tStatus: ");
			for (final Function f : functionsList)
				Engine.getPlatform().getConsoleUtils().out().print(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, f.toString());
			Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE);
		}

		for (final MathParserStep step : steps) {
			if (Engine.getPlatform().getSettings().isDebugEnabled())
				Engine.getPlatform().getConsoleUtils().out().println(2, "Stack fixing step \"" + step.getStepName() + "\"");
			final int stepQty = step.requiresReversedIteration() ? -1 : 1,
					initialIndex = step.requiresReversedIteration() ? functionsList.size() - 1 : 0;
			do {
				lastLoopDidSomething = false;
				lastElement = null;
				final IntWrapper curIndex = new IntWrapper(initialIndex);
				while (curIndex.i >= 0 && curIndex.i < functionsList.size()) {
					final int i = curIndex.i;
					final Function f = functionsList.get(i);

					if (step.eval(curIndex, lastElement, f, functionsList))
						lastLoopDidSomething = true;

					lastElement = i >= functionsList.size() ? null : functionsList.get(i);
					curIndex.i += stepQty;
				}
			} while (lastLoopDidSomething);

			if (Engine.getPlatform().getSettings().isDebugEnabled()) {
				Engine.getPlatform().getConsoleUtils().out().print(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, "\tStatus: ");
				for (final Function f : functionsList)
					Engine.getPlatform().getConsoleUtils().out().print(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE, f.toString());
				Engine.getPlatform().getConsoleUtils().out().println(ConsoleUtils.OUTPUTLEVEL_DEBUG_VERBOSE);
			}
		}

//		//Phase 4
//		do {
//			lastLoopDidSomething = false;
//			functionListIterator = functionsList.iterator();
//			while (functionListIterator.hasNext()) {
//				final Function f = functionListIterator.next();
//
//				if (f instanceof Function2Args) {
//
//				}
//			}
//		} while (lastLoopDidSomething);

		return functionsList;
	}

	private static ObjectArrayList<Feature> fixFeatures(final MathContext context, ObjectArrayList<Feature> features)
			throws Error {

		features = MathParser.fixMinuses(context, features);

		features = MathParser.makeNumbers(context, features);

		features = MathParser.makePowers(context, features);

		features = MathParser.convertFunctionChars(context, features);

		return features;
	}

	/**
	 * Create function features from char features
	 *
	 * @param context
	 * @param features
	 * @return
	 */
	private static ObjectArrayList<Feature> convertFunctionChars(final MathContext context,
			final ObjectArrayList<Feature> features) throws Error {
		final ObjectArrayList<Feature> process = new ObjectArrayList<>();

		for (final Feature f : features)
			if (f instanceof FeatureChar) {
				final char featureChar = ((FeatureChar) f).ch;
				Feature result = null;
				switch (featureChar) {
					case MathematicalSymbols.SUM:
						result = new FeatureSum(null, null);
						break;
					case MathematicalSymbols.SUM_SUBTRACTION:
						result = new FeatureSumSubtraction(null, null);
						break;
					case MathematicalSymbols.SUBTRACTION:
						result = new FeatureSubtraction(null, null);
						break;
					case MathematicalSymbols.MULTIPLICATION:
						result = new FeatureMultiplication(null, null);
						break;
					case MathematicalSymbols.DIVISION:
						result = new FeatureDivision(null, null);
						break;
				}

				for (final char var : MathematicalSymbols.variables)
					if (featureChar == var) {
						result = new FeatureVariable(featureChar, V_TYPE.VARIABLE);
						break;
					}

				if (result == null)
					throw new Error(Errors.SYNTAX_ERROR, "Char " + featureChar + " isn't a known feature");

				process.add(result);
			} else
				process.add(f);

		return process;
	}

	/**
	 * Make numbers [-][1][2][+][-][3] => [-12]
	 *
	 * @param context
	 * @param features
	 * @return
	 */
	private static ObjectArrayList<Feature> makeNumbers(final MathContext context,
			final ObjectArrayList<Feature> features) {
		final ObjectArrayList<Feature> process = new ObjectArrayList<>();

		FeatureNumber numberBuffer = null;
		for (final Feature f : features)
			if (f instanceof FeatureChar) {
				final FeatureChar bcf = (FeatureChar) f;
				final char[] numbers = MathematicalSymbols.numbers;
				boolean isNumber = false;
				for (final char n : numbers)
					if (bcf.ch == n) {
						isNumber = true;
						break;
					}
				if (bcf.ch == MathematicalSymbols.MINUS || bcf.ch == '.')
					isNumber = true;
				if (isNumber) {
					if (numberBuffer == null) {
						numberBuffer = new FeatureNumber(bcf.ch);
						process.add(numberBuffer);
					} else
						numberBuffer.append(bcf.ch);
				} else {
					if (numberBuffer != null)
						numberBuffer = null;
					process.add(f);
				}
			} else
				process.add(f);

		return process;
	}

	/**
	 * Fix minuses [-][1][2][+][-][3][-][2] => [-][12][+][-][3][—][2]
	 *
	 * @param context
	 * @param features
	 * @return
	 * @throws Error
	 */
	private static ObjectArrayList<Feature> fixMinuses(final MathContext context,
			final ObjectArrayList<Feature> features) throws Error {
		final ObjectArrayList<Feature> process = new ObjectArrayList<>();
		Feature lastFeature = null;
		for (final Feature f : features) {
			if (f instanceof FeatureChar && (((FeatureChar) f).ch == MathematicalSymbols.SUBTRACTION || ((FeatureChar) f).ch == MathematicalSymbols.MINUS)) {
				boolean isNegativeOfNumber = false;
				if (lastFeature == null)
					isNegativeOfNumber = true;
				else if (lastFeature instanceof FeatureChar) {
					final FeatureChar lcf = (FeatureChar) lastFeature;
					final char[] operators = MathematicalSymbols.functionsAndSignums;
					for (final char operator : operators)
						if (lcf.ch == operator) {
							isNegativeOfNumber = true;
							break;
						}
				}
				if (isNegativeOfNumber)
					process.add(new FeatureChar(MathematicalSymbols.MINUS));
				else
					process.add(new FeatureChar(MathematicalSymbols.SUBTRACTION));
			} else
				process.add(f);
			lastFeature = f;
		}
		return process;
	}

	/**
	 * Make powers [12][^[15]] => [[12]^[15]]
	 *
	 * @param context
	 * @param features
	 * @return
	 * @throws Error
	 */
	private static ObjectArrayList<Feature> makePowers(final MathContext context,
			final ObjectArrayList<Feature> features) throws Error {
		final ObjectArrayList<Feature> process = new ObjectArrayList<>();

		Feature lastFeature = null;
		for (final Feature f : features) {
			if (f instanceof FeaturePowerChar) {
				if (lastFeature != null)
					process.set(process.size() - 1, new FeaturePower(lastFeature.toFunction(context), ((FeaturePowerChar) f).getChild()));
				else
					process.add(f);
			} else
				process.add(f);
			lastFeature = f;
		}

		return process;
	}
}
