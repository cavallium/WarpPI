package it.cavallium.warppi.math.solver;

import it.cavallium.warppi.math.functions.equations.Equation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public interface SolveMethod {
	SolveMethod[] techniques = new SolveMethod[] { new IsolateVariableSolver() };

	ObjectArrayList<Equation> solve(Equation equation, char variable);

	default ObjectArrayList<Equation> solve(final Equation equation) {
		final ObjectArrayList<Equation> result = new ObjectArrayList<>();
		for (final char variable : SolverUtils.findTargets(equation)) {
			for (final Equation solved : solve(equation, variable)) {
				if (!result.contains(solved)) {
					result.add(solved);
				}
			}
		}
		return result;
	}
}
