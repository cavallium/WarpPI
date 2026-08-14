package it.cavallium.warppi.math.parser.features;

import java.util.Objects;

import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.ScientificFunction;
import it.cavallium.warppi.math.functions.ScientificFunctionKind;
import it.cavallium.warppi.util.Error;

public final class FeatureScientificFunction extends FeatureSingleImpl {
	private final ScientificFunctionKind kind;

	public FeatureScientificFunction(final ScientificFunctionKind kind, final Object child) {
		super(child);
		this.kind = Objects.requireNonNull(kind, "kind");
	}

	@Override
	public Function toFunction(final MathContext context) throws Error {
		return new ScientificFunction(context, kind, getFunction1());
	}
}
