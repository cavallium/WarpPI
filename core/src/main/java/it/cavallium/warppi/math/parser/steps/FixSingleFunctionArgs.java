package it.cavallium.warppi.math.parser.steps;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.parser.MathParserStep;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.cavallium.warppi.util.IntWrapper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Puts the argument of Single Functions inside them
 *
 * @author Andrea Cavalli
 *
 */
public class FixSingleFunctionArgs implements MathParserStep {

	@Override
	public boolean eval(final IntWrapper curIndex, final Function lastFunction, final Function currentFunction,
			final ObjectArrayList<Function> functionsList) throws Error {
		if (currentFunction instanceof FunctionSingle)
			if (((FunctionSingle) currentFunction).getParameter() == null) {
				if (lastFunction == null)
					throw new Error(Errors.MISSING_ARGUMENTS, "There is a function at the end without any argument specified.");
				else {
					((FunctionSingle) currentFunction).setParameter(lastFunction);
					functionsList.remove(curIndex.i + 1);
				}
				return true;
			}
		return false;
	}

	@Override
	public boolean requiresReversedIteration() {
		return true;
	}

	@Override
	public String getStepName() {
		return "Fix Single Function Arguments";
	}

}
