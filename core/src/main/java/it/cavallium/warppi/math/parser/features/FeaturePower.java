package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Power;
import it.cavallium.warppi.util.Error;

public class FeaturePower extends FeatureDoubleImpl {

	public FeaturePower(final Object child1, final Object child2) {
		super(child1, child2);
	}

	@Override
	public Power toFunction(final MathContext context) throws Error {
		return new Power(context, getFunction1(), getFunction2());
	}

}
