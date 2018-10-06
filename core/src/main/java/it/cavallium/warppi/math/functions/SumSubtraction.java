package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.math.*;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class SumSubtraction extends FunctionOperator {

	public SumSubtraction(final MathContext root, final Function value1, final Function value2) {
		super(root, value1, value2);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof SumSubtraction) {
			final FunctionOperator f = (FunctionOperator) o;
			return parameter1.equals(f.getParameter1()) && parameter2.equals(f.getParameter2());
		}
		return false;
	}

	@Override
	public SumSubtraction clone() {
		return new SumSubtraction(mathContext, parameter1 == null ? null : parameter1.clone(), parameter2 == null ? null : parameter2.clone());
	}

	@Override
	public SumSubtraction clone(MathContext c) {
		return new SumSubtraction(c, parameter1 == null ? null : parameter1.clone(c), parameter2 == null ? null : parameter2.clone(c));
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		result.addAll(getParameter1().toBlock(context));
		result.add(new BlockChar(MathematicalSymbols.SUM_SUBTRACTION));
		result.addAll(getParameter2().toBlock(context));
		return result;
	}

	@Override
	public <T> T accept(final FunctionVisitor<T> visitor) {
		return visitor.visit(this);
	}

}
