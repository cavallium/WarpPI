package it.cavallium.warppi.math.functions;

import java.util.Arrays;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionDynamic;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/** A stable prime-product result which must not be multiplied back together by calculation rules. */
public final class PrimeFactorization extends FunctionDynamic {
	public PrimeFactorization(final MathContext context, final Function[] factors) {
		super(context, factors);
		if (factors.length == 0)
			throw new IllegalArgumentException("A prime factorization needs at least one factor");
	}

	@Override
	public PrimeFactorization clone() {
		final Function[] factors = Arrays.stream(functions).map(Function::clone).toArray(Function[]::new);
		return new PrimeFactorization(root, factors);
	}

	@Override
	public PrimeFactorization clone(final MathContext context) {
		final Function[] factors = Arrays.stream(functions)
				.map(factor -> factor.clone(context)).toArray(Function[]::new);
		return new PrimeFactorization(context, factors);
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof PrimeFactorization factorization
				&& Arrays.equals(functions, factorization.functions);
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		final ObjectArrayList<Block> blocks = new ObjectArrayList<>();
		for (int index = 0; index < functions.length; index++) {
			if (index > 0)
				blocks.add(new BlockChar(MathematicalSymbols.MULTIPLICATION));
			blocks.addAll(functions[index].toBlock(context));
		}
		return blocks;
	}

	@Override
	public <Argument, Result> Result accept(final Function.Visitor<Argument, Result> visitor,
			final Argument argument) {
		return visitor.visit(this, argument);
	}

	@Override
	public String toString() {
		return "FACT" + Arrays.toString(functions);
	}
}
