package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockSquareRoot;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RootSquare extends FunctionSingle {

	private final Number degree;

	public RootSquare(final MathContext root, final Function value) {
		super(root, value);
		this.degree = new Number(root, 2);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof RootSquare) {
			final RootSquare f = (RootSquare) o;
			return parameter.equals(f.getParameter());
		}
		return false;
	}

	@Override
	public RootSquare clone() {
		return new RootSquare(mathContext, parameter == null ? null : parameter.clone());
	}

	@Override
	public RootSquare clone(MathContext c) {
		return new RootSquare(c, parameter == null ? null : parameter.clone(c));
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		final BlockSquareRoot bsqr = new BlockSquareRoot();
		final BlockContainer bsqrc = bsqr.getNumberContainer();
		for (final Block b : getParameter().toBlock(context)) {
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

	public Number getDegree() {
		return degree;
	}
}
