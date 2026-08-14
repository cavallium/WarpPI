package it.cavallium.warppi.math.parser.features;

import java.util.Objects;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.ScientificFunctionKind;

/** Temporary parser feature; it is attached to the feature on its left. */
public final class FeaturePostfixFunction extends FeatureSingleImpl {
	private final ScientificFunctionKind kind;

	public FeaturePostfixFunction(final ScientificFunctionKind kind) {
		super(null);
		if (!Objects.requireNonNull(kind, "kind").isPostfix())
			throw new IllegalArgumentException(kind + " is not postfix");
		this.kind = kind;
	}

	public ScientificFunctionKind getKind() {
		return kind;
	}

	@Override
	public Function toFunction(final MathContext context) {
		return null;
	}
}
