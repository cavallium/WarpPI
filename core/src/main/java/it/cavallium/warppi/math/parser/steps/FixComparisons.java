package it.cavallium.warppi.math.parser.steps;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.functions.Comparison;
import it.cavallium.warppi.math.parser.MathParserStep;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.cavallium.warppi.util.IntWrapper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class FixComparisons implements MathParserStep {
	@Override
	public boolean eval(final IntWrapper index, final Function lastFunction,
			final Function currentFunction, final ObjectArrayList<Function> functions) throws Error {
		if (!(currentFunction instanceof Comparison comparison))
			return false;
		if (comparison.getParameter1() != null && comparison.getParameter2() != null)
			return false;
		if (comparison.getParameter1() != null || comparison.getParameter2() != null
				|| index.i == 0 || index.i + 1 >= functions.size())
			throw new Error(Errors.MISSING_ARGUMENTS,
					"A comparison needs an argument on both sides.");

		final Function previous = functions.get(index.i - 1);
		final Function next = functions.get(index.i + 1);
		if (previous instanceof Comparison || next instanceof Comparison)
			throw new Error(Errors.SYNTAX_ERROR, "Chained comparisons are not supported.");
		functions.set(index.i, comparison.setParameter(0, previous).setParameter(1, next));
		functions.remove(index.i + 1);
		functions.remove(index.i - 1);
		index.i--;
		return true;
	}

	@Override public boolean requiresReversedIteration() { return false; }
	@Override public String getStepName() { return "Fix Comparisons"; }
}
