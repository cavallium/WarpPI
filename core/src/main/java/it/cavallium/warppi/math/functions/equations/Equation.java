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
		return new Equation(mathContext, parameter1 == null ? null : parameter1.clone(), parameter2 == null ? null : parameter2.clone());
	}

	@Override
	public Equation clone(MathContext c) {
		return new Equation(c, parameter1 == null ? null : parameter1.clone(c), parameter2 == null ? null : parameter2.clone(c));
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof Equation) {
			final FunctionOperator f = (FunctionOperator) o;
			return parameter1.equals(f.getParameter1()) && parameter2.equals(f.getParameter2());
		}
		return false;
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Argument, Result> Result accept(final Function.Visitor<Argument, Result> visitor, final Argument argument) {
		return visitor.visit(this, argument);
	}
}
