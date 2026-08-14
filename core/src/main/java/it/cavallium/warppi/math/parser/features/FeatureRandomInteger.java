package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.RandomInteger;
import it.cavallium.warppi.util.Error;

public final class FeatureRandomInteger extends FeatureDoubleImpl {
	public FeatureRandomInteger(final Object minimum, final Object maximum) {
		super(minimum, maximum);
	}

	@Override
	public RandomInteger toFunction(final MathContext context) throws Error {
		return new RandomInteger(context, getFunction1(), getFunction2());
	}
}
