package it.cavallium.warppi.math.parser.steps;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Comparison;
import it.cavallium.warppi.math.functions.equations.Equation;
import it.cavallium.warppi.math.parser.MathParserStep;
import it.cavallium.warppi.util.IntWrapper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class AddImplicitMultiplications implements MathParserStep {

	private final MathContext context;

	public AddImplicitMultiplications(final MathContext context) {
		this.context = context;
	}

	@Override
	public boolean eval(final IntWrapper curIndex, final Function lastFunction, final Function currentFunction,
			final ObjectArrayList<Function> functionsList) {
		if (currentFunction == null || lastFunction == null
				|| currentFunction instanceof Equation || lastFunction instanceof Equation
				|| currentFunction instanceof Comparison || lastFunction instanceof Comparison) {
			return false;
		}
		functionsList.set(curIndex.i, new Multiplication(context, currentFunction, lastFunction));
		functionsList.remove(curIndex.i + 1);
		return true;
	}

	@Override
	public boolean requiresReversedIteration() {
		return true;
	}

	@Override
	public String getStepName() {
		return "Add implicit multiplications before and after Functions";
	}

}
