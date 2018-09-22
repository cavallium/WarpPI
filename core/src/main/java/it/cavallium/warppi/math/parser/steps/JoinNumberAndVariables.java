package it.cavallium.warppi.math.parser.steps;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Number;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.parser.MathParserStep;
import it.cavallium.warppi.util.IntWrapper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class JoinNumberAndVariables implements MathParserStep {

	private final MathContext context;

	public JoinNumberAndVariables(final MathContext context) {
		this.context = context;
	}

	@Override
	public boolean eval(final IntWrapper curIndex, final Function lastFunction, final Function currentFunction,
			final ObjectArrayList<Function> functionsList) {
		if (currentFunction instanceof Number | currentFunction instanceof Variable | currentFunction instanceof Division)
			if (lastFunction instanceof Variable | lastFunction instanceof Number | (lastFunction instanceof Multiplication && ((Multiplication) lastFunction).getParameter2() != null)) {
				final Function a = currentFunction;
				final Function b = lastFunction;
				functionsList.set(curIndex.i, new Multiplication(context, a, b));
				functionsList.remove(curIndex.i + 1);
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
		return "Join number and variables together";
	}

}
