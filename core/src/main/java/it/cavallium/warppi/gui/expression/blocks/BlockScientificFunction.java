package it.cavallium.warppi.gui.expression.blocks;

import java.util.Objects;

import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.ScientificFunctionKind;
import it.cavallium.warppi.math.parser.features.FeatureScientificFunction;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;

public final class BlockScientificFunction extends BlockParenthesisAbstract {
	private final ScientificFunctionKind kind;

	public BlockScientificFunction(final ScientificFunctionKind kind) {
		super(requirePrefixKind(kind).prefix());
		this.kind = kind;
	}

	private BlockScientificFunction(final TreeContainer parent, final BlockScientificFunction old,
			final InputContext inputContext) {
		super(parent, old, inputContext);
		kind = old.kind;
	}

	private static ScientificFunctionKind requirePrefixKind(final ScientificFunctionKind kind) {
		if (Objects.requireNonNull(kind, "kind").isPostfix())
			throw new IllegalArgumentException(kind + " is postfix");
		return kind;
	}

	@Override
	public Feature toFeature(final MathContext context) throws Error {
		final Function child = getNumberContainer().toFunction(context);
		return new FeatureScientificFunction(kind, child);
	}

	@Override
	public BlockScientificFunction clone(final TreeContainer parent, final InputContext inputContext) {
		return new BlockScientificFunction(parent, this, inputContext);
	}
}
