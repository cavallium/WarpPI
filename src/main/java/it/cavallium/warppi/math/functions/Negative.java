package it.cavallium.warppi.math.functions;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockChar;
import it.cavallium.warppi.gui.expression.blocks.BlockParenthesis;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.MathematicalSymbols;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class Negative extends FunctionSingle {

	public Negative(MathContext root, Function value) {
		super(root, value);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Negative) {
			return ((Negative) o).getParameter().equals(parameter);
		}
		return false;
	}

	@Override
	public Negative clone() {
		return new Negative(mathContext, parameter);
	}

	@Override
	public ObjectArrayList<Block> toBlock(MathContext context) throws Error {
		final ObjectArrayList<Block> blocks = new ObjectArrayList<>();
		blocks.add(new BlockChar(MathematicalSymbols.MINUS));
		if (new Expression(context, getParameter()).parenthesisNeeded()) {
			final BlockParenthesis par = new BlockParenthesis();
			final ObjectArrayList<Block> parBlocks = getParameter().toBlock(context);
			for (final Block b : parBlocks) {
				par.getNumberContainer().appendBlockUnsafe(b); // Skips recomputeDimension
			}
			par.recomputeDimensions(); // Recompute dimensions after appendBlockUnsafe
			blocks.add(par);
		} else {
			blocks.addAll(getParameter().toBlock(context));
		}
		return blocks;
		// throw new Error(Errors.NOT_IMPLEMENTED, "Unknown function " + getClass().getSimpleName());
	}
}
