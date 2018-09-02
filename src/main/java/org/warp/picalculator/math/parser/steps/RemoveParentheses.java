package org.warp.picalculator.math.parser.steps;

import org.warp.picalculator.IntegerObj;
import org.warp.picalculator.math.Function;
import org.warp.picalculator.math.MathContext;
import org.warp.picalculator.math.functions.Expression;
import org.warp.picalculator.math.parser.MathParserStep;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RemoveParentheses implements MathParserStep {

	@SuppressWarnings("unused")
	private final MathContext context;

	public RemoveParentheses(MathContext context) {
		this.context = context;
	}

	@Override
	public boolean eval(IntegerObj curIndex, Function lastFunction, Function currentFunction,
			ObjectArrayList<Function> functionsList) {
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
