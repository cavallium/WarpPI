package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Negative;
import it.cavallium.warppi.util.Error;

public final class FeatureNegative extends FeatureSingleImpl {
	public FeatureNegative(final Object child) {
		super(child);
	}

	@Override
	public Negative toFunction(final MathContext context) throws Error {
		return new Negative(context, getFunction1());
	}
}
