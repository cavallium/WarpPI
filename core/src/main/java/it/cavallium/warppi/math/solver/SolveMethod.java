package it.cavallium.warppi.math.solver;

import it.cavallium.warppi.math.functions.equations.Equation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public interface SolveMethod {
	SolveMethod[] techniques = new SolveMethod[] {};

	ObjectArrayList<Equation> solve(Equation equation);
}
