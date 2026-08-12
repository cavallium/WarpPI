package it.cavallium.warppi.math.solver;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Division;
import it.cavallium.warppi.math.functions.Multiplication;
import it.cavallium.warppi.math.functions.Negative;
import it.cavallium.warppi.math.functions.Subtraction;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.math.functions.Variable;
import it.cavallium.warppi.math.functions.equations.Equation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class IsolateVariableSolver implements SolveMethod {
	@Override
	public ObjectArrayList<Equation> solve(final Equation equation, final char variable) {
		final ObjectArrayList<Equation> result = new ObjectArrayList<>();
		final Variable target = SolverUtils.findVariable(equation, variable);
		if (target == null || target.getType() == Variable.V_TYPE.CONSTANT
				|| SolverUtils.countVariable(equation, variable) != 1) {
			return result;
		}

		final Equation isolated = isolate(equation, variable);
		if (isolated != null && !isolated.equals(equation)) {
			result.add(isolated);
		}
		return result;
	}

	private Equation isolate(final Equation equation, final char variable) {
		Function left = equation.getParameter1();
		Function right = equation.getParameter2();
		if (SolverUtils.countVariable(left, variable) == 0) {
			left = equation.getParameter2();
			right = equation.getParameter1();
		}

		final MathContext context = equation.getMathContext();
		while (!isVariable(left, variable)) {
			if (left instanceof Sum) {
				final Sum sum = (Sum) left;
				if (contains(sum.getParameter1(), variable)) {
					right = new Subtraction(context, right, sum.getParameter2());
					left = sum.getParameter1();
				} else {
					right = new Subtraction(context, right, sum.getParameter1());
					left = sum.getParameter2();
				}
			} else if (left instanceof Subtraction) {
				final Subtraction subtraction = (Subtraction) left;
				if (contains(subtraction.getParameter1(), variable)) {
					right = new Sum(context, right, subtraction.getParameter2());
					left = subtraction.getParameter1();
				} else {
					right = new Subtraction(context, subtraction.getParameter1(), right);
					left = subtraction.getParameter2();
				}
			} else if (left instanceof Negative) {
				left = ((Negative) left).getParameter();
				right = new Negative(context, right);
			} else if (left instanceof Multiplication) {
				final Multiplication multiplication = (Multiplication) left;
				final Function other;
				if (contains(multiplication.getParameter1(), variable)) {
					left = multiplication.getParameter1();
					other = multiplication.getParameter2();
				} else {
					left = multiplication.getParameter2();
					other = multiplication.getParameter1();
				}
				if (!SolverUtils.isProvablyNonZero(other)) {
					return null;
				}
				right = new Division(context, right, other);
			} else if (left instanceof Division) {
				final Division division = (Division) left;
				if (!contains(division.getParameter1(), variable)
						|| !SolverUtils.isProvablyNonZero(division.getParameter2())) {
					return null;
				}
				left = division.getParameter1();
				right = new Multiplication(context, right, division.getParameter2());
			} else {
				return null;
			}
		}
		return new Equation(context, left, right);
	}

	private static boolean contains(final Function function, final char variable) {
		return SolverUtils.countVariable(function, variable) == 1;
	}

	private static boolean isVariable(final Function function, final char variable) {
		return function instanceof Variable && ((Variable) function).getChar() == variable;
	}
}
