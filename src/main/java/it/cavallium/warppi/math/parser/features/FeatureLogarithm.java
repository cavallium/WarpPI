package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Logarithm;

public class FeatureLogarithm extends FeatureDoubleImpl {

	public FeatureLogarithm(Object child1, Object child2) {
		super(child1, child2);
	}

	@Override
	public Logarithm toFunction(MathContext context) throws Error {
		return new Logarithm(context, getFunction1(), getFunction2());
	}

}
