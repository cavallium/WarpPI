package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockSquareRoot;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RootSquare extends FunctionOperator {

	public RootSquare(MathContext root, Function value2) {
		super(root, new Number(root, 2), value2);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Root) {
			final FunctionOperator f = (FunctionOperator) o;
			return parameter1.equals(f.getParameter1()) && parameter2.equals(f.getParameter2());
		}
		return false;
	}

	@Override
	public RootSquare clone() {
		return new RootSquare(mathContext, parameter2);
	}

	@Override
	public ObjectArrayList<Block> toBlock(MathContext context) throws Error {
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		final BlockSquareRoot bsqr = new BlockSquareRoot();
		final BlockContainer bsqrc = bsqr.getNumberContainer();
		for (final Block b : getParameter2().toBlock(context)) {
			bsqrc.appendBlockUnsafe(b);
		}
		bsqrc.recomputeDimensions();
		bsqr.recomputeDimensions();
		result.add((bsqr));
		return result;
	}

}
