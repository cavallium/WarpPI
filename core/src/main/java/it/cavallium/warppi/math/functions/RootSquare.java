package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockSquareRoot;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RootSquare extends FunctionOperator {

	public RootSquare(final MathContext root, final Function value2) {
		super(root, new Number(root, 2), value2);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof Root || o instanceof RootSquare) {
			final FunctionOperator f = (FunctionOperator) o;
			return parameter1.equals(f.getParameter1()) && parameter2.equals(f.getParameter2());
		}
		return false;
	}

	@Override
	public RootSquare clone() {
		return new RootSquare(mathContext, parameter2 == null ? null : parameter2.clone());
	}

	@Override
	public RootSquare clone(MathContext c) {
		return new RootSquare(c, parameter2 == null ? null : parameter2.clone(c));
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		final BlockSquareRoot bsqr = new BlockSquareRoot();
		final BlockContainer bsqrc = bsqr.getNumberContainer();
		for (final Block b : getParameter2().toBlock(context)) {
			bsqrc.appendBlockUnsafe(b);
		}
		bsqrc.recomputeDimensions();
		bsqr.recomputeDimensions();
		result.add(bsqr);
		return result;
	}

	@Override
	public <Argument, Result> Result accept(final Function.Visitor<Argument, Result> visitor, final Argument argument) {
		return visitor.visit(this, argument);
	}
}
