package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionVisitor;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class EmptyNumber implements Function {

	public EmptyNumber(final MathContext root) {
		this.root = root;
	}

	private final MathContext root;

	@Override
	public ObjectArrayList<Function> simplify(final Rule rule) throws Error, InterruptedException {
		return rule.execute(this);
	}

	@Override
	public MathContext getMathContext() {
		return root;
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof EmptyNumber;
	}

	@Override
	public Function clone() {
		return new EmptyNumber(root);
	}

	@Override
	public Function clone(MathContext c) {
		return new EmptyNumber(c);
	}

	@Override
	public Function setParameter(final int index, final Function var) throws IndexOutOfBoundsException {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public Function getParameter(final int index) throws IndexOutOfBoundsException {
		throw new IndexOutOfBoundsException();
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T accept(FunctionVisitor<T> visitor) {
		return visitor.visit(this);
	}

}
