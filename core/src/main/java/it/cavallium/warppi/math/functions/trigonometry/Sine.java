package it.cavallium.warppi.math.functions.trigonometry;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockSine;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Sine extends FunctionSingle {

	public Sine(final MathContext root, final Function value) {
		super(root, value);
	}

	@Override
	public boolean equals(final Object o) {
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
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
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
