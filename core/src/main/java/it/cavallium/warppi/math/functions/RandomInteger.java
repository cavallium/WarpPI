package it.cavallium.warppi.math.functions;

import java.util.Objects;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockRandomInteger;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionOperator;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class RandomInteger extends FunctionOperator {
	public RandomInteger(final MathContext context, final Function minimum, final Function maximum) {
		super(context, minimum, maximum);
	}

	@Override
	public RandomInteger clone() {
		return new RandomInteger(mathContext,
				parameter1 == null ? null : parameter1.clone(),
				parameter2 == null ? null : parameter2.clone());
	}

	@Override
	public RandomInteger clone(final MathContext context) {
		return new RandomInteger(context,
				parameter1 == null ? null : parameter1.clone(context),
				parameter2 == null ? null : parameter2.clone(context));
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof RandomInteger randomInteger
				&& Objects.equals(parameter1, randomInteger.parameter1)
				&& Objects.equals(parameter2, randomInteger.parameter2);
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		final BlockRandomInteger block = new BlockRandomInteger();
		append(block.getMinimumContainer(), parameter1.toBlock(context));
		append(block.getMaximumContainer(), parameter2.toBlock(context));
		block.recomputeDimensions();
		return new ObjectArrayList<>(new Block[] { block });
	}

	private static void append(final BlockContainer container, final ObjectArrayList<Block> blocks) {
		for (final Block block : blocks)
			container.appendBlockUnsafe(block);
		container.recomputeDimensions();
	}

	@Override
	public <Argument, Result> Result accept(final Function.Visitor<Argument, Result> visitor,
			final Argument argument) {
		return visitor.visit(this, argument);
	}
}
