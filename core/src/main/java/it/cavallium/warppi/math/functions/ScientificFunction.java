package it.cavallium.warppi.math.functions;

import java.util.Objects;

import it.cavallium.warppi.gui.expression.blocks.Block;
import it.cavallium.warppi.gui.expression.blocks.BlockContainer;
import it.cavallium.warppi.gui.expression.blocks.BlockPostfixFunction;
import it.cavallium.warppi.gui.expression.blocks.BlockScientificFunction;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.FunctionSingle;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.util.Error;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class ScientificFunction extends FunctionSingle {
	private final ScientificFunctionKind kind;

	public ScientificFunction(final MathContext context, final ScientificFunctionKind kind,
			final Function parameter) {
		super(context, parameter);
		this.kind = Objects.requireNonNull(kind, "kind");
	}

	public ScientificFunctionKind getKind() {
		return kind;
	}

	@Override
	public ScientificFunction clone() {
		return new ScientificFunction(mathContext, kind, parameter == null ? null : parameter.clone());
	}

	@Override
	public ScientificFunction clone(final MathContext context) {
		return new ScientificFunction(context, kind, parameter == null ? null : parameter.clone(context));
	}

	@Override
	public boolean equals(final Object other) {
		return other instanceof ScientificFunction function
				&& kind == function.kind && Objects.equals(parameter, function.parameter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(kind, parameter);
	}

	@Override
	public ObjectArrayList<Block> toBlock(final MathContext context) throws Error {
		final ObjectArrayList<Block> parameterBlocks = parameter.toBlock(context);
		if (kind.isPostfix()) {
			parameterBlocks.add(new BlockPostfixFunction(kind));
			return parameterBlocks;
		}

		final BlockScientificFunction block = new BlockScientificFunction(kind);
		final BlockContainer container = block.getNumberContainer();
		for (final Block parameterBlock : parameterBlocks)
			container.appendBlockUnsafe(parameterBlock);
		container.recomputeDimensions();
		block.recomputeDimensions();
		return new ObjectArrayList<>(new Block[] { block });
	}

	@Override
	public <Argument, Result> Result accept(final Function.Visitor<Argument, Result> visitor,
			final Argument argument) {
		return visitor.visit(this, argument);
	}
}
