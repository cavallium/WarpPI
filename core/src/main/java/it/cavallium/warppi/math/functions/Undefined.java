package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockUndefined;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.rules.Rule;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Undefined implements Function {

	protected final MathContext root;

	public Undefined(final MathContext root) {
		this.root = root;
	}

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
		return false;
	}

	@Override
	public Undefined clone() {
		return new Undefined(root);
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
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		result.add(new BlockUndefined());
		return result;
	}

	@Override
	public String toString() {
		return "UNDEFINED";
	}

}
