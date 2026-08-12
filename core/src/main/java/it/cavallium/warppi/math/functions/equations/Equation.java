package it.cavallium.warppi.math.functions.equations;

import java.util.List;
import java.util.Objects;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
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
	public ObjectArrayList<Equation> solveStep(final char variableCharacter) {
		for (final SolveMethod t : SolveMethod.techniques) {
			final ObjectArrayList<Equation> result = t.solve(this, variableCharacter);
			if (!result.isEmpty()) {
				return result;
			}
		}
		return new ObjectArrayList<>();
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
		return o instanceof Equation
				&& Objects.equals(parameter1, ((Equation) o).parameter1)
				&& Objects.equals(parameter2, ((Equation) o).parameter2);
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		if (parameter1 != null) {
			result.addAll(parameter1.toBlock(context));
		}
		result.add(new BlockChar(MathematicalSymbols.EQUATION));
		if (parameter2 != null) {
			result.addAll(parameter2.toBlock(context));
		}
		return result;
	}

	@Override
	public <Argument, Result> Result accept(final Function.Visitor<Argument, Result> visitor, final Argument argument) {
		return visitor.visit(this, argument);
	}
}
