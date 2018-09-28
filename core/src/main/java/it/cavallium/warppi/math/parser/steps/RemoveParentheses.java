package it.cavallium.warppi.math.parser.steps;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Expression;
import it.cavallium.warppi.math.parser.MathParserStep;
import it.cavallium.warppi.util.IntWrapper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RemoveParentheses implements MathParserStep {

	@SuppressWarnings("unused")
	private final MathContext context;

	public RemoveParentheses(final MathContext context) {
		this.context = context;
	}

	@Override
	public boolean eval(final IntWrapper curIndex, final Function lastFunction, final Function currentFunction,
			final ObjectArrayList<Function> functionsList) {
		if (currentFunction instanceof Expression) {
			if (((Expression) currentFunction).getParameter() == null) {
				functionsList.remove(curIndex.i);
			} else {
				functionsList.set(curIndex.i, ((Expression) currentFunction).getParameter());
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean requiresReversedIteration() {
		return false;
	}

	@Override
	public String getStepName() {
		return "Remove parentheses";
	}

}
