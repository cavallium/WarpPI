package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.math.*;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Subtraction extends FunctionOperator {

	public Subtraction(final MathContext root, final Function value1, final Function value2) {
		super(root, value1, value2);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof Subtraction) {
			final FunctionOperator f = (FunctionOperator) o;
			return parameter1.equals(f.getParameter1()) && parameter2.equals(f.getParameter2());
		}
		return false;
	}

	@Override
	public Subtraction clone() {
		return new Subtraction(mathContext, parameter1 == null ? null : parameter1.clone(), parameter2 == null ? null : parameter2.clone());
	}

	@Override
	public Subtraction clone(MathContext c) {
		return new Subtraction(c, parameter1 == null ? null : parameter1.clone(c), parameter2 == null ? null : parameter2.clone(c));
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		result.addAll(getParameter1().toBlock(context));
		result.add(new BlockChar(MathematicalSymbols.SUBTRACTION));
		result.addAll(getParameter2().toBlock(context));
		return result;
	}

	@Override
	public <T> T accept(final FunctionVisitor<T> visitor) {
		return visitor.visit(this);
	}

}