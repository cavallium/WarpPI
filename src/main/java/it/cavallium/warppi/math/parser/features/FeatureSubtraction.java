package it.cavallium.warppi.math.parser.features;

import it.cavallium.warppi.Error;
import it.cavallium.warppi.math.Function;
import it.cavallium.warppi.math.MathContext;
import it.cavallium.warppi.math.functions.Subtraction;

public class FeatureSubtraction extends FeatureDoubleImpl {

	public FeatureSubtraction(Object child1, Object child2) {
		super(child1, child2);
	}

	@Override
	public Function toFunction(MathContext context) throws Error {
		return new Subtraction(context, getFunction1(), getFunction2());
	}

}
