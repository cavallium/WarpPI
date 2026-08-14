package it.cavallium.warppi.gui.expression.blocks;

import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeatureNegative;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;

/** A calculator-style unary negative template, distinct from subtraction. */
public final class BlockNegative extends BlockParenthesisAbstract {
	public BlockNegative() {
		super("-");
	}

	private BlockNegative(final TreeContainer parent, final BlockNegative old,
			final InputContext inputContext) {
		super(parent, old, inputContext);
	}

	@Override
	public Feature toFeature(final MathContext context) throws Error {
		final Function child = getNumberContainer().toFunction(context);
		return new FeatureNegative(child);
	}

	@Override
	public BlockNegative clone(final TreeContainer parent, final InputContext inputContext) {
		return new BlockNegative(parent, this, inputContext);
	}
}
