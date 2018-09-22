package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Sum;
import it.cavallium.warppi.util.Error;

public class FeatureSum extends FeatureDoubleImpl {

	public FeatureSum(final Object child1, final Object child2) {
		super(child1, child2);
	}

	@Override
	public Sum toFunction(final MathContext context) throws Error {
		return new Sum(context, getFunction1(), getFunction2());
	}

}
