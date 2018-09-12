package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.SumSubtraction;
import it.cavallium.warppi.util.Error;

public class FeatureSumSubtraction extends FeatureDoubleImpl {

	public FeatureSumSubtraction(Object child1, Object child2) {
		super(child1, child2);
	}

	@Override
	public SumSubtraction toFunction(MathContext context) throws Error {
		return new SumSubtraction(context, getFunction1(), getFunction2());
	}

}
