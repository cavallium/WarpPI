package it.cavallium.warppi.math.parser.features;

import java.util.Objects;

import it.cavallium.warppi.gui.expression.blocks.BlockTrigonometric.Kind;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.trigonometry.ArcCosine;
import it.cavallium.warppi.math.functions.trigonometry.ArcSine;
import it.cavallium.warppi.math.functions.trigonometry.ArcTangent;
import it.cavallium.warppi.math.functions.trigonometry.Cosine;
import it.cavallium.warppi.math.functions.trigonometry.Sine;
import it.cavallium.warppi.math.functions.trigonometry.Tangent;
import it.cavallium.warppi.util.Error;

public final class FeatureTrigonometric extends FeatureSingleImpl {
	private final Kind kind;

	public FeatureTrigonometric(final Kind kind, final Object child) {
		super(child);
		this.kind = Objects.requireNonNull(kind, "kind");
	}

	@Override
	public Function toFunction(final MathContext context) throws Error {
		return switch (kind) {
			case SINE -> new Sine(context, getFunction1());
			case COSINE -> new Cosine(context, getFunction1());
			case TANGENT -> new Tangent(context, getFunction1());
			case ARC_SINE -> new ArcSine(context, getFunction1());
			case ARC_COSINE -> new ArcCosine(context, getFunction1());
			case ARC_TANGENT -> new ArcTangent(context, getFunction1());
		};
	}
}
