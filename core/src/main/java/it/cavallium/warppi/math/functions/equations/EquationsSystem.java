package it.cavallium.warppi.math.functions.equations;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionDynamic;
import it.cavallium.warppi.math.FunctionVisitor;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class EquationsSystem extends FunctionDynamic {
	static final int spacing = 2;

	public EquationsSystem(final MathContext root) {
		super(root);
	}

	public EquationsSystem(final MathContext root, final Function value) {
		super(root, new Function[] { value });
	}

	public EquationsSystem(final MathContext root, final Function[] value) {
		super(root, value);
	}

	@Override
	public EquationsSystem clone() {
		Function[] newFuncs = functions.clone();
		for (int i = 0; i < newFuncs.length; i++) {
			newFuncs[i] = newFuncs[i].clone();
		}
		return new EquationsSystem(root, newFuncs);
	}

	@Override
	public EquationsSystem clone(MathContext c) {
		Function[] newFuncs = new Function[this.functions.length];
		for (int i = 0; i < newFuncs.length; i++) {
			newFuncs[i] = this.functions[i] == null ? null : this.functions[i].clone(c);
		}
		return new EquationsSystem(c, newFuncs);
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T accept(final FunctionVisitor<T> visitor) {
		return visitor.visit(this);
	}

}
