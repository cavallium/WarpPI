package org.warp.picalculator.math.functions.equations;

import org.warp.picalculator.Error;
import org.warp.picalculator.gui.expression.blocks.Block;
import org.warp.picalculator.math.Function;
import org.warp.picalculator.math.FunctionDynamic;
import org.warp.picalculator.math.MathContext;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class EquationsSystem extends FunctionDynamic {
	static final int spacing = 2;

	public EquationsSystem(MathContext root) {
		super(root);
	}

	public EquationsSystem(MathContext root, Function value) {
		super(root, new Function[] { value });
	}

	public EquationsSystem(MathContext root, Function[] value) {
		super(root, value);
	}

	@Override
	public EquationsSystem clone() {
		return new EquationsSystem(root, functions);
	}

	@Override
	public ObjectArrayList<Block> toBlock(MathContext context) throws Error {
		// TODO Auto-generated method stub
		return null;
	}

}
