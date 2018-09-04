package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Power;

public class FeaturePower extends FeatureDoubleImpl {

	public FeaturePower(Object child1, Object child2) {
		super(child1, child2);
	}

	@Override
	public Power toFunction(MathContext context) throws Error {
		return new Power(context, getFunction1(), getFunction2());
	}

}
