package org.warp.picalculator.math.functions.trigonometry;

import org.warp.picalculator.Error;
import org.warp.picalculator.gui.expression.blocks.Block;
import org.warp.picalculator.gui.expression.blocks.BlockContainer;
import org.warp.picalculator.gui.expression.blocks.BlockSine;
import org.warp.picalculator.math.Function;
import org.warp.picalculator.math.FunctionSingle;
import org.warp.picalculator.math.MathContext;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Sine extends FunctionSingle {

	public Sine(MathContext root, Function value) {
		super(root, value);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Sine) {
			final FunctionSingle f = (FunctionSingle) o;
			if (parameter.equals(f.getParameter())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Sine clone() {
		return new Sine(mathContext, parameter);
	}

	@Override
	public ObjectArrayList<Block> toBlock(MathContext context) throws Error {
		final ObjectArrayList<Block> result = new ObjectArrayList<>();
		final ObjectArrayList<Block> sub = getParameter(0).toBlock(context);
		final BlockSine bs = new BlockSine();
		final BlockContainer bpc = bs.getNumberContainer();
		for (final Block b : sub) {
			bpc.appendBlockUnsafe(b);
		}
		bpc.recomputeDimensions();
		bs.recomputeDimensions();
		result.add(bs);
		return result;
	}

}
