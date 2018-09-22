package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Logarithm;
import it.cavallium.warppi.util.Error;

public class FeatureLogarithm extends FeatureDoubleImpl {

	public FeatureLogarithm(final Object child1, final Object child2) {
		super(child1, child2);
	}

	@Override
	public Logarithm toFunction(final MathContext context) throws Error {
		return new Logarithm(context, getFunction1(), getFunction2());
	}

}
