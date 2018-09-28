package it.cavallium.warppi.math.functions.equations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.solver.SolveMethod;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Equation extends FunctionOperator {

	public Equation(final MathContext root, final Function value1, final Function value2) {
		super(root, value1, value2);
	}

	public List<Function> solve(final char variableCharacter) {
		@SuppressWarnings("unused")
		final ObjectArrayList<Equation> e;
		//TODO: WORK IN PROGRESS.
		//TODO: Finire. Fare in modo che risolva i passaggi fino a che non ce ne sono più
		return null;
	}

	//WORK IN PROGRESS
	public ObjectArrayList<Equation> solveStep(final char charIncognita) {
		ObjectArrayList<Equation> result = new ObjectArrayList<>();
		result.add(clone());
		for (final SolveMethod t : SolveMethod.techniques) {
			final ObjectArrayList<Equation> newResults = new ObjectArrayList<>();
			final int sz = result.size();
			for (int n = 0; n < sz; n++) {
				newResults.addAll(t.solve(result.get(n)));
			}
			final Set<Equation> hs = new HashSet<>();
			hs.addAll(newResults);
			newResults.clear();
			newResults.addAll(hs);
			result = newResults;
		}
		// TODO: controllare se è a posto
		return result;
	}

	@Override
	public Equation clone() {
		return new Equation(mathContext, parameter1, parameter2);
	}

	@Override
	public boolean equals(final Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		// TODO Auto-generated method stub
		return null;
	}

}