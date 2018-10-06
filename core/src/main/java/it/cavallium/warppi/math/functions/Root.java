package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.FunctionVisitor;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;
import it.cavallium.warppi.util.Errors;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Root extends FunctionOperator {

	public Root(final MathContext root, final Function value1, final Function value2) {
		super(root, value1, value2);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof Root) {
			final FunctionOperator f = (FunctionOperator) o;
			return parameter1.equals(f.getParameter1()) && parameter2.equals(f.getParameter2());
		}
		return false;
	}

	@Override
	public Root clone() {
		return new Root(mathContext, parameter1 == null ? null : parameter1.clone(), parameter2 == null ? null : parameter2.clone());
	}

	@Override
	public Root clone(MathContext c) {
		return new Root(c, parameter1 == null ? null : parameter1.clone(c), parameter2 == null ? null : parameter2.clone(c));
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		// TODO Auto-generated method stub
		throw new Error(Errors.NOT_IMPLEMENTED, "Unknown function " + getClass().getSimpleName());
	}

	@Override
	public <T> T accept(FunctionVisitor<T> visitor) {
		return visitor.visit(this);
	}

}
