package it.cavallium.warppi.gui.expression.blocks;

import java.util.Objects;

import it.cavallium.warppi.gui.expression.InputContext;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.parser.features.FeatureTrigonometric;
import it.cavallium.warppi.math.parser.features.interfaces.Feature;
import it.cavallium.warppi.util.Error;

public final class BlockTrigonometric extends BlockParenthesisAbstract {
	public enum Kind {
		SINE("sin"), COSINE("cos"), TANGENT("tan"),
		ARC_SINE("sin⁻¹"), ARC_COSINE("cos⁻¹"), ARC_TANGENT("tan⁻¹");

		private final String prefix;

		Kind(final String prefix) {
			this.prefix = prefix;
		}
	}

	private final Kind kind;

	public BlockTrigonometric(final Kind kind) {
		super(Objects.requireNonNull(kind, "kind").prefix);
		this.kind = kind;
	}

	private BlockTrigonometric(final TreeContainer parent, final BlockTrigonometric old,
			final InputContext inputContext) {
		super(parent, old, inputContext);
		kind = old.kind;
	}

	@Override
	public Feature toFeature(final MathContext context) throws Error {
		final Function child = getNumberContainer().toFunction(context);
		return new FeatureTrigonometric(kind, child);
	}

	@Override
	public BlockTrigonometric clone(final TreeContainer parent, final InputContext inputContext) {
		return new BlockTrigonometric(parent, this, inputContext);
	}
}
