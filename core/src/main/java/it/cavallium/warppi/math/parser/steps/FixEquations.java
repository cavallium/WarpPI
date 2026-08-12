package it.cavallium.warppi.math.parser.steps;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.functions.equations.Equation;
import it.cavallium.warppi.math.parser.MathParserStep;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.cavallium.warppi.util.IntWrapper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class FixEquations implements MathParserStep {

	@Override
	public boolean eval(final IntWrapper curIndex, final Function lastFunction, final Function currentFunction,
			final ObjectArrayList<Function> functionsList) throws Error {
		if (!(currentFunction instanceof Equation)) {
			return false;
		}
		final Equation equation = (Equation) currentFunction;
		final Function left = equation.getParameter1();
		final Function right = equation.getParameter2();
		if (left != null && right != null) {
			return false;
		}
		if (left != null || right != null || curIndex.i == 0 || curIndex.i + 1 >= functionsList.size()) {
			throw new Error(Errors.MISSING_ARGUMENTS, "There is an equation symbol without arguments on both sides.");
		}

		final Function previous = functionsList.get(curIndex.i - 1);
		final Function next = functionsList.get(curIndex.i + 1);
		if (previous instanceof Equation || next instanceof Equation) {
			throw new Error(Errors.SYNTAX_ERROR, "Chained equations are not supported.");
		}
		functionsList.set(curIndex.i, equation.setParameter(0, previous).setParameter(1, next));
		functionsList.remove(curIndex.i + 1);
		functionsList.remove(curIndex.i - 1);
		curIndex.i--;
		return true;
	}

	@Override
	public boolean requiresReversedIteration() {
		return false;
	}

	@Override
	public String getStepName() {
		return "Fix Equations";
	}

}
