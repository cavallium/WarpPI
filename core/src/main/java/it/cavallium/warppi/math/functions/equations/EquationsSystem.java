package it.cavallium.warppi.math.functions.equations;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionDynamic;
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
		return new EquationsSystem(root, functions);
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		// TODO Auto-generated method stub
		return null;
	}

}
