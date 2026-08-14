package it.cavallium.warppi.math.functions.trigonometry;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockTrigonometric;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ArcCosine extends FunctionSingle {

	public ArcCosine(final MathContext root, final Function value) {
		super(root, value);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof ArcCosine) {
			final FunctionSingle f = (FunctionSingle) o;
			return parameter.equals(f.getParameter());
		}
		return false;
	}

	@Override
	public ArcCosine clone() {
		return new ArcCosine(mathContext, parameter == null ? null : parameter.clone());
	}

	@Override
	public ArcCosine clone(MathContext c) {
		return new ArcCosine(c, parameter == null ? null : parameter.clone(c));
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		final BlockTrigonometric block = new BlockTrigonometric(BlockTrigonometric.Kind.ARC_COSINE);
		final BlockContainer container = block.getNumberContainer();
		for (final Block child : getParameter().toBlock(context))
			container.appendBlockUnsafe(child);
		container.recomputeDimensions();
		block.recomputeDimensions();
		return new ObjectArrayList<>(new Block[] { block });
	}

	@Override
	public <Argument, Result> Result accept(final Function.Visitor<Argument, Result> visitor, final Argument argument) {
		return visitor.visit(this, argument);
	}

}
