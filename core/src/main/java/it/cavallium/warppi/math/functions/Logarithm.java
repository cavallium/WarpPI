package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockLogarithm;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.FunctionVisitor;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Logarithm extends FunctionOperator {

	public Logarithm(final MathContext root, final Function value1, final Function value2) {
		super(root, value1, value2);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof Logarithm) {
			final FunctionOperator f = (FunctionOperator) o;
			return parameter1.equals(f.getParameter1()) && parameter2.equals(f.getParameter2());
		}
		return false;
	}

	@Override
	public Logarithm clone() {
		return new Logarithm(mathContext, parameter1 == null ? null : parameter1.clone(), parameter2 == null ? null : parameter2.clone());
	}
	
	@Override
	public Logarithm clone(MathContext c) {
		return new Logarithm(c, parameter1 == null ? null : parameter1.clone(c), parameter2 == null ? null : parameter2.clone(c));
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		final ObjectArrayList<Block> sub1 = getParameter1().toBlock(context);
		final ObjectArrayList<Block> sub2 = getParameter2().toBlock(context);
		final BlockLogarithm bd = new BlockLogarithm();
		final BlockContainer uc = bd.getBaseContainer();
		final BlockContainer lc = bd.getNumberContainer();
		for (final Block b : sub1) {
			uc.appendBlockUnsafe(b);
		}
		for (final Block b : sub2) {
			lc.appendBlockUnsafe(b);
		}
		uc.recomputeDimensions();
		lc.recomputeDimensions();
		bd.recomputeDimensions();
		result.add(bd);
		return result;
	}

	@Override
	public <T> T accept(final FunctionVisitor<T> visitor) {
		return visitor.visit(this);
	}

}
