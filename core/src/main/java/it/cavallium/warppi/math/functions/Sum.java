package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.math.*;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Sum extends FunctionOperator {

	public Sum(final MathContext root, final Function value1, final Function value2) {
		super(root, value1, value2);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof Sum) {
			final FunctionOperator f = (FunctionOperator) o;
			if (parameter1.equals(f.getParameter1()) && parameter2.equals(f.getParameter2())) {
				return true;
			} else if (parameter1.equals(f.getParameter2()) && parameter2.equals(f.getParameter1())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Sum clone() {
		return new Sum(mathContext, parameter1 == null ? parameter1 : parameter1.clone(), parameter2 == null ? null : parameter2.clone());
	}

	@Override
	public Sum clone(MathContext c) {
		return new Sum(c, parameter1 == null ? null : parameter1.clone(c), parameter2 == null ? null : parameter2.clone(c));
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		result.addAll(getParameter1().toBlock(context));
		result.add(new BlockChar(MathematicalSymbols.SUM));
		result.addAll(getParameter2().toBlock(context));
		return result;
	}

	@Override
	public <T> T accept(FunctionVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
